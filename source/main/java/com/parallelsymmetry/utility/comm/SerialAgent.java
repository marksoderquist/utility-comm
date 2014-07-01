package com.parallelsymmetry.utility.comm;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.ParseException;
import java.util.TooManyListenersException;

import com.parallelsymmetry.utility.ConfigurationException;
import com.parallelsymmetry.utility.Parameters;
import com.parallelsymmetry.utility.agent.PipeAgent;
import com.parallelsymmetry.utility.log.Log;

/**
 * @author mvsoder
 */
public class SerialAgent extends PipeAgent implements SerialPortEventListener {

	private static final int RETRY_COUNT = 10;

	private static final int DEFAULT_BUFFER_SIZE = 256;

	private CommPortIdentifier identifier;

	private SerialSettings settings;

	private SerialPort port;

	private InputStream input;

	private OutputStream output;

	private PipedOutputStream serialOutput;

	private SerialInputStream serialInput;

	private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

	public SerialAgent() {
		super();
	}

	public SerialAgent( String name ) {
		super( name );
	}

	public SerialAgent( String name, String port, int baud, int bits, int parity, int stop ) {
		super( name );
		configure( port, baud, bits, parity, stop );
	}

	public SerialAgent( String name, String port, SerialSettings settings ) {
		super( name );
		this.settings = settings;
		setStopOnConnectException( true );

		if( !isRxtxSerialLibAvailable() ) throw new RuntimeException( "RXTX library is not installed." );
	}

	public void configure( String name, int baud, int bits, int parity, int stop ) {
		this.settings = new SerialSettings( name, baud, bits, parity, stop );
	}

	public void configure( Parameters parameters ) throws ConfigurationException {
		String name = parameters.get( "comm.port", "COM3" );

		// Check if the port exists.
		try {
			CommPortIdentifier.getPortIdentifier( name );
			int rate = SerialSettings.parseBaud( parameters.get( "comm.rate", "9600" ) );
			int bits = SerialSettings.parseData( parameters.get( "comm.bits", "8" ) );
			int stop = SerialSettings.parseStop( parameters.get( "comm.stop", "1" ) );
			int parity = SerialSettings.parseParity( parameters.get( "comm.parity", "N" ) );
			configure( name, rate, bits, parity, stop );
		} catch( NoSuchPortException exception ) {
			throw new ConfigurationException( "No such port: " + name, exception );
		} catch( ParseException exception ) {
			throw new ConfigurationException( exception );
		}
	}

	public SerialPort getSerialPort() {
		return port;
	}

	@Override
	public void serialEvent( SerialPortEvent event ) {
		switch( event.getEventType() ) {
			case SerialPortEvent.DATA_AVAILABLE: {
				dataAvailable();
				break;
			}
		}
	}

	public static final boolean isRxtxSerialLibAvailable() {
		try {
			// This odd line of code helps deal with a difference between running in
			// the real work and running in a development environment.
			if( SerialAgent.class.getClassLoader() == CommPortIdentifier.class.getClassLoader() ) System.loadLibrary( "rxtxSerial" );
			CommPortIdentifier.getPortIdentifiers();
		} catch( UnsatisfiedLinkError error ) {
			Log.write( Log.DEBUG, error.getMessage() );
			return false;
		}
		return true;
	}

	@Override
	protected void startAgent() throws Exception {
		serialConnect();
		super.startAgent();
	}

	@Override
	protected void stopAgent() throws Exception {
		super.stopAgent();
		serialDisconnect();
	}

	@Override
	protected void connect() throws Exception {
		serialOutput = new PipedOutputStream();
		serialInput = new SerialInputStream( serialOutput );
		setRealInputStream( serialInput );
		setRealOutputStream( new SerialOutputStream( output ) );
	}

	@Override
	protected void disconnect() throws Exception {
		OutputStream output = getRealOutputStream();
		if( output != null ) output.close();
		setRealOutputStream( null );

		InputStream input = getRealInputStream();
		if( input != null ) input.close();
		setRealInputStream( null );
	}

	private void serialConnect() throws IOException {

		if( settings == null ) {
			Log.write( Log.ERROR, getName(), " Serial device settings are null." );
			return;
		}

		try {
			Log.write( Log.DEBUG, getName(), " Opening serial port: [" + settings.toString() + "]..." );
			if( identifier == null ) identifier = CommPortIdentifier.getPortIdentifier( settings.getName() );
			port = (SerialPort)identifier.open( getName(), 0 );

			setSerialSettings( port, settings );
			try {
				port.setFlowControlMode( SerialPort.FLOWCONTROL_NONE );
			} catch( UnsupportedCommOperationException exception ) {
				throw new IOException( exception );
			}
			port.setDTR( false );
			port.setRTS( false );

			SerialSettings actualSettings = new SerialSettings( port );
			if( !actualSettings.equals( settings ) ) throw new IOException( "Actual port settings are not requested settings: " + actualSettings + " != " + settings );

			input = port.getInputStream();
			output = port.getOutputStream();

			try {
				port.addEventListener( this );
			} catch( TooManyListenersException exception ) {
				Log.write( exception, port.toString() );
			}
			port.notifyOnDataAvailable( true );

			Log.write( Log.TRACE, getName(), " Serial port open:  [" + actualSettings.toString() + "]." );
		} catch( NoSuchPortException exception ) {
			throw new IOException( "Port does not exist: " + settings.getName(), exception );
		} catch( PortInUseException exception ) {
			throw new IOException( exception );
		}
	}

	private void dataAvailable() {
		int read;
		try {
			while( input.available() > 0 && ( read = input.read( buffer ) ) > -1 ) {
				serialOutput.write( buffer, 0, read );
				serialOutput.flush();
			}
		} catch( IOException exception ) {
			serialInput.setException( exception );
		}
	}

	private void serialDisconnect() throws IOException {
		try {
			port.removeEventListener();
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}

		if( output != null ) output.close();
		if( input != null ) input.close();
		if( port != null ) port.close();
	}

	private void setSerialSettings( SerialPort port, SerialSettings settings ) {
		setSerialSettings( port, settings.getBaud(), settings.getBits(), settings.getParity(), settings.getStop() );
	}

	private void setSerialSettings( SerialPort port, int baud, int bits, int parity, int stop ) {
		int attempt = 0;
		while( attempt < RETRY_COUNT ) {
			try {
				port.setSerialPortParams( baud, bits, stop, parity );
				break;
			} catch( UnsupportedCommOperationException exception ) {
				exception.printStackTrace();
				continue;
			} catch( Exception exception ) {
				try {
					Thread.sleep( 10 * attempt );
				} catch( InterruptedException pauseException ) {
					return;
				}
				continue;
			} finally {
				attempt++;
			}
		}
	}

	private class SerialInputStream extends PipedInputStream {

		private IOException exception;

		public SerialInputStream( PipedOutputStream output ) throws IOException {
			super( output, DEFAULT_BUFFER_SIZE );
		}

		@Override
		public int read() throws IOException {
			checkException();
			return super.read();
		}

		@Override
		public int read( byte[] buffer ) throws IOException {
			checkException();
			return super.read( buffer );
		}

		@Override
		public int read( byte[] buffer, int offset, int length ) throws IOException {
			checkException();
			return super.read( buffer, offset, length );
		}

		public void setException( IOException exception ) {
			this.exception = exception;
		}

		private void checkException() throws IOException {
			if( exception != null ) throw exception;
		}

	}

	private class SerialOutputStream extends OutputStream {

		private OutputStream output;

		public SerialOutputStream( OutputStream output ) throws IOException {
			this.output = output;
		}

		@Override
		public void write( int bite ) throws IOException {
			output.write( bite );
		}

		@Override
		public void write( byte[] buffer ) throws IOException {
			output.write( buffer );
		}

		@Override
		public void write( byte[] buffer, int offset, int length ) throws IOException {
			output.write( buffer, offset, length );
		}

		@Override
		public void flush() throws IOException {
			output.flush();
		}

		@Override
		public void close() throws IOException {
			output = null;
		}

	}

}
