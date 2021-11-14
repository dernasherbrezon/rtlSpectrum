package ru.r2cloud.rtlspectrum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class SaveTask extends StatusBarTask<Void> {

	private final File file;
	private final List<BinData> data;

	public SaveTask(StatusBar statusBar, File file, List<BinData> data) {
		super(statusBar);
		this.file = file;
		this.data = data;
	}

	@Override
	protected Void call() throws Exception {
		updateMessage("Saving to file: " + file.getAbsolutePath());
		try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
			for (int i = 0; i < data.size(); i++) {
				BinData cur = data.get(i);
				w.append(cur.getDate()).append(',').append(cur.getTime()).append(',').append(cur.getFrequencyStart());
				w.append(',').append(cur.getFrequencyEnd()).append(',').append(cur.getBinSize()).append(',');
				w.append(cur.getNumberOfSamples());
				w.append(',').append(String.valueOf(cur.getDbmAverage()));
				w.append('\n');
				updateProgress(i, data.size());
			}
		}
		return null;
	}
}
