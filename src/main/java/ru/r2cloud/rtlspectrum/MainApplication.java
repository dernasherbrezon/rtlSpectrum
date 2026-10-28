package ru.r2cloud.rtlspectrum;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

	private Controller controller;

	@Override
	public void start(Stage stage) {
		Parent root;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("layout.fxml")) {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("layout.fxml"));
			root = fxmlLoader.load(is);
			controller = fxmlLoader.getController();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		Scene scene = new Scene(root, 640, 480);
		String osName = System.getProperty("os.name");
		if (osName != null && osName.toLowerCase(Locale.UK).contains("mac") && MacOsUtil.isDark("/usr/bin/defaults")) {
			scene.getStylesheets().add("dark.css");
		}
		stage.setScene(scene);
		stage.setTitle("rtlSpectrum");
		stage.show();
		applyCliArgs();
	}

	@Override
	public void stop() throws Exception {
		if (controller != null) {
			controller.stop();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void applyCliArgs() {
		List<String> raw = getParameters().getRaw();
		CliArgs.Parsed parsed = CliArgs.parse(raw.toArray(new String[0]));
		if (parsed.isHelpRequested()) {
			System.out.println(CliArgs.usage());
			Platform.exit();
			return;
		}
		if (parsed.hasError()) {
			System.err.println(parsed.getError());
			Platform.exit();
			return;
		}
		if (parsed.getBaseFile() == null) {
			return;
		}
		File base = parsed.getBaseFile();
		if (!parsed.getOperations().isEmpty()) {
			controller.loadFileDirect(base);
			for (CliArgs.Operation op : parsed.getOperations()) {
				if (op.getType() == CliArgs.OpType.ADD) {
					controller.addFileDirect(op.getFile());
				} else {
					controller.subtractFileDirect(op.getFile());
				}
			}
		} else {
			controller.loadFileDirect(base);
		}
	}

}
