package ru.r2cloud.rtlspectrum;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.util.StringConverter;

public class PowerFormatter extends StringConverter<Number> {

	private final NumberFormat format;

	public PowerFormatter() {
		format = new DecimalFormat("#.##");
	}

	@Override
	public String toString(Number object) {
		if (object == null) {
			return "";
		}
		return format.format(object.doubleValue());
	}

	@Override
	public Number fromString(String string) {
		return null;
	}
}
