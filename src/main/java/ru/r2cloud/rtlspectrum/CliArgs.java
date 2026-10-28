package ru.r2cloud.rtlspectrum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CliArgs {

	public enum OpType {
		ADD, SUBTRACT
	}

	public static class Operation {
		private final OpType type;
		private final File file;

		public Operation(OpType type, File file) {
			this.type = type;
			this.file = file;
		}

		public OpType getType() {
			return type;
		}

		public File getFile() {
			return file;
		}
	}

	public static class Parsed {
		private File baseFile;
		private final List<Operation> operations = new ArrayList<>();
		private boolean helpRequested;
		private String error;

		public File getBaseFile() {
			return baseFile;
		}

		public List<Operation> getOperations() {
			return Collections.unmodifiableList(operations);
		}

		public boolean isHelpRequested() {
			return helpRequested;
		}

		public String getError() {
			return error;
		}

		public boolean hasError() {
			return error != null;
		}
	}

	public static Parsed parse(String[] args) {
		Parsed result = new Parsed();
		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("--help") || arg.equals("-h")) {
				result.helpRequested = true;
				return result;
			} else if (arg.equals("--add") || arg.startsWith("--add=")) {
				result.error = "--add was removed; list files directly to add them";
				return result;
			} else if (arg.startsWith("--subtract=")) {
				String value = arg.substring("--subtract=".length());
				if (value.isEmpty()) {
					result.error = "--subtract requires a file argument, e.g. --subtract=x.csv";
					return result;
				}
				result.operations.add(new Operation(OpType.SUBTRACT, new File(value)));
				i++;
			} else if (arg.equals("--subtract")) {
				result.error = "--subtract requires =file form, e.g. --subtract=x.csv";
				return result;
			} else if (arg.startsWith("-")) {
				result.error = "Unknown option: " + arg + "\n" + usage();
				return result;
			} else {
				result.operations.add(new Operation(OpType.ADD, new File(arg)));
				i++;
			}
		}
		for (int j = 0; j < result.operations.size(); j++) {
			if (result.operations.get(j).getType() == OpType.ADD) {
				result.baseFile = result.operations.remove(j).getFile();
				break;
			}
		}
		if (!result.operations.isEmpty() && result.baseFile == null) {
			result.error = "A base file argument is required when using --subtract=file\n" + usage();
			return result;
		}
		return result;
	}

	public static String usage() {
		return "Usage: rtlSpectrum [file]... [--subtract=file]... [--help]\n"
				+ "  file              CSV file; the first opens as if Load File was pressed, the rest add in order\n"
				+ "  --subtract=file   Subtract file as if Subtract menu was used (requires base file, repeatable, applied in order; single token, no space)\n"
				+ "  --help, -h        Show this usage and exit";
	}
}
