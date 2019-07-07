package ru.r2cloud.rtlspectrum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sun.javafx.scene.control.skin.Utils;

import javafx.application.Platform;
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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;

public class LineChartWithMarkers extends LineChart<Number, Number> {

	private static final int CURSOR_X_MARGIN = 12;
	private static final int CURSOR_Y_MARGIN = 20;

	private Line line = new Line();
	private Text text = new Text();
	private StringConverter<Number> xConverter;
	private StringConverter<Number> yConverter;

	private final BooleanProperty noData = new SimpleBooleanProperty(this, "noData", true);
	private final Pane tooltip;
	private final Label tooltipXLabel;
	private Map<String, Legend> legendByName = new HashMap<>();

	public LineChartWithMarkers(@NamedArg("xAxis") Axis<Number> xAxis, @NamedArg("yAxis") Axis<Number> yAxis, @NamedArg("tooltip") Pane tooltip) {
		super(xAxis, yAxis);
		this.tooltip = tooltip;
		line.setVisible(false);
		line.setStroke(Color.GREY);
		line.getStrokeDashArray().add(4d);

		tooltipXLabel = (Label) tooltip.lookup("#tooltipXLabel");
		getChildren().add(tooltip);

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

		chartBackground2.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				Point2D point = new Point2D(mouseEvent.getX(), mouseEvent.getY());
				String xLabel = getXLabel(point);
				if (xLabel.length() == 0) {
					return;
				}

				line.setStartX(point.getX());
				line.setEndX(line.getStartX());
				line.setStartY(0.0);
				line.setEndY(getBoundsInLocal().getHeight());
				line.setVisible(true);

				tooltip.setVisible(true);
				tooltipXLabel.setText(xLabel);

				double xWithinPlotArea = CURSOR_X_MARGIN + point.getX();
				double yWithinPlotArea = CURSOR_Y_MARGIN + point.getY();

				double newX = xWithinPlotArea + chartBackground2.getLayoutX();
				double newY = yWithinPlotArea + chartBackground2.getLayoutY();

				if (xWithinPlotArea + tooltip.getWidth() > chartBackground2.getBoundsInLocal().getMaxX()) {
					newX = point.getX() - tooltip.getWidth() - CURSOR_X_MARGIN + chartBackground2.getLayoutX();
				}
				if (yWithinPlotArea + tooltip.getHeight() > chartBackground2.getBoundsInLocal().getMaxY()) {
					newY = point.getY() - tooltip.getHeight();
				}

				tooltip.setTranslateX(newX);
				tooltip.setTranslateY(newY);

				Map<String, Number> yValues = getNearestYValue(getXAxis().getValueForDisplay(point.getX()));
				for (Series<Number, Number> series : getData()) {
					Legend legend = legendByName.get(series.getName());
					if (legend == null) {
						continue;
					}
					Number yValue = yValues.get(series.getName());
					if (yValue == null) {
						legend.setVisible(false);
						continue;
					}
					legend.setVisible(true);
					if (yConverter != null) {
						legend.setText(yConverter.toString(yValue));
					} else {
						legend.setText(yValue.toString());
					}
				}
			}

		});

		getPlotChildren().add(line);

		getData().addListener(new ListChangeListener<Series<Number, Number>>() {
			@Override
			public void onChanged(Change<? extends Series<Number, Number>> c) {
				while (c.next()) {
					for (Series<Number, Number> cur : c.getAddedSubList()) {
						Legend rec = new Legend();
						rec.setVisible(false);
						legendByName.put(cur.getName(), rec);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								rec.setStroke(((Path) cur.getNode().lookup(".chart-series-line")).getStroke());
							}
						});
						tooltip.getChildren().add(rec);
					}
					for (Series<Number, Number> cur : c.getRemoved()) {
						Legend previous = legendByName.remove(cur.getName());
						if (previous == null) {
							continue;
						}
						tooltip.getChildren().remove(previous);
					}
				}
				noData.set(c.getList().isEmpty());
			}
		});
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		final Pane chartContent = (Pane) lookup(".chart-content");
		double top = chartContent.getLayoutY();
		double left = chartContent.getLayoutX();
		double right = snappedRightInset();
		final double width = getWidth();
		// copy paste from legend
		final double legendHeight = snapSize(tooltip.prefHeight(width - left - right));
		final double legendWidth = Utils.boundedSize(snapSize(tooltip.prefWidth(legendHeight)), 0, width - left - right);
		tooltip.resizeRelocate(snapPosition(left), snapPosition(top), legendWidth, legendHeight);
	}

	private Map<String, Number> getNearestYValue(Number xValue) {
		if (getData().isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Number> result = new HashMap<>();
		for (Series<Number, Number> series : getData()) {
			if (series.getData().isEmpty()) {
				continue;
			}
			Number yValue = getNumber(xValue, series);
			if (yValue == null) {
				continue;
			}
			result.put(series.getName(), yValue);
		}

		return result;
	}

	private static Number getNumber(Number xValue, Series<Number, Number> series) {
		Data<Number, Number> previous = null;
		for (Data<Number, Number> cur : series.getData()) {
			double curX = cur.getXValue().doubleValue();
			if (curX < xValue.doubleValue()) {
				previous = cur;
				continue;
			}
			if (curX == xValue.doubleValue()) {
				return cur.getYValue();
			}

			if (previous == null) {
				return null;
			}
			// interpolate linearly
			return previous.getYValue().doubleValue() + ((xValue.doubleValue() - previous.getXValue().doubleValue()) / (cur.getXValue().doubleValue() - previous.getXValue().doubleValue())) * (cur.getYValue().doubleValue() - previous.getYValue().doubleValue());
		}
		return null;
	}

	private String getXLabel(Point2D point) {
		Number xValue = getXAxis().getValueForDisplay(point.getX());
		String textValue = "";
		if (xConverter != null) {
			textValue += xConverter.toString(xValue);
		} else {
			textValue = xValue.toString();
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
