package ru.r2cloud.rtlspectrum;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class StatusBar {

	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label statusMessage;

	private double previousValue = -1;

	public void beginTask() {
		progressBar.setVisible(true);
	}

	public void completeTask() {
		completeTask(null);
	}

	public void completeTask(String message) {
		statusMessage.textProperty().unbind();
		if (message != null) {
			statusMessage.textProperty().set(message);
		} else {
			statusMessage.textProperty().set("OK");
		}
		progressBar.setVisible(false);
		previousValue = -1;
		progressBar.progressProperty().unbind();
		progressBar.progressProperty().set(previousValue);
	}

	public DoubleProperty progressProperty() {
		return progressBar.progressProperty();
	}

	public StringProperty messageProperty() {
		return statusMessage.textProperty();
	}
}
