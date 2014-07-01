package com.parallelsymmetry.util.comm;

import gnu.io.SerialPort;

import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class SerialSettings {

	private String name;

	private int baud;

	private int bits;

	private int parity;

	private int stop;

	public SerialSettings( SerialPort port ) {
		this( port.getName(), port.getBaudRate(), port.getDataBits(), port.getParity(), port.getStopBits() );
	}

	public SerialSettings( String name, int baud, int bits, int parity, int stop ) {
		this.name = name;
		this.baud = baud;
		this.bits = bits;
		this.parity = parity;
		this.stop = stop;
	}

	public static final SerialSettings parse( String settings ) throws ParseException {
		if( settings == null ) throw new ParseException( "Settings cannot be null.", 0 );
		if( "".equals( settings ) ) throw new ParseException( "Settings cannot be empty.", 0 );

		StringTokenizer tokenizer = new StringTokenizer( settings, "," );

		String name = null;
		int baud = -1;
		int bits = -1;
		int parity = -1;
		int stop = -1;

		try {
			name = tokenizer.nextToken();
		} catch( NoSuchElementException exception ) {
			throw new ParseException( "Missing name.", 1 );
		}

		try {
			baud = parseBaud( tokenizer.nextToken() );
		} catch( NoSuchElementException exception ) {
			throw new ParseException( "Missing data rate.", 2 );
		}

		try {
			bits = parseData( tokenizer.nextToken() );
		} catch( NoSuchElementException exception ) {
			throw new ParseException( "Missing data bits.", 3 );
		}

		try {
			parity = parseParity( tokenizer.nextToken().toLowerCase() );
		} catch( NoSuchElementException exception ) {
			throw new ParseException( "Missing parity bits.", 4 );
		}

		try {
			stop = parseStop( tokenizer.nextToken() );
		} catch( NoSuchElementException exception ) {
			throw new ParseException( "Missing stop bits.", 5 );
		}

		return new SerialSettings( name, baud, bits, parity, stop );
	}

	public static final int parseBaud( String baud ) throws ParseException {
		try {
			return Integer.parseInt( baud );
		} catch( NumberFormatException exception ) {
			throw new ParseException( "Invalid data rate.", 2 );
		}
	}

	public static final int parseData( String data ) throws ParseException {
		if( "8".equals( data ) ) {
			return SerialPort.DATABITS_8;
		} else if( "7".equals( data ) ) {
			return SerialPort.DATABITS_7;
		} else if( "6".equals( data ) ) {
			return SerialPort.DATABITS_6;
		} else if( "5".equals( data ) ) {
			return SerialPort.DATABITS_5;
		}

		throw new ParseException( "Invalid data bits.", 3 );
	}

	public static final int parseParity( String parity ) throws ParseException {
		parity = parity.toLowerCase();

		if( "n".equals( parity ) ) {
			return SerialPort.PARITY_NONE;
		} else if( "e".equals( parity ) ) {
			return SerialPort.PARITY_EVEN;
		} else if( "o".equals( parity ) ) {
			return SerialPort.PARITY_ODD;
		} else if( "m".equals( parity ) ) {
			return SerialPort.PARITY_MARK;
		} else if( "s".equals( parity ) ) {
			return SerialPort.PARITY_SPACE;
		}

		throw new ParseException( "Invalid parity bits.", 4 );
	}

	public static final int parseStop( String stop ) throws ParseException {
		if( stop.equals( "1" ) ) {
			return SerialPort.STOPBITS_1;
		} else if( stop.equals( "2" ) ) {
			return SerialPort.STOPBITS_2;
		} else if( stop.equals( "1.5" ) ) {
			return SerialPort.STOPBITS_1_5;
		}

		throw new ParseException( "Invalid stop bits.", 5 );
	}

	public String getName() {
		return name;
	}

	public int getBaud() {
		return baud;
	}

	public int getBits() {
		return bits;
	}

	public int getParity() {
		return parity;
	}

	public int getStop() {
		return stop;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		String baudString = String.valueOf( baud );
		String bitsString = String.valueOf( bits );
		String parityString = "?";
		switch( parity ) {
			case SerialPort.PARITY_NONE: {
				parityString = "n";
				break;
			}
			case SerialPort.PARITY_EVEN: {
				parityString = "e";
				break;
			}
			case SerialPort.PARITY_ODD: {
				parityString = "o";
				break;
			}
			case SerialPort.PARITY_MARK: {
				parityString = "m";
				break;
			}
			case SerialPort.PARITY_SPACE: {
				parityString = "s";
				break;
			}
		}
		String stopString = String.valueOf( stop );
		if( stop == SerialPort.STOPBITS_1_5 ) stopString = "1.5";

		builder.append( name );
		builder.append( "," );
		builder.append( baudString );
		builder.append( "," );
		builder.append( bitsString );
		builder.append( "," );
		builder.append( parityString );
		builder.append( "," );
		builder.append( stopString );

		return builder.toString();
	}

	@Override
	public boolean equals( Object object ) {
		if( !( object instanceof SerialSettings ) ) return false;
		SerialSettings that = (SerialSettings)object;
		return this.baud == that.baud && this.bits == that.bits && this.parity == that.parity && this.stop == that.stop;
	}

}
