package ru.r2cloud.rtlspectrum;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller implements Initializable {

	@FXML
	private LineChart<Number, Number> lineChart;

	@FXML
	private VBox welcomeMessage;

	@FXML
	private HBox statusBar;
	@FXML
	private StatusBar statusBarController;

	private ExecutorService executorService;
	private RunRtlPower rtlPowerTask;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		executorService = Executors.newFixedThreadPool(1);
		((NumberAxis) lineChart.getXAxis()).setTickLabelFormatter(new FrequencyFormatter());
		((NumberAxis) lineChart.getYAxis()).setTickLabelFormatter(new PowerFormatter());
	}

	@FXML
	public void runNow() {
		rtlPowerTask = new RunRtlPower(statusBarController);
		rtlPowerTask.setOnRunning((succeesesEvent) -> {
			statusBarController.beginTask("Running rtl_power");
		});

		rtlPowerTask.setOnSucceeded((succeededEvent) -> {
			List<XYChart.Data<Number, Number>> result = rtlPowerTask.getValue();
			statusBarController.completeTask();
			welcomeMessage.setVisible(false);
			setupChart(result);
		});
		rtlPowerTask.setOnFailed((workerStateEvent) -> {
			statusBarController.completeTask(rtlPowerTask.getException().getMessage());
		});

		executorService.execute(rtlPowerTask);
	}

	@FXML
	public void loadFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open file");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV files", "*.csv"));
		File selectedFile = fileChooser.showOpenDialog(welcomeMessage.getScene().getWindow());
		if (selectedFile == null) {
			return;
		}

		ReadFromFile readTask = new ReadFromFile(statusBarController, selectedFile);
		readTask.setOnRunning((succeesesEvent) -> {
			statusBarController.beginTask("Reading file: " + selectedFile.getAbsolutePath());
		});
		readTask.setOnSucceeded((succeededEvent) -> {
			List<XYChart.Data<Number, Number>> result = readTask.getValue();
			statusBarController.completeTask();
			welcomeMessage.setVisible(false);
			setupChart(result);
		});
		readTask.setOnFailed((workerStateEvent) -> {
			statusBarController.completeTask(readTask.getException().getMessage());
		});

		executorService.execute(readTask);
	}

	private void setupChart(List<XYChart.Data<Number, Number>> data) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		// series.setName("Spectrum");
		for (XYChart.Data<Number, Number> cur : data) {
			series.getData().add(cur);
		}
		lineChart.setLegendVisible(false);
		lineChart.setAnimated(false);
		lineChart.setCreateSymbols(false);
		lineChart.getData().add(series);
		lineChart.setVisible(true);
	}

	public void stop() {
		if (executorService != null) {
			executorService.shutdownNow();
			// explicitly cancel this task as rtl_power is on native code and interruptions do not work there
			if (rtlPowerTask != null) {
				rtlPowerTask.cancel();
			}
		}
	}
}
