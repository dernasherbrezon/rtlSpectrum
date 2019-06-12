package ru.r2cloud.rtlspectrum;

import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	private Controller controller;

	@Override
	public void start(Stage stage) {
		Parent root;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("layout.fxml")) {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("layout.fxml"));
			root = fxmlLoader.load(is);
			controller = fxmlLoader.getController();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.setTitle("rtlSpectrum");
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		if (controller != null) {
			controller.stop();
		}
	}

	public static void main(String[] args) {
		launch();
	}

}
