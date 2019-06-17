package ru.r2cloud.rtlspectrum;

public class RtlPowerProgress extends StatusBarTask<Void> {

	public RtlPowerProgress(StatusBar statusBar) {
		super(statusBar);
	}

	@Override
	protected Void call() throws Exception {
		// taken from the reference rtl-sdr mac book air 2013
		long averageNumberOfSecondsPerFullSpectrum = 121;
		long numberOfSeconds = ((long) Math.ceil((double) RunRtlPower.NUMBER_OF_SECONDS / averageNumberOfSecondsPerFullSpectrum)) * averageNumberOfSecondsPerFullSpectrum;
		for (long i = 0; i < numberOfSeconds; i++) {
			updateProgress("Running rtl_power ETA: " + formatETA((numberOfSeconds - i) * 1000), i, numberOfSeconds);
			Thread.sleep(1000);
		}
		updateProgress("Running rtl_power ETA: almost done", -1, -1);
		return null;
	}

}
