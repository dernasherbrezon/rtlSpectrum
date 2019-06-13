package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javafx.scene.chart.XYChart;

public class RunRtlPower extends StatusBarTask<List<XYChart.Data<Number, Number>>> {

	private static final Pattern SPACE = Pattern.compile("\\s");

	private Process process;

	public RunRtlPower(StatusBar statusBar) {
		super(statusBar);
	}

	@Override
	protected List<XYChart.Data<Number, Number>> call() throws Exception {
		long minimum = 24_000_000;
		long maximum = 1_700_000_000;
		long step = 1_000_000;
		ProcessBuilder processBuilder = new ProcessBuilder(SPACE.split("/usr/local/opt/coreutils/libexec/gnubin/stdbuf -i 0 -o 0 -e 0 rtl_power -f " + minimum + ":" + maximum + ":" + step + " -g 1 -1 -"));
		process = processBuilder.start();
		String curLine = null;
		ArrayList<XYChart.Data<Number, Number>> result = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.ISO_8859_1))) {
			while ((curLine = r.readLine()) != null && !Thread.currentThread().isInterrupted()) {
				if (isCancelled()) {
					break;
				}
				System.out.println(curLine);
				// FIXME
			}
		}
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.ISO_8859_1))) {
			while ((curLine = r.readLine()) != null && !Thread.currentThread().isInterrupted()) {
				if (curLine.equalsIgnoreCase("No supported devices found.")) {
					throw new IllegalStateException(curLine);
				}
				if (curLine.startsWith("usb_claim_interface")) {
					throw new IllegalStateException(curLine);
				}
				if (curLine.startsWith("stdbuf:")) {
					throw new IllegalStateException(curLine);
				}
				if (isCancelled()) {
					break;
				}
				System.out.println(curLine);
			}
		}
		return result;
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		if (process != null) {
			process.destroyForcibly();
		}
	}

}
