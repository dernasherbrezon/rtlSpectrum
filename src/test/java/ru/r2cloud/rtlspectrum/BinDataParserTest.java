package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BinDataParserTest {

	private BinDataParser parser;

	@Test
	public void testMergeIntervals() {
		parser.addLine("2021-11-14, 20:27:18, 433006, 435994, 58.59, 342414, -27.70, nan");
		parser.addLine("2021-11-14, 20:27:18, 433006, 435994, 58.59, 342414, nan, -26.70");

		List<BinData> result = parser.convert();
		assertEquals(2, result.size());
		assertBinData(result.get(0), "433006", -27.70);
		assertBinData(result.get(1), "433064", -26.70);
	}

	@Test
	public void testAverage() {
		parser.addLine("2021-11-14, 20:27:18, 433006, 435994, 58.59, 342414, -30.0, -60.0");
		parser.addLine("2021-11-14, 20:28:18, 433006, 435994, 58.59, 342414, -60.0, -30.0");

		List<BinData> result = parser.convert();
		assertEquals(2, result.size());
		assertBinData(result.get(0), "433006", -45.0);
		assertBinData(result.get(1), "433064", -45.0);
	}

	private static void assertBinData(BinData actual, String frequencyStart, double value) {
		assertEquals(actual.getFrequencyStart(), frequencyStart);
		assertEquals(actual.getDbmAverage(), value, 0.0);
	}

	@Before
	public void start() {
		parser = new BinDataParser();
	}
}
