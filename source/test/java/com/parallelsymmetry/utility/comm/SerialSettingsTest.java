package com.parallelsymmetry.utility.comm;

import org.junit.jupiter.api.Test;
import purejavacomm.SerialPort;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

public class SerialSettingsTest {

	@Test
	public void testParse() throws Exception {
		SerialSettings settings = null;

		try {
			settings = SerialSettings.parse( null );
			fail( "ParseException should have been thrown" );
		} catch( ParseException exception ) {
			// Allow test to pass.
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "" );
		} catch( ParseException exception ) {
			// Allow test to pass.
		}
		assertNull( settings );

		settings = SerialSettings.parse( "COM1,300,5,n,1" );
		assertEquals( "COM1", settings.getName() );
		assertEquals( 300, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_5, settings.getBits() );
		assertEquals( SerialPort.PARITY_NONE, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_1, settings.getStop() );

		settings = SerialSettings.parse( "COM2,2400,6,e,1.5" );
		assertEquals( "COM2", settings.getName() );
		assertEquals( 2400, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_6, settings.getBits() );
		assertEquals( SerialPort.PARITY_EVEN, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_1_5, settings.getStop() );

		settings = SerialSettings.parse( "COM3,4800,7,o,2" );
		assertEquals( "COM3", settings.getName() );
		assertEquals( 4800, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_7, settings.getBits() );
		assertEquals( SerialPort.PARITY_ODD, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_2, settings.getStop() );

		settings = SerialSettings.parse( "COM4,9600,8,m,1" );
		assertEquals( "COM4", settings.getName() );
		assertEquals( 9600, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_8, settings.getBits() );
		assertEquals( SerialPort.PARITY_MARK, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_1, settings.getStop() );

		settings = SerialSettings.parse( "/dev/ttyS0,14400,8,s,1" );
		assertEquals( "/dev/ttyS0", settings.getName() );
		assertEquals( 14400, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_8, settings.getBits() );
		assertEquals( SerialPort.PARITY_SPACE, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_1, settings.getStop() );

		settings = SerialSettings.parse( "/dev/ttyS1,19200,7,e,1" );
		assertEquals( "/dev/ttyS1", settings.getName() );
		assertEquals( 19200, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_7, settings.getBits() );
		assertEquals( SerialPort.PARITY_EVEN, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_1, settings.getStop() );

		settings = SerialSettings.parse( "/dev/ttyUSB0,57600,8,n,1" );
		assertEquals( "/dev/ttyUSB0", settings.getName() );
		assertEquals( 57600, settings.getBaud() );
		assertEquals( SerialPort.DATABITS_8, settings.getBits() );
		assertEquals( SerialPort.PARITY_NONE, settings.getParity() );
		assertEquals( SerialPort.STOPBITS_1, settings.getStop() );
	}

	@Test
	public void testParseBaudFailures() throws Exception {
		SerialSettings settings = null;

		try {
			settings = SerialSettings.parse( "COM3,bad,8,n,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 2, exception.getErrorOffset() );
		}
		assertNull( settings );
	}

	@Test
	public void testParseBitsFailures() throws Exception {
		SerialSettings settings = null;

		try {
			settings = SerialSettings.parse( "COM3,300,bad,n,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 3, exception.getErrorOffset() );
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "COM3,300,4,n,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 3, exception.getErrorOffset() );
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "COM3,300,9,n,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 3, exception.getErrorOffset() );
		}
		assertNull( settings );
	}

	@Test
	public void testParseParityFailures() throws Exception {
		SerialSettings settings = null;

		try {
			settings = SerialSettings.parse( "COM3,300,8,bad,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 4, exception.getErrorOffset() );
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "COM3,300,8,a,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 4, exception.getErrorOffset() );
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "COM3,300,8,8,1" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 4, exception.getErrorOffset() );
		}
		assertNull( settings );
	}

	@Test
	public void testParseStopBitsFailures() throws Exception {
		SerialSettings settings = null;

		try {
			settings = SerialSettings.parse( "COM3,300,8,n,bad" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 5, exception.getErrorOffset() );
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "COM3,300,8,n,0" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 5, exception.getErrorOffset() );
		}
		assertNull( settings );

		try {
			settings = SerialSettings.parse( "COM3,300,8,n,4" );
			fail( "ParseException should have been thrown." );
		} catch( ParseException exception ) {
			assertEquals( 5, exception.getErrorOffset() );
		}
		assertNull( settings );
	}

	@Test
	public void testToString() throws Exception {
		String settings;

		settings = "COM3,300,5,n,1";
		assertEquals( settings, SerialSettings.parse( settings ).toString() );
		settings = "COM3,2400,6,e,1.5";
		assertEquals( settings, SerialSettings.parse( settings ).toString() );
		settings = "COM3,4800,7,o,2";
		assertEquals( settings, SerialSettings.parse( settings ).toString() );
		settings = "COM3,9600,8,m,1";
		assertEquals( settings, SerialSettings.parse( settings ).toString() );
		settings = "COM3,14400,8,s,1";
		assertEquals( settings, SerialSettings.parse( settings ).toString() );
	}

	@Test
	public void testEquals() throws Exception {
		SerialSettings settings1 = SerialSettings.parse( "COM3,9600,8,n,1" );
		SerialSettings settings2 = SerialSettings.parse( "COM3,9600,8,n,1" );
		SerialSettings settings3 = SerialSettings.parse( "COM3,19200,8,n,1" );

		assertEquals( settings1, settings2 );
		assertEquals( settings2, settings1 );
		assertNotEquals( settings1, settings3 );
		assertNotEquals( settings3, settings1 );
	}

}
