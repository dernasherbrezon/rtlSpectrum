package ru.r2cloud.rtlspectrum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

class MacOsUtil {

	static boolean isDark(String command) {
		ProcessBuilder builder = new ProcessBuilder().command(command, "read", "-g", "AppleInterfaceStyle");
		Process process;
		try {
			process = builder.start();
			int resultCode = process.waitFor();
			if (resultCode != 0) {
				return false;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.ISO_8859_1))) {
			String result = r.readLine();
			if (result == null) {
				return false;
			}
			return result.toLowerCase(Locale.UK).contains("dark");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private MacOsUtil() {
		// do nothing
	}
}
