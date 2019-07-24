package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javafx.scene.chart.XYChart;

public class LoadFile extends StatusBarTask<List<BinData>> {

	private static final Pattern COMMA = Pattern.compile(",");
	private final File file;

	public LoadFile(StatusBar statusBar, File file) {
		super(statusBar);
		this.file = file;
	}

	@Override
	protected List<BinData> call() throws Exception {
		updateMessage("Reading file: " + file.getAbsolutePath());
		long minimum = 24_000_000;
		long maximum = 1_700_000_000;
		List<BinData> result = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String curLine = null;
			while ((curLine = r.readLine()) != null) {
				BinData cur = convert(curLine);
				if (cur == null) {
					continue;
				}
				result.add(cur);
				updateProgress(cur.getParsed().getXValue().longValue() - minimum, maximum - minimum);
			}
		}
		return result;
	}

	// format is: 2019-06-07, 19:44:45, 40000000, 41000000, 1000000.00, 1, -24.22, -24.22, ...
	static BinData convert(String line) {
		String[] parts = COMMA.split(line);
		if (parts.length < 7) {
			return null;
		}

		BinData result = new BinData();
		int i = 0;
		result.setDate(parts[i++].trim());
		result.setTime(parts[i++].trim());
		result.setFrequencyStart(parts[i++].trim());
		result.setFrequencyEnd(parts[i++].trim());
		result.setBinSize(parts[i++].trim());
		result.setNumberOfSamples(parts[i++].trim());
		List<String> dbm = new ArrayList<>();
		Double totalValue = null;
		int totalCount = 0;
		for (; i < parts.length; i++) {
			String cur = parts[i].trim();
			dbm.add(cur);
			try {
				double curValue = Double.valueOf(cur);
				if (totalValue == null) {
					totalValue = curValue;
				} else {
					totalValue += curValue;
				}
				totalCount++;
			} catch (NumberFormatException e) {
				continue;
			}
		}
		if (totalValue == null) {
			totalValue = Double.NaN;
		}
		if (totalCount == 0) {
			result.setDbmAverage(Double.NaN);
		} else {
			result.setDbmAverage(totalValue / totalCount);
		}
		result.setDbm(dbm);

		XYChart.Data<Number, Number> parsed = new XYChart.Data<>();
		parsed.setXValue(Long.valueOf(result.getFrequencyStart()));
		parsed.setYValue(result.getDbmAverage());

		result.setParsed(parsed);
		return result;
	}
}
