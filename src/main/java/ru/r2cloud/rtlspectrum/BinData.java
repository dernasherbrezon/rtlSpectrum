package ru.r2cloud.rtlspectrum;

import javafx.scene.chart.XYChart;

public class BinData {

	private String date;
	private String time;
	private String frequencyStart;
	private String frequencyEnd;
	private String binSize;
	private String numberOfSamples;
	private String dbmStart;
	private String dbmEnd;
	
	private XYChart.Data<Number, Number> parsed;
	
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

	public String getDbmStart() {
		return dbmStart;
	}

	public void setDbmStart(String dbmStart) {
		this.dbmStart = dbmStart;
	}

	public String getDbmEnd() {
		return dbmEnd;
	}

	public void setDbmEnd(String dbmEnd) {
		this.dbmEnd = dbmEnd;
	}

}
