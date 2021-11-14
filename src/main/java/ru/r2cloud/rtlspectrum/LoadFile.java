package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class LoadFile extends StatusBarTask<List<BinData>> {

	private final File file;

	public LoadFile(StatusBar statusBar, File file) {
		super(statusBar);
		this.file = file;
	}

	@Override
	protected List<BinData> call() throws Exception {
		updateMessage("Reading file: " + file.getAbsolutePath());
		BinDataParser parser = new BinDataParser();
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String curLine = null;
			while ((curLine = r.readLine()) != null) {
				parser.addLine(curLine);
			}
		}
		return parser.convert();
	}

}
