package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FrequencyFormatterTest {

	@Test
	public void formatNegative() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("", formatter.toString(-1));
	}
	
	@Test
	public void formatHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10 Hz", formatter.toString(10));
	}

	@Test
	public void formatKHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10.1 KHz", formatter.toString(10100));
	}
	
	@Test
	public void formatMHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10 MHz", formatter.toString(10_001_000));
	}
	
	@Test
	public void formatGHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10.1 GHz", formatter.toString(10_101_000_000L));
	}
	
}
