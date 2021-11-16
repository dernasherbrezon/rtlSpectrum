package ru.r2cloud.rtlspectrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javafx.scene.chart.XYChart;

public class BinDataParser {

	private static final Pattern COMMA = Pattern.compile(",");
	private final Map<String, BinData> cache = new HashMap<>();

	public void addLine(String line) {
		Map<String, BinData> cur = convertLine(line);
		if (cur.isEmpty()) {
			return;
		}
		for (Entry<String, BinData> curEntry : cur.entrySet()) {
			BinData existingValue = cache.get(curEntry.getKey());
			if (existingValue == null) {
				cache.put(curEntry.getKey(), curEntry.getValue());
				continue;
			}
			existingValue.setDbmTotal(existingValue.getDbmTotal() + curEntry.getValue().getDbmTotal());
			existingValue.setDbmCount(existingValue.getDbmCount() + curEntry.getValue().getDbmCount());
		}
	}

	public List<BinData> convert() {
		List<BinData> result = new ArrayList<>(cache.values());
		Collections.sort(result, new Comparator<BinData>() {
			@Override
			public int compare(BinData o1, BinData o2) {
				return Long.compare(o1.getFrequencyStartParsed(), o2.getFrequencyStartParsed());
			}
		});
		for (BinData cur : result) {

			XYChart.Data<Number, Number> parsed = new XYChart.Data<>();
			parsed.setXValue(cur.getFrequencyStartParsed());
			parsed.setYValue(cur.getDbmTotal() / cur.getDbmCount());

			cur.setParsed(parsed);
			cur.setDbmAverage(cur.getDbmTotal() / cur.getDbmCount());
		}
		return result;
	}

	// format is: 2019-06-07, 19:44:45, 40000000, 41000000, 1000000.00, 1, -24.22,
	// -24.22, ...
	static Map<String, BinData> convertLine(String line) {
		String[] parts = COMMA.split(line);
		if (parts.length < 7) {
			return Collections.emptyMap();
		}
		String date = parts[0].trim();
		String time = parts[1].trim();
		long frequencyStart = Long.parseLong(parts[2].trim());
		double step = Double.parseDouble(parts[4].trim());
		Map<String, BinData> result = new HashMap<>();
		for (int i = 0; i < parts.length - 7 + 1; i++) {
			BinData cur = new BinData();
			cur.setDate(date);
			cur.setTime(time);
			cur.setFrequencyStartParsed((long) (frequencyStart + i * step));
			cur.setFrequencyStart(String.valueOf(cur.getFrequencyStartParsed()));
			cur.setFrequencyEnd(parts[4].trim());
			cur.setBinSize(parts[4].trim());
			cur.setNumberOfSamples(parts[5].trim());
			double value;
			try {
				value = Double.valueOf(parts[6 + i].trim());
			} catch (NumberFormatException e) {
				continue;
			}

			cur.setDbmTotal(value);
			cur.setDbmCount(1);

			result.put(cur.getFrequencyStart(), cur);
		}
		return result;
	}

}
