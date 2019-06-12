package ru.r2cloud.rtlspectrum;

import javafx.concurrent.Task;

public abstract class StatusBarTask<V> extends Task<V> {

	protected final StatusBar statusBar;
	
	public StatusBarTask(StatusBar statusBar) {
		this.statusBar = statusBar;
		this.statusBar.progressProperty().bind(progressProperty());
	}
	
}
