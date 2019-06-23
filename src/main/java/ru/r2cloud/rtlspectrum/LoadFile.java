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
		System.out.println("load file: " + file.getAbsolutePath());
		updateMessage("Reading file: " + file.getAbsolutePath());
		long minimum = 24_000_000;
		long maximum = 1_700_000_000;
		List<BinData> result = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String curLine = null;
			while ((curLine = r.readLine()) != null) {
				BinData cur = convert(curLine);
				result.add(cur);
				updateProgress(cur.getParsed().getXValue().longValue() - minimum, maximum - minimum);
			}
		}
		return result;
	}

	// format is: 2019-06-07, 19:44:45, 40000000, 41000000, 1000000.00, 1, -24.22, -24.22
	static BinData convert(String line) {
		String[] parts = COMMA.split(line);
		if (parts.length < 8) {
			return null;
		}

		BinData result = new BinData();
		result.setDate(parts[0].trim());
		result.setTime(parts[1].trim());
		result.setFrequencyStart(parts[2].trim());
		result.setFrequencyEnd(parts[3].trim());
		result.setBinSize(parts[4].trim());
		result.setNumberOfSamples(parts[5].trim());
		result.setDbmStart(parts[6].trim());
		result.setDbmEnd(parts[7].trim());

		XYChart.Data<Number, Number> parsed = new XYChart.Data<>();
		parsed.setXValue(Long.valueOf(result.getFrequencyStart()));
		parsed.setYValue(Double.valueOf(result.getDbmStart()));

		result.setParsed(parsed);
		return result;
	}
}
