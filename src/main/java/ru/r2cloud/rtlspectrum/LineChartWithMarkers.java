package ru.r2cloud.rtlspectrum;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;

public class LineChartWithMarkers extends LineChart<Number, Number> {

	private static final int CURSOR_X_MARGIN = 12;
	private static final int CURSOR_Y_MARGIN = 20;

	private static final int TEXT_PADDING = 5;

	private Line line = new Line();
	private Rectangle label = new Rectangle();
	private Text text = new Text();
	private StringConverter<Number> xConverter;
	private StringConverter<Number> yConverter;

	private final BooleanProperty noData = new SimpleBooleanProperty(this, "noData", true);

	public LineChartWithMarkers(@NamedArg("xAxis") Axis<Number> xAxis, @NamedArg("yAxis") Axis<Number> yAxis) {
		super(xAxis, yAxis);
		line.setVisible(false);
		line.setStroke(Color.GREY);
		line.getStrokeDashArray().add(4d);
		label.setVisible(false);
		label.setFill(Color.LIGHTGRAY);

		if (xAxis instanceof NumberAxis) {
			NumberAxis numberedXAxis = (NumberAxis) xAxis;
			xConverter = numberedXAxis.getTickLabelFormatter();
			numberedXAxis.tickLabelFormatterProperty().addListener(new ChangeListener<StringConverter<Number>>() {
				@Override
				public void changed(ObservableValue<? extends StringConverter<Number>> observable, StringConverter<Number> oldValue, StringConverter<Number> newValue) {
					xConverter = newValue;
				}
			});
		}
		if (yAxis instanceof NumberAxis) {
			NumberAxis numberedYAxis = (NumberAxis) yAxis;
			yConverter = numberedYAxis.getTickLabelFormatter();
			numberedYAxis.tickLabelFormatterProperty().addListener(new ChangeListener<StringConverter<Number>>() {
				@Override
				public void changed(ObservableValue<? extends StringConverter<Number>> observable, StringConverter<Number> oldValue, StringConverter<Number> newValue) {
					yConverter = newValue;
				}
			});
		}
		text.setTextAlignment(TextAlignment.JUSTIFY);

		final Node chartBackground2 = lookup(".chart-plot-background");
		final Node chartBackground = lookup(".chart-content");
		chartBackground.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				line.setVisible(true);
				label.setVisible(true);
			}
		});

		chartBackground.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				Point2D point = chartBackground2.parentToLocal(mouseEvent.getX(), mouseEvent.getY());
				line.setStartX(point.getX());
				line.setEndX(line.getStartX());
				line.setStartY(0.0);
				line.setEndY(getBoundsInLocal().getHeight());

				text.setFont(getXAxis().getTickLabelFont());
				text.setX(CURSOR_X_MARGIN + point.getX() + TEXT_PADDING);
				text.setY(CURSOR_Y_MARGIN + point.getY() + 3 * TEXT_PADDING);
				text.setText(getLabel(point));

				label.setX(CURSOR_X_MARGIN + point.getX());
				label.setY(CURSOR_Y_MARGIN + point.getY());
				label.setHeight(text.getBoundsInLocal().getHeight() + 2 * TEXT_PADDING);
				label.setWidth(text.getBoundsInLocal().getWidth() + 2 * TEXT_PADDING);

				if (label.getY() + label.getHeight() > chartBackground2.getBoundsInLocal().getMaxY()) {
					label.setY(point.getY() - label.getHeight());
					text.setY(point.getY() - text.getBoundsInLocal().getHeight() + TEXT_PADDING);
				}

				if (label.getX() + label.getWidth() > chartBackground2.getBoundsInLocal().getMaxX()) {
					label.setX(point.getX() - label.getWidth() - CURSOR_X_MARGIN);
					text.setX(point.getX() - text.getBoundsInLocal().getWidth() - CURSOR_X_MARGIN - TEXT_PADDING);
				}

			}

		});

		getPlotChildren().add(line);
		getPlotChildren().add(label);
		getPlotChildren().add(text);

		getData().addListener(new ListChangeListener<Series<Number, Number>>() {
			@Override
			public void onChanged(Change<? extends Series<Number, Number>> c) {
				noData.set(c.getList().isEmpty());
			}
		});
	}

	@Override
	protected void layoutPlotChildren() {
		super.layoutPlotChildren();
		label.toFront();
		text.toFront();
	}

	private Number getNearestYValue(Number value) {
		if (getData().isEmpty()) {
			return null;
		}
		// use only first series
		Series<Number, Number> series = getData().get(0);
		if (series.getData().isEmpty()) {
			return null;
		}
		Data<Number, Number> previous = null;
		for (Data<Number, Number> cur : series.getData()) {
			double curX = cur.getXValue().doubleValue();
			if (curX < value.doubleValue()) {
				previous = cur;
				continue;
			}
			if (curX == value.doubleValue()) {
				return cur.getYValue();
			}

			if (previous == null) {
				return null;
			}
			// interpolate linearly
			return previous.getYValue().doubleValue() + ((value.doubleValue() - previous.getXValue().doubleValue()) / (cur.getXValue().doubleValue() - previous.getXValue().doubleValue())) * (cur.getYValue().doubleValue() - previous.getYValue().doubleValue());
		}

		return null;
	}

	private String getLabel(Point2D point) {
		Number xValue = getXAxis().getValueForDisplay(point.getX());
		Number yValue = getNearestYValue(xValue);

		String textValue = "";
		if (xConverter != null) {
			textValue += xConverter.toString(xValue);
		} else {
			textValue = xValue.toString();
		}
		if (yValue != null) {
			textValue += "\n";
			if (yConverter != null) {
				textValue += yConverter.toString(yValue);
			} else {
				textValue += yValue.toString();
			}
		}
		return textValue.trim();
	}

	public BooleanProperty noDataProperty() {
		return noData;
	}

	public boolean getNoData() {
		return noData.get();
	}

	public void setNoData(boolean value) {
		noData.set(value);
	}
}
