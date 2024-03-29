package ru.r2cloud.rtlspectrum;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.concurrent.Task;

public abstract class StatusBarTask<V> extends Task<V> {

	private final SimpleDateFormat sdf = new SimpleDateFormat("mm'm':ss's'");
	protected final StatusBar statusBar;

	protected StatusBarTask(StatusBar statusBar) {
		this.statusBar = statusBar;
		this.statusBar.progressProperty().bind(progressProperty());
		this.statusBar.messageProperty().bind(messageProperty());
	}

	public void updateProgress(String message, long workDone, long max) {
		updateMessage(message);
		updateProgress(workDone, max);
	}
	
	public String formatETA(long time) {
		return sdf.format(new Date(time));
	}
}
