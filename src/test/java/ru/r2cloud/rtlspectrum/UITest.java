package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.loadui.testfx.GuiTest;

import com.google.common.collect.Iterables;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;

public class UITest extends GuiTest {

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
		click("#loadFileButton");
		push(KeyCode.DIGIT1, KeyCode.ENTER);
		waitForCompletion(testData.getCanonicalPath());

		// verify load file
		List<XYChart.Data<Number, Number>> file1Data = expectedDataFromFile1();
		assertData(file1Data);

		
		click("#editMenu");
		click("#subtractFileMenu");
		push(KeyCode.DIGIT2, KeyCode.ENTER);
		waitForCompletion(subtractData.getCanonicalPath());

		// verify subtract file
		List<XYChart.Data<Number, Number>> expected = new ArrayList<>();
		expected.add(new Data<Number, Number>(24000000L, -2.0));
		expected.add(new Data<Number, Number>(25000000L, -2.0));
		expected.add(new Data<Number, Number>(26000000L, 2.0));
		assertData(expected);

		click("#fileMenu");
		click("#addFileMenu");
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
	public void setupStage() throws Throwable {
		super.setupStage();
//		System.setProperty("rtlSpectrum.defaultdirectory", tempFolder.getRoot().getAbsolutePath());
//		System.setProperty("testfx.running", "true");
//		Parent root;
//		try (InputStream is = getClass().getClassLoader().getResourceAsStream("layout.fxml")) {
//			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("layout.fxml"));
//			root = fxmlLoader.load(is);
//		} catch (IOException e) {
//			throw new IllegalStateException(e);
//		}
		
		double width = 640;
	    double height = 480;

	    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	    stage.setX((screenBounds.getWidth() - width) / 2); 
	    stage.setY((screenBounds.getHeight() - height) / 2);
	    
//		Scene scene = new Scene(root, 640, 480);
//		stage.setScene(scene);
//		stage.show();
		
//		stage.
	}
	
	@Override
	protected Parent getRootNode() {
		System.setProperty("rtlSpectrum.defaultdirectory", tempFolder.getRoot().getAbsolutePath());
		System.setProperty("testfx.running", "true");
		Parent root;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("layout.fxml")) {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("layout.fxml"));
			root = fxmlLoader.load(is);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
//		Scene scene = new Scene(root, 640, 480);
//		stage.setScene(scene);
//		stage.show();

		return root;
	}

	@SafeVarargs
	private final void assertData(List<XYChart.Data<Number, Number>>... data) {
		LineChart<Number, Number> chart = findAny("#lineChart");//.query();
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
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ProgressBar bar;
		try {
		bar = findAny("#progressBar");//.query();
		} catch( Exception e ) {
			return;
		}
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
	
	private <T extends Node> T findAny(String selector) {
		Set<Node> result = findAll(selector);
		return (T)Iterables.getFirst(result, null);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
	}
}
