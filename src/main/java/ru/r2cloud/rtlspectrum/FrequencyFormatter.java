package ru.r2cloud.rtlspectrum;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.util.StringConverter;

public class FrequencyFormatter extends StringConverter<Number> {

	private static final long ONE_KILOHERZ = 1_000;
	private static final long ONE_MEGAHERZ = 1_000_000;
	private static final long ONE_GIGAHERZ = 1_000_000_000;

	private final NumberFormat format;

	public FrequencyFormatter() {
		format = new DecimalFormat("#.#");
	}

	@Override
	public String toString(Number object) {
		if (object == null) {
			return "";
		}
		if (object.longValue() < 0) {
			return "";
		}
		if (object.longValue() < ONE_KILOHERZ) {
			return Long.valueOf(object.longValue()) + " hz";
		}
		if (object.longValue() < ONE_MEGAHERZ) {
			return format.format(object.doubleValue() / ONE_KILOHERZ) + " Khz";
		}
		if (object.longValue() < ONE_GIGAHERZ) {
			return format.format(object.doubleValue() / ONE_MEGAHERZ) + " Mhz";
		}
		return format.format(object.doubleValue() / ONE_GIGAHERZ) + " Ghz";
	}

	@Override
	public Number fromString(String string) {
		return null;
	}

}
