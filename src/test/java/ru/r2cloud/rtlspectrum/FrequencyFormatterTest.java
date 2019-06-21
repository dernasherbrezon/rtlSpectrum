package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FrequencyFormatterTest {

	@Test
	public void formatHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10 hz", formatter.toString(10));
	}

	@Test
	public void formatMHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10 Mhz", formatter.toString(10_001_000));
	}
	
	@Test
	public void formatGHz() {
		FrequencyFormatter formatter = new FrequencyFormatter();
		assertEquals("10.1 Ghz", formatter.toString(10_101_000_000L));
	}
	
}
