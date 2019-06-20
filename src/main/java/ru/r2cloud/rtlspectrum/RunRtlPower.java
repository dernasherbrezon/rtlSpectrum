package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javafx.concurrent.Task;

public class RunRtlPower extends Task<List<BinData>> {

	static final long MINIMUM_FREQ = 24_000_000;
	static final long MAXIMUM_FREQ = 1_700_000_000;
	static final long STEP = 1_000_000;
	static final long NUMBER_OF_SECONDS = TimeUnit.MINUTES.toSeconds(2);

	private static final Pattern SPACE = Pattern.compile("\\s");

	private Process process;
	private final RtlPowerProgress progressTask;

	public RunRtlPower(RtlPowerProgress progressTask) {
		this.progressTask = progressTask;
	}

	@Override
	protected List<BinData> call() throws Exception {
		updateMessage("Running rtl_power");
		ProcessBuilder processBuilder = new ProcessBuilder(SPACE.split("rtl_power -f " + MINIMUM_FREQ + ":" + MAXIMUM_FREQ + ":" + STEP + " -i " + NUMBER_OF_SECONDS + " -g 45 -1 -"));
		process = processBuilder.start();
		String curLine = null;
		List<BinData> result = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.ISO_8859_1))) {
			while ((curLine = r.readLine()) != null && !Thread.currentThread().isInterrupted()) {
				if (isCancelled()) {
					break;
				}
				BinData cur = ReadFromFile.convert(curLine);
				result.add(cur);
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
			}
		}
		progressTask.cancel(true);
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
