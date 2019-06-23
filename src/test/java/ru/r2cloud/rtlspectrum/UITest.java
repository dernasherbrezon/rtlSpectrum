package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.robot.Motion;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class UITest extends ApplicationTest {

	private static final long TIMEOUT = 100;
	private static final int MAX_RETRIES = 60;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void loadFile() throws Exception {
		File subtractData = new File(tempFolder.getRoot(), "2_" + UUID.randomUUID().toString() + ".csv");
		try (FileOutputStream fos = new FileOutputStream(subtractData); InputStream is = UITest.class.getClassLoader().getResourceAsStream("subtract.csv")) {
			copy(is, fos);
		}
		File testData = new File(tempFolder.getRoot(), "1_" + UUID.randomUUID().toString() + ".csv");
		try (FileOutputStream fos = new FileOutputStream(testData); InputStream is = UITest.class.getClassLoader().getResourceAsStream("test.csv")) {
			copy(is, fos);
		}
		File[] files = tempFolder.getRoot().listFiles();
		for( File cur : files ) {
			System.out.println(cur.getAbsolutePath());
		}
		clickOn("#loadFileButton");
		push(KeyCode.DIGIT1, KeyCode.ENTER);
		waitForCompletion(testData.getCanonicalPath());

		// verify load file
		List<XYChart.Data<Number, Number>> file1Data = expectedDataFromFile1();
		assertData(file1Data);

		clickOn("#editMenu");
		clickOn("#subtractFileMenu", Motion.VERTICAL_FIRST);
		push(KeyCode.DIGIT2, KeyCode.ENTER);
		waitForCompletion(subtractData.getCanonicalPath());

		// verify subtract file
		List<XYChart.Data<Number, Number>> expected = new ArrayList<>();
		expected.add(new Data<Number, Number>(24000000L, -2.0));
		expected.add(new Data<Number, Number>(25000000L, -2.0));
		expected.add(new Data<Number, Number>(26000000L, 2.0));
		assertData(expected);

		clickOn("#fileMenu");
		clickOn("#addFileMenu", Motion.VERTICAL_FIRST);
		push(KeyCode.DIGIT1, KeyCode.ENTER);
		waitForCompletion(testData.getCanonicalPath());
		// verify add file
		assertData(expected, file1Data);
	}

	private static List<XYChart.Data<Number, Number>> expectedDataFromFile1() {
		List<XYChart.Data<Number, Number>> expected = new ArrayList<>();
		expected.add(new Data<Number, Number>(24000000L, -24.14));
		expected.add(new Data<Number, Number>(25000000L, -24.15));
		expected.add(new Data<Number, Number>(26000000L, 14.07));
		return expected;
	}

	@Override
	public void start(Stage stage) {
		System.setProperty("rtlSpectrum.defaultdirectory", tempFolder.getRoot().getAbsolutePath());
		System.setProperty("testfx.running", "true");
		Parent root;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("layout.fxml")) {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("layout.fxml"));
			root = fxmlLoader.load(is);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	@SafeVarargs
	private final void assertData(List<XYChart.Data<Number, Number>>... data) {
		LineChart<Number, Number> chart = lookup("#lineChart").query();
		assertEquals(data.length, chart.getData().size());
		for (int j = 0; j < data.length; j++) {
			ObservableList<Data<Number, Number>> curSeries = chart.getData().get(j).getData();
			List<XYChart.Data<Number, Number>> curExpected = data[j];
			assertEquals(curExpected.size(), curSeries.size());
			for (int i = 0; i < curExpected.size(); i++) {
				Data<Number, Number> expected = curExpected.get(i);
				Data<Number, Number> actual = curSeries.get(i);
				assertEquals(expected.getXValue(), actual.getXValue());
				assertEquals(expected.getYValue(), actual.getYValue());
			}
		}
	}

	private void waitForCompletion(String expectedTaskId) {
		ProgressBar bar = lookup("#progressBar").query();
		int curRetry = 0;
		while (curRetry < MAX_RETRIES && !Thread.currentThread().isInterrupted()) {
			String taskId = (String) bar.getProperties().get(StatusBar.LAST_COMPLETED_TASK);
			if (taskId != null && taskId.equals(expectedTaskId)) {
				return;
			}
			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			curRetry++;
		}
		throw new RuntimeException("task did not complete in time: " + expectedTaskId);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
	}
}
