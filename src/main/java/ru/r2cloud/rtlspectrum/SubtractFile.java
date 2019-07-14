package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.chart.XYChart;

public class SubtractFile extends StatusBarTask<List<List<BinData>>> {

	private final File file;
	private final List<List<BinData>> rawData;

	public SubtractFile(StatusBar statusBar, File file, List<List<BinData>> rawData) {
		super(statusBar);
		this.file = file;
		this.rawData = rawData;
	}

	@Override
	protected List<List<BinData>> call() throws Exception {
		updateMessage("Reading file: " + file.getAbsolutePath());
		long minimum = 24_000_000;
		long maximum = 1_700_000_000;
		List<BinData> fileData = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String curLine = null;
			while ((curLine = r.readLine()) != null) {
				BinData cur = LoadFile.convert(curLine);
				fileData.add(cur);
				updateProgress(cur.getParsed().getXValue().longValue() - minimum, maximum - minimum);
			}
		}
		List<List<BinData>> result = new ArrayList<>();
		for (List<BinData> cur : rawData) {
			List<BinData> resultGraph = new ArrayList<>();
			for (BinData curBin : cur) {
				BinData fileBin = findByFrequency(fileData, curBin.getFrequencyStart());
				if (fileBin == null) {
					// copy parsed as well
					resultGraph.add(new BinData(curBin));
					continue;
				}

				double dbm1 = curBin.getDbmAverage();
				double dbm2 = fileBin.getDbmAverage();

				double value = dbm1 - dbm2;

				XYChart.Data<Number, Number> parsed = new XYChart.Data<>();
				parsed.setXValue(Long.valueOf(curBin.getFrequencyStart()));
				parsed.setYValue(value);

				BinData subtractedBin = new BinData(curBin);
				subtractedBin.setDbmAverage(value);
				subtractedBin.setParsed(parsed);
				subtractedBin.setDbm(Collections.singletonList(String.valueOf(value)));
				resultGraph.add(subtractedBin);
			}
			result.add(resultGraph);
		}
		return result;
	}

	private static BinData findByFrequency(List<BinData> data, String frequency) {
		for (BinData cur : data) {
			if (cur.getFrequencyStart().equals(frequency)) {
				return cur;
			}
		}
		return null;
	}

}
