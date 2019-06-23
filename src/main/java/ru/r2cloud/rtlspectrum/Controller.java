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
import javafx.scene.control.MenuBar;
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
	@FXML
	private MenuItem addFileMenu;
	@FXML
	private MenuItem subtractFileMenu;
	@FXML
	private MenuBar menuBar;

	private List<List<BinData>> rawData = new ArrayList<>();

	private ExecutorService executorService;
	private RunRtlPower rtlPowerTask;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		executorService = Executors.newFixedThreadPool(2);
		((NumberAxis) lineChart.getXAxis()).setTickLabelFormatter(new FrequencyFormatter());
		((NumberAxis) lineChart.getYAxis()).setTickLabelFormatter(new PowerFormatter());
		if (System.getProperty("testfx.running") != null) {
			System.out.println("system menu bar is off");
			menuBar.setUseSystemMenuBar(false);
		} else {
			System.out.println("system menu bar is on");
		}
	}

	@FXML
	public void runNow() {
		RtlPowerProgress progressTask = new RtlPowerProgress(statusBarController);
		rtlPowerTask = new RunRtlPower(progressTask);
		rtlPowerTask.setOnRunning(succeesesEvent -> statusBarController.beginTask("null"));

		disableButtons(true);

		rtlPowerTask.setOnSucceeded(succeededEvent -> {
			List<BinData> result = rtlPowerTask.getValue();
			statusBarController.completeTask();
			welcomeMessage.setVisible(false);
			disableButtons(false);
			setupChart(result, false);
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
		loadFile(false);
	}

	@FXML
	public void addFile() {
		loadFile(true);
	}

	@FXML
	public void save() {
		if (lineChart.getNoData() || rawData.isEmpty()) {
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

		SaveTask saveTask = new SaveTask(statusBarController, selectedFile, rawData.get(0));
		saveTask.setOnRunning(succeesesEvent -> statusBarController.beginTask(selectedFile.getAbsolutePath()));
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

	@FXML
	public void clearChart() {
		lineChart.getData().clear();
	}

	@FXML
	public void subtractFile() {
		System.out.println("subtracting load");
		File selectedFile = requestFileForOpen();
		if (selectedFile == null) {
			System.out.println("file is null");
			return;
		}

		disableButtons(true);

		SubtractFile task = new SubtractFile(statusBarController, selectedFile, rawData);
		task.setOnRunning(succeesesEvent -> statusBarController.beginTask(selectedFile.getAbsolutePath()));
		task.setOnSucceeded(succeededEvent -> {
			List<List<BinData>> result = task.getValue();
			statusBarController.completeTask();
			disableButtons(false);

			rawData = result;
			lineChart.getData().clear();
			for (List<BinData> curGraph : result) {
				XYChart.Series<Number, Number> series = new XYChart.Series<>();
				for (BinData cur : curGraph) {
					series.getData().add(cur.getParsed());
				}
				lineChart.getData().add(series);
			}
			lineChart.setVisible(true);
		});
		task.setOnFailed(workerStateEvent -> {
			disableButtons(false);
			statusBarController.completeTask("Error: " + task.getException().getMessage());
		});

		executorService.execute(task);
	}

	private void loadFile(boolean append) {
		File selectedFile = requestFileForOpen();
		if (selectedFile == null) {
			return;
		}

		disableButtons(true);

		LoadFile readTask = new LoadFile(statusBarController, selectedFile);
		readTask.setOnRunning(succeesesEvent -> statusBarController.beginTask(selectedFile.getAbsolutePath()));
		readTask.setOnSucceeded(succeededEvent -> {
			List<BinData> result = readTask.getValue();
			statusBarController.completeTask();
			welcomeMessage.setVisible(false);
			disableButtons(false);
			setupChart(result, append);
		});
		readTask.setOnFailed(workerStateEvent -> {
			disableButtons(false);
			statusBarController.completeTask("Error: " + readTask.getException().getMessage());
		});

		executorService.execute(readTask);
	}

	private File requestFileForOpen() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open file");
		String defaultDirectory = System.getProperty("rtlSpectrum.defaultdirectory");
		if (defaultDirectory != null) {
			fileChooser.setInitialDirectory(new File(defaultDirectory));
		}
		fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV files", "*.csv"));
		return fileChooser.showOpenDialog(welcomeMessage.getScene().getWindow());
	}

	private void setupChart(List<BinData> data, boolean append) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		for (BinData cur : data) {
			series.getData().add(cur.getParsed());
		}
		if (!append) {
			lineChart.getData().clear();
			rawData = new ArrayList<>();
		}
		rawData.add(data);
		lineChart.getData().add(series);
		lineChart.setVisible(true);
	}

	private void disableButtons(boolean value) {
		runNowButton.setDisable(value);
		loadFileButton.setDisable(value);
		loadFileMenu.setDisable(value);
		runNowMenu.setDisable(value);
		addFileMenu.setDisable(value);
		subtractFileMenu.setDisable(value);
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
