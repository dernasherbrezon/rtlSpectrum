package ru.r2cloud.rtlspectrum;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Legend extends HBox {

	private final Rectangle rectangle = new Rectangle();
	private final Label label = new Label();

	public Legend() {
		rectangle.setHeight(10.0);
		rectangle.setWidth(10.0);
		
		label.setStyle("-fx-padding: 0; -fx-text-fill: black;");

		BorderPane pane = new BorderPane();
		pane.setCenter(rectangle);
		getChildren().add(pane);
		getChildren().add(label);
		setSpacing(5.0);
	}

	public void setStroke(Paint stroke) {
		rectangle.setStroke(stroke);
		rectangle.setFill(stroke);
	}

	public void setText(String text) {
		label.setText(text);
	}
}
