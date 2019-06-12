package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javafx.scene.chart.XYChart;

public class ReadFromFile extends StatusBarTask<List<XYChart.Data<Number, Number>>> {

	private static final Pattern COMMA = Pattern.compile(",");
	private final File file;

	public ReadFromFile(StatusBar statusBar, File file) {
		super(statusBar);
		this.file = file;
	}

	@Override
	protected List<XYChart.Data<Number, Number>> call() throws Exception {
		long minimum = 24_000_000;
		long maximum = 1_700_000_000;
		ArrayList<XYChart.Data<Number, Number>> result = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new FileReader(file, StandardCharsets.ISO_8859_1))) {
			String curLine = null;
			while ((curLine = r.readLine()) != null) {
				XYChart.Data<Number, Number> cur = parse(curLine);
				result.add(cur);
				updateProgress(cur.getXValue().longValue() - minimum, maximum - minimum);
			}
		}
		return result;
	}

	// format is: 2019-06-07, 19:44:45, 40000000, 41000000, 1000000.00, 1, -24.22, -24.22
	private static XYChart.Data<Number, Number> parse(String line) {
		String[] parts = COMMA.split(line);
		XYChart.Data<Number, Number> result = new XYChart.Data<>();
		result.setXValue(Long.valueOf(parts[2].trim()));
		result.setYValue(Double.valueOf(parts[6].trim()));
		return result;
	}
}
