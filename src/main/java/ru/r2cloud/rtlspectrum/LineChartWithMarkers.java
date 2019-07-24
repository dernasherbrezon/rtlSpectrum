package ru.r2cloud.rtlspectrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.StringConverter;

public class LineChartWithMarkers extends LineChart<Number, Number> {

	private static final int CURSOR_X_MARGIN = 12;
	private static final int CURSOR_Y_MARGIN = 20;

	private Line line = new Line();
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
					if (yValue == null || Double.isNaN(yValue.doubleValue())) {
						legend.setVisible(false);
						continue;
					}
					legend.setVisible(true);
					if (!legend.hasStroke()) {
						legend.setStroke(((Path) series.getNode().lookup(".chart-series-line")).getStroke());
					}
					if (yConverter != null) {
						legend.setText(yConverter.toString(yValue));
					} else {
						legend.setText(yValue.toString());
					}
				}
			}

		});

		line.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				chartBackground2.getOnMouseMoved().handle(mouseEvent);
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
	protected void layoutPlotChildren() {
		List<PathElement> constructedPath = new ArrayList<>(getData().size());
		for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
			Series<Number, Number> series = getData().get(seriesIndex);
			if (series.getNode() instanceof Path) {
				ObservableList<PathElement> seriesLine = ((Path) series.getNode()).getElements();
				seriesLine.clear();
				constructedPath.clear();
				MoveTo nextMoveTo = null;
				for (Iterator<Data<Number, Number>> it = getDisplayedDataIterator(series); it.hasNext();) {
					Data<Number, Number> item = it.next();
					double x = getXAxis().getDisplayPosition(item.getXValue());
					double y = getYAxis().getDisplayPosition(getYAxis().toRealValue(getYAxis().toNumericValue(item.getYValue())));
					if (Double.isNaN(x) || Double.isNaN(y)) {
						int index = series.getData().indexOf(item);
						if (index < series.getData().size() - 1) {
							Data<Number, Number> next = series.getData().get(index + 1);
							double nextX = getXAxis().getDisplayPosition(next.getXValue());
							double nextY = getYAxis().getDisplayPosition(getYAxis().toRealValue(getYAxis().toNumericValue(next.getYValue())));
							nextMoveTo = new MoveTo(nextX, nextY);
						}
					} else {
						if (nextMoveTo != null) {
							constructedPath.add(nextMoveTo);
							nextMoveTo = null;
						}
						constructedPath.add(new LineTo(x, y));
						Node symbol = item.getNode();
						if (symbol != null) {
							double w = symbol.prefWidth(-1);
							double h = symbol.prefHeight(-1);
							symbol.resizeRelocate(x - (w / 2), y - (h / 2), w, h);
						}
					}
				}

				if (!constructedPath.isEmpty()) {
					PathElement first = constructedPath.get(0);
					seriesLine.add(new MoveTo(getX(first), getY(first)));
					seriesLine.addAll(constructedPath);
				}
			}
		}

	}

	private static double getX(PathElement element) {
		if (element instanceof LineTo) {
			return ((LineTo) element).getX();
		} else if (element instanceof MoveTo) {
			return ((MoveTo) element).getX();
		} else {
			throw new IllegalArgumentException(element + " is not a valid type");
		}
	}

	private static double getY(PathElement element) {
		if (element instanceof LineTo) {
			return ((LineTo) element).getY();
		} else if (element instanceof MoveTo) {
			return ((MoveTo) element).getY();
		} else {
			throw new IllegalArgumentException(element + " is not a valid type");
		}
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
		final double legendWidth = boundedSize(snapSize(tooltip.prefWidth(legendHeight)), 0, width - left - right);
		tooltip.resizeRelocate(snapPosition(left), snapPosition(top), legendWidth, legendHeight);
	}

	private static double boundedSize(double value, double min, double max) {
		// if max < value, return max
		// if min > value, return min
		// if min > max, return min
		return Math.min(Math.max(value, min), Math.max(min, max));
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

	@Override
	protected void updateAxisRange() {
		final Axis<Number> xa = getXAxis();
		final Axis<Number> ya = getYAxis();
		List<Number> xData = null;
		List<Number> yData = null;
		if (xa.isAutoRanging())
			xData = new ArrayList<Number>();
		if (ya.isAutoRanging())
			yData = new ArrayList<Number>();
		if (xData != null || yData != null) {
			for (Series<Number, Number> series : getData()) {
				for (Data<Number, Number> data : series.getData()) {
					if (xData != null && !Double.isNaN(data.getXValue().doubleValue()))
						xData.add(data.getXValue());
					if (yData != null && !Double.isNaN(data.getYValue().doubleValue()))
						yData.add(data.getYValue());
				}
			}
			// RT-32838 No need to invalidate range if there is one data item - whose value is zero.
			if (xData != null && !(xData.size() == 1 && getXAxis().toNumericValue(xData.get(0)) == 0)) {
				xa.invalidateRange(xData);
			}
			if (yData != null && !(yData.size() == 1 && getYAxis().toNumericValue(yData.get(0)) == 0)) {
				ya.invalidateRange(yData);
			}

		}
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
