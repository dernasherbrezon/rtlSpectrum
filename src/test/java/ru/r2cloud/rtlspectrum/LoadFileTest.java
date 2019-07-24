package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoadFileTest {

	@Test
	public void testGaps() throws Exception {
		BinData data = LoadFile.convert("2019-06-16,23:10:56,26000000,27000000,1000000.00,1,-inf");
		assertTrue(Double.isNaN(data.getDbmAverage()));
	}

	@Test
	public void testGaps2() throws Exception {
		BinData data = LoadFile.convert("2019-06-16,23:10:56,26000000,27000000,1000000.00,1,12.76,-inf");
		assertEquals(12.76, data.getDbmAverage(), 0.00001);
	}

}
