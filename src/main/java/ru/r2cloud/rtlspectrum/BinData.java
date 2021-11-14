package ru.r2cloud.rtlspectrum;

import javafx.scene.chart.XYChart;

public class BinData {

	private String date;
	private String time;
	private String frequencyStart;
	private long frequencyStartParsed;
	private String frequencyEnd;
	private String binSize;
	private String numberOfSamples;
	private XYChart.Data<Number, Number> parsed;
	private double dbmAverage;

	private double dbmTotal;
	private int dbmCount;

	public BinData() {
		// do nothing
	}

	public BinData(BinData copy) {
		this.date = copy.date;
		this.time = copy.time;
		this.frequencyStart = copy.frequencyStart;
		this.frequencyStartParsed = copy.frequencyStartParsed;
		this.frequencyEnd = copy.frequencyEnd;
		this.binSize = copy.binSize;
		this.numberOfSamples = copy.numberOfSamples;
		this.parsed = copy.parsed;
		this.dbmAverage = copy.dbmAverage;
		this.dbmTotal = copy.dbmTotal;
		this.dbmCount = copy.dbmCount;
	}

	public long getFrequencyStartParsed() {
		return frequencyStartParsed;
	}

	public void setFrequencyStartParsed(long frequencyStartParsed) {
		this.frequencyStartParsed = frequencyStartParsed;
	}

	public int getDbmCount() {
		return dbmCount;
	}

	public void setDbmCount(int dbmCount) {
		this.dbmCount = dbmCount;
	}

	public double getDbmTotal() {
		return dbmTotal;
	}

	public void setDbmTotal(double dbmTotal) {
		this.dbmTotal = dbmTotal;
	}

	public XYChart.Data<Number, Number> getParsed() {
		return parsed;
	}

	public void setParsed(XYChart.Data<Number, Number> parsed) {
		this.parsed = parsed;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getFrequencyStart() {
		return frequencyStart;
	}

	public void setFrequencyStart(String frequencyStart) {
		this.frequencyStart = frequencyStart;
	}

	public String getFrequencyEnd() {
		return frequencyEnd;
	}

	public void setFrequencyEnd(String frequencyEnd) {
		this.frequencyEnd = frequencyEnd;
	}

	public String getBinSize() {
		return binSize;
	}

	public void setBinSize(String binSize) {
		this.binSize = binSize;
	}

	public String getNumberOfSamples() {
		return numberOfSamples;
	}

	public void setNumberOfSamples(String numberOfSamples) {
		this.numberOfSamples = numberOfSamples;
	}

	public void setDbmAverage(double dbmAverage) {
		this.dbmAverage = dbmAverage;
	}

	public double getDbmAverage() {
		return dbmAverage;
	}

}
