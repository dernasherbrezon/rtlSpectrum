package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MacOsUtilTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private File command;

	@Test
	public void dark() throws Exception {
		setupCommand("defaults_mock.sh");
		assertTrue(MacOsUtil.isDark(command.getAbsolutePath()));
	}

	@Test
	public void white() {
		assertFalse(MacOsUtil.isDark("echo"));
	}

	@Test
	public void testInvalidCode() throws Exception {
		setupCommand("defaults_fail.sh");
		assertFalse(MacOsUtil.isDark(command.getAbsolutePath()));
	}

	@Test
	public void testUnknownCommand() {
		assertFalse(MacOsUtil.isDark("expected-unknown-command-" + UUID.randomUUID().toString()));
	}

	@Test
	public void testEmptyResponse() throws Exception {
		setupCommand("defaults_empty.sh");
		assertFalse(MacOsUtil.isDark(command.getAbsolutePath()));
	}

	private void setupCommand(String file) throws IOException, FileNotFoundException {
		command = new File(tempFolder.getRoot(), UUID.randomUUID().toString() + ".sh");
		try (FileOutputStream fos = new FileOutputStream(command); InputStream is = UITest.class.getClassLoader().getResourceAsStream(file)) {
			UITest.copy(is, fos);
		}
		command.setExecutable(true);
	}

}
