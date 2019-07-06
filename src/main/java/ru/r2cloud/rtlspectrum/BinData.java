package ru.r2cloud.rtlspectrum;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.XYChart;

public class BinData {

	private String date;
	private String time;
	private String frequencyStart;
	private String frequencyEnd;
	private String binSize;
	private String numberOfSamples;
	private List<String> dbm;
	private XYChart.Data<Number, Number> parsed;
	private double dbmAverage;

	public BinData() {
		// do nothing
	}

	public BinData(BinData copy) {
		this.date = copy.date;
		this.time = copy.time;
		this.frequencyStart = copy.frequencyStart;
		this.frequencyEnd = copy.frequencyEnd;
		this.binSize = copy.binSize;
		this.numberOfSamples = copy.numberOfSamples;
		this.dbm = new ArrayList<>(copy.dbm);
		this.parsed = copy.parsed;
		this.dbmAverage = copy.dbmAverage;
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

	public List<String> getDbm() {
		return dbm;
	}

	public void setDbm(List<String> dbm) {
		this.dbm = dbm;
	}
	
	public void setDbmAverage(double dbmAverage) {
		this.dbmAverage = dbmAverage;
	}
	
	public double getDbmAverage() {
		return dbmAverage;
	}

}
