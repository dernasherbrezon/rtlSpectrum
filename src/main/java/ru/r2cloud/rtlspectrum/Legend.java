package ru.r2cloud.rtlspectrum;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Legend extends HBox {

	private final Rectangle rectangle = new Rectangle();
	private final Label label = new Label("test");

	public Legend() {
		BorderPane p = new BorderPane();
		rectangle.setHeight(10.0);
		rectangle.setWidth(10.0);
		p.setCenter(rectangle);
		label.setStyle("-fx-padding: 0; -fx-text-fill: black;");
		getChildren().add(p);
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
