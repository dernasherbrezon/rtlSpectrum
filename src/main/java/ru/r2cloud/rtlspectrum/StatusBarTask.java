package ru.r2cloud.rtlspectrum;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.concurrent.Task;

public abstract class StatusBarTask<V> extends Task<V> {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("mm'm':ss's'");
	protected final StatusBar statusBar;

	public StatusBarTask(StatusBar statusBar) {
		this.statusBar = statusBar;
		this.statusBar.progressProperty().bind(progressProperty());
		this.statusBar.messageProperty().bind(messageProperty());
	}

	public void updateProgress(String message, long workDone, long max) {
		updateMessage(message);
		updateProgress(workDone, max);
	}
	
	public static String formatETA(long time) {
		return sdf.format(new Date(time));
	}
}
