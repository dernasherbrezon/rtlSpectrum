package ru.r2cloud.rtlspectrum;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller implements Initializable {

	@FXML
	private LineChartWithMarkers lineChart;

	@FXML
	private VBox welcomeMessage;

	@FXML
	private HBox statusBar;
	@FXML
	private StatusBar statusBarController;

	@FXML
	private Button runNowButton;
	@FXML
	private Button loadFileButton;
	@FXML
	private MenuItem loadFileMenu;
	@FXML
	private MenuItem runNowMenu;

	private List<BinData> rawData = new ArrayList<>();

	private ExecutorService executorService;
	private RunRtlPower rtlPowerTask;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		executorService = Executors.newFixedThreadPool(2);
		((NumberAxis) lineChart.getXAxis()).setTickLabelFormatter(new FrequencyFormatter());
		((NumberAxis) lineChart.getYAxis()).setTickLabelFormatter(new PowerFormatter());
	}

	@FXML
	public void runNow() {
		RtlPowerProgress progressTask = new RtlPowerProgress(statusBarController);
		rtlPowerTask = new RunRtlPower(progressTask);
		rtlPowerTask.setOnRunning(succeesesEvent -> statusBarController.beginTask());

		disableButtons(true);

		rtlPowerTask.setOnSucceeded(succeededEvent -> {
			List<BinData> result = rtlPowerTask.getValue();
			statusBarController.completeTask();
			welcomeMessage.setVisible(false);
			disableButtons(false);
			setupChart(result);
		});
		rtlPowerTask.setOnFailed(workerStateEvent -> {
			progressTask.cancel(true);
			statusBarController.completeTask("Error: " + rtlPowerTask.getException().getMessage());
			disableButtons(false);
		});

		executorService.execute(rtlPowerTask);
		executorService.execute(progressTask);
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

		disableButtons(true);

		ReadFromFile readTask = new ReadFromFile(statusBarController, selectedFile);
		readTask.setOnRunning(succeesesEvent -> statusBarController.beginTask());
		readTask.setOnSucceeded(succeededEvent -> {
			List<BinData> result = readTask.getValue();
			statusBarController.completeTask();
			welcomeMessage.setVisible(false);
			disableButtons(false);
			setupChart(result);
		});
		readTask.setOnFailed(workerStateEvent -> {
			disableButtons(false);
			statusBarController.completeTask("Error: " + readTask.getException().getMessage());
		});

		executorService.execute(readTask);
	}

	@FXML
	public void save() {
		if (lineChart.getNoData()) {
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save file");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV files", "*.csv"));
		File selectedFile = fileChooser.showSaveDialog(welcomeMessage.getScene().getWindow());
		if (selectedFile == null) {
			return;
		}

		disableButtons(true);

		SaveTask saveTask = new SaveTask(statusBarController, selectedFile, rawData);
		saveTask.setOnRunning(succeesesEvent -> statusBarController.beginTask());
		saveTask.setOnSucceeded(succeededEvent -> {
			disableButtons(false);
			statusBarController.completeTask();
		});
		saveTask.setOnFailed(workerStateEvent -> {
			disableButtons(false);
			statusBarController.completeTask("Error: " + saveTask.getException().getMessage());
		});

		executorService.execute(saveTask);
	}

	private void setupChart(List<BinData> data) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		for (BinData cur : data) {
			series.getData().add(cur.getParsed());
		}
		lineChart.getData().clear();
		lineChart.getData().add(series);
		lineChart.setVisible(true);
		rawData = data;
	}

	private void disableButtons(boolean value) {
		runNowButton.setDisable(value);
		loadFileButton.setDisable(value);
		loadFileMenu.setDisable(value);
		runNowMenu.setDisable(value);
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
