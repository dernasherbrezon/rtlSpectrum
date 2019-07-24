package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PowerFormatterTest {

	@Test
	public void formatNan() {
		assertEquals("", new PowerFormatter().toString(Double.NaN));
	}
	
	@Test
	public void format() {
		PowerFormatter formatter = new PowerFormatter();
		assertEquals("23.46", formatter.toString(23.4567));
	}

}
