package ru.r2cloud.rtlspectrum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CliArgsTest {

	@Test
	public void testNoArgs() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[0]);
		assertFalse(parsed.hasError());
		assertFalse(parsed.isHelpRequested());
		assertNull(parsed.getBaseFile());
		assertTrue(parsed.getOperations().isEmpty());
	}

	@Test
	public void testSingleFileIsBase() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv" });
		assertFalse(parsed.hasError());
		assertNotNull(parsed.getBaseFile());
		assertEquals("a.csv", parsed.getBaseFile().getPath());
		assertTrue(parsed.getOperations().isEmpty());
	}

	@Test
	public void testMultipleNakedFilesBecomeAddsInOrder() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "b.csv", "c.csv" });
		assertFalse(parsed.hasError());
		assertEquals("a.csv", parsed.getBaseFile().getPath());
		assertEquals(2, parsed.getOperations().size());
		assertEquals(CliArgs.OpType.ADD, parsed.getOperations().get(0).getType());
		assertEquals("b.csv", parsed.getOperations().get(0).getFile().getPath());
		assertEquals(CliArgs.OpType.ADD, parsed.getOperations().get(1).getType());
		assertEquals("c.csv", parsed.getOperations().get(1).getFile().getPath());
	}

	@Test
	public void testAddFlagWasRemoved() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "--add", "b.csv" });
		assertTrue(parsed.hasError());
		assertTrue(parsed.getError().contains("removed"));
	}

	@Test
	public void testBareAddFlagWasRemoved() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--add" });
		assertTrue(parsed.hasError());
		assertTrue(parsed.getError().contains("removed"));
	}

	@Test
	public void testSubtractEqualsFormWorks() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--subtract=s.csv" });
		assertFalse(parsed.hasError());
		assertEquals(1, parsed.getOperations().size());
		assertEquals(CliArgs.OpType.SUBTRACT, parsed.getOperations().get(0).getType());
		assertEquals("s.csv", parsed.getOperations().get(0).getFile().getPath());
	}

	@Test
	public void testSubtractEqualsFormRequiresBaseFile() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "--subtract=s.csv" });
		assertTrue(parsed.hasError());
		assertTrue(parsed.getError().contains("base file"));
	}

	@Test
	public void testBareSubtractIsError() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--subtract" });
		assertTrue(parsed.hasError());
	}

	@Test
	public void testSubtractSpaceFormIsError() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--subtract", "s.csv" });
		assertTrue(parsed.hasError());
	}

	@Test
	public void testSubtractEmptyValueIsError() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--subtract=" });
		assertTrue(parsed.hasError());
	}

	@Test
	public void testSubtractValueMayStartWithDash() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--subtract=-x.csv" });
		assertFalse(parsed.hasError());
		assertEquals("-x.csv", parsed.getOperations().get(0).getFile().getPath());
	}

	@Test
	public void testNakedAndSubtractMixedInOrder() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "base.csv", "s1.csv", "--subtract=x.csv", "s2.csv" });
		assertFalse(parsed.hasError());
		assertEquals("base.csv", parsed.getBaseFile().getPath());
		assertEquals(3, parsed.getOperations().size());
		assertEquals(CliArgs.OpType.ADD, parsed.getOperations().get(0).getType());
		assertEquals("s1.csv", parsed.getOperations().get(0).getFile().getPath());
		assertEquals(CliArgs.OpType.SUBTRACT, parsed.getOperations().get(1).getType());
		assertEquals("x.csv", parsed.getOperations().get(1).getFile().getPath());
		assertEquals(CliArgs.OpType.ADD, parsed.getOperations().get(2).getType());
		assertEquals("s2.csv", parsed.getOperations().get(2).getFile().getPath());
	}

	@Test
	public void testUnknownOptionIsError() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--bogus" });
		assertTrue(parsed.hasError());
	}

	@Test
	public void testHelpShortCircuits() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "--help" });
		assertTrue(parsed.isHelpRequested());
		assertFalse(parsed.hasError());
	}

	@Test
	public void testHelpShortFlag() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "-h" });
		assertTrue(parsed.isHelpRequested());
	}

	@Test
	public void testJarFlagIsRejectedAsUnknownOption() {
		// --jar is a launcher-script flag; the app must not accept it
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "--jar", "x.jar", "a.csv" });
		assertTrue(parsed.hasError());
	}

	@Test
	public void testHelpShortCircuitsOverFileArgs() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "a.csv", "--help" });
		assertTrue(parsed.isHelpRequested());
		assertFalse(parsed.hasError());
	}

	@Test
	public void testMissingBaseErrorMentionsUsage() {
		CliArgs.Parsed parsed = CliArgs.parse(new String[] { "--subtract=b.csv" });
		assertTrue(parsed.hasError());
		assertTrue(parsed.getError().contains("base file"));
	}

	@Test
	public void testUsageMentionsCommands() {
		String usage = CliArgs.usage();
		assertTrue(usage.contains("--subtract="));
		assertTrue(usage.contains("--help"));
		assertFalse(usage.contains("--add "));
	}
}
