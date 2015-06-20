package pl.szczurmys.nodemcu;

import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author szczurmys
 */
public class Interpreter {

	public static void main(String[] args) {
		//String endCommand = "\r\n";
		String endCommand = "\n";
		File fileToRun = null;
		File parentDirectory = null;
		boolean sendOnlyOne = false;
		boolean notRunOnlySave = false;
		String port = null;
		boolean selectFirstPort = false;
		boolean onlyRemoveFiles = false;
		int timeout = NodeMcuInterpreter.DEFAULT_TIMEOUT;
		boolean ignoreDirectories = false;
		boolean waitForOutput = true;
		String[] excludeFilesInString = new String[0];
		Set<String> excludeFiles = new HashSet<String>();

		if (args.length == 0) {
			System.err.println("Error: Lack parameters.");
			System.err.println();
			printHelp();
			System.exit(ErrorCode.LACK_PARAMETERS.code());
			return;
		}


		for (int i = 0; i < args.length; i++) {
			String v = args[i];
			
			if ("-h".equals(v) || "--help".equals(v)) {
				printHelp();
				System.exit(ErrorCode.SUCCESS.code());
				return;
			}

			if (i == args.length - 1) {
				fileToRun = new File(args[args.length - 1]);
				if (!fileToRun.exists()) {
					System.err.println(String.format("File '%s' not exits!", fileToRun.getAbsolutePath()));
					System.exit(ErrorCode.FILE_TO_RUN_NOT_EXISTS.code());
					return;
				}
				if (!fileToRun.isFile()) {
					System.err.println(String.format("File '%s' is not file!", fileToRun.getAbsolutePath()));
					System.exit(ErrorCode.FILE_TO_RUN_IS_NOT_FILE.code());
					return;
				}

				if (isNull(parentDirectory)) {
					parentDirectory = fileToRun.getParentFile();
				}
			}

			if (i < args.length - 1) {
				if (v.startsWith("-l=")) {
					endCommand = v.substring(3)
							.replaceAll("\\r", "\r")
							.replaceAll("\\n", "\n")
							.replaceAll("\\t", "\t");
				}
				if (v.startsWith("-p=")) {
					port = v.substring(3);
				}
				if (v.startsWith("-e=")) {
					excludeFilesInString = v.substring(3).split(",");
				}
				if ("-o".equals(v)) {
					sendOnlyOne = true;
				}
				if ("-f".equals(v)) {
					selectFirstPort = true;
				}
				if ("-R".equals(v)) {
					onlyRemoveFiles = true;
				}
				if ("-nr".equals(v)) {
					notRunOnlySave = true;
				}
				if ("-i".equals(v)) {
					ignoreDirectories = true;
				}
				if ("-nw".equals(v)) {
					waitForOutput = false;
				}
				if (v.startsWith("-t=")) {
					String sTimeout = v.substring(3);
					timeout = Integer.parseInt(sTimeout);
				}
				if (v.startsWith("-d=")) {
					parentDirectory = new File(v.substring(3));
				}

			}
		}
		if (!fileToRun.getAbsolutePath().startsWith(parentDirectory.getAbsolutePath())) {
			System.err.println("Parent dir must be also parent for main file!");
			System.err.println("Parent dir: " + parentDirectory.getAbsolutePath());
			System.err.println("Main file:  " + fileToRun.getAbsolutePath());
			System.exit(ErrorCode.PARENT_DIR_MUST_BE_ALSO_PARENT_FOR_MAIN_FILE.code());
			return;
		}

		final File parentForLambda = parentDirectory;
		Stream.of(excludeFilesInString).forEach(v -> {
			File tempF = new File(v);
			if (tempF.isAbsolute()) {
				if (tempF.getAbsolutePath().startsWith(parentForLambda.getAbsolutePath())) {
					excludeFiles.add(FileHelper.getUnixRelativePath(
							parentForLambda,
							tempF
					));
				}
			} else {
				String file = v.trim().replace('\\', '/');
				if (file.startsWith("/")) {
					file = file.substring(1);
				}
				excludeFiles.add(file);
			}
		});


		System.out.println("Available ports: ");
		String[] ports = SerialPortList.getPortNames();
		for (String p : ports) {
			System.out.println("\t" + p);
		}

		if (isNull(ports) || ports.length == 0) {
			System.err.println("Not find any ports!");
			System.exit(ErrorCode.NOT_FIND_ANY_PORT.code());
			return;
		}


		if (isNull(port)) {
			if (selectFirstPort) {
				port = ports[0];
			} else {
				System.out.print(String.format("Enter port name [%s]: ", ports[0]));
				try (Scanner scanner = new Scanner(System.in)) {
					String readPort = scanner.nextLine().trim();
					port = readPort.isEmpty() ? ports[0] : readPort;
				}
			}
		}

		boolean findPort = false;
		for (String p : ports) {
			if (p != null && p.equals(port)) {
				findPort = true;
			}
		}

		if (!findPort) {
			if (port != null) {
				System.err.println(String.format("Not find port '%s'!", port));
				System.exit(ErrorCode.NOT_FIND_PORT.code());
				return;
			}
		}
		String fileToRunRelative = FileHelper.getUnixRelativePath(
				parentDirectory,
				fileToRun
		);

		System.out.println("File to run: " + fileToRun.getAbsolutePath());
		System.out.println("File to run relative: " + fileToRunRelative);
		System.out.println("Parent directory: " + parentDirectory.getAbsolutePath());
		System.out.println("Send only one file: " + sendOnlyOne);
		System.out.println("Not execute, only save: " + notRunOnlySave);
		System.out.println("Only remove files: " + onlyRemoveFiles);
		System.out.println("Selected port: " + port);

		try (final NodeMcuInterpreter interpreter = new NodeMcuInterpreter(port, endCommand, timeout)) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (nonNull(interpreter) && !interpreter.isClosed()) {
					interpreter.close();
				}
				System.out.println("Close interpreter!");
				return;
			}));

			FileWriter fileWriter = new FileWriter(parentDirectory, interpreter, onlyRemoveFiles, ignoreDirectories, excludeFiles);
			if (sendOnlyOne) {
				if (ignoreDirectories &&
						!fileToRun.getParentFile().getAbsolutePath().equals(parentDirectory.getAbsolutePath())) {
					System.err.println("SendOnlyOne and IgnoreDirectories are set. You cannot have fileToRun in directory!");
					System.exit(ErrorCode.FILE_TO_RUN_IN_DIRECTORIES_WHEN_ONLY_ONE_AND_IGNORE_DIRECTORIES.code());
					return;
				} else {
					fileWriter.runOnlyForOneFile(fileToRun);
				}
			} else {
				fileWriter.run();
			}

			if (!notRunOnlySave && !excludeFiles.contains(fileToRunRelative)) {
				interpreter.runFile(fileToRunRelative, waitForOutput);
			}

		} catch (SerialPortException e) {
			System.err.println(e.getMessage());
			System.exit(ErrorCode.SERIAL_PORT_EXCEPTION.code());
			return;
		} catch (SerialPortTimeoutException e) {
			System.err.println(e.getMessage());
			System.exit(ErrorCode.SERIAL_PORT_TIMEOUT_EXCEPTION.code());
			return;
		} catch (DetectedException e) {
			System.err.println(e.getMessage());
			System.exit(ErrorCode.DETECTION_EXCEPTION.code());
			return;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(ErrorCode.IO_EXCEPTION.code());
			return;
		}


		System.exit(ErrorCode.SUCCESS.code());
	}

	public static void printHelp() {
		System.out.println("java -jar interpreter.jar [OPTIONS] <MAIN_FILE>");
		System.out.println("java -jar interpreter.jar -h|--help");
		System.out.println();
		System.out.println("MAIN_FILE - file to run and copy file from parent directory.");
		System.out.println("OPTIONS: ");
		System.out.println(createOptionHelp("-h or --help", "show this message"));
		System.out.println(createOptionHelp("-e=file1,...,file", "excludes file, path can be relative for parent directory"));
		System.out.println(createOptionHelp("-l=END_COMMAND", "end of command in esp firmware, default \\r\\n"));
		System.out.println(createOptionHelp("-d=PARENT_DIRECTORY", "parent directory, default - parent directory for MAIN_FILE"));
		System.out.println(createOptionHelp("-o", "send only main file"));
		System.out.println(createOptionHelp("-p=PORT", "serial port"));
		System.out.println(createOptionHelp("-f", "select first port"));
		System.out.println(createOptionHelp("-nr", "not execute (dofile), only save"));
		System.out.println(createOptionHelp("-t=TIMEOUT", "timeout, default - 10000 [ms]"));
		System.out.println(createOptionHelp("-R", "only remove files from device"));
		System.out.println(createOptionHelp("-i", "ignore files in directories"));
		System.out.println(createOptionHelp("-nw", "not wait for output"));
	}

	private static String createOptionHelp(String option, String help) {
		return String.format("  %-25s - %s", option, help);
	}

}
