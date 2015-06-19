package pl.szczurmys.nodemcu;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author szczurmys
 */
public class FileWriter {
	private final File directory;
	private final NodeMcuInterpreter interpreter;
	private final boolean onlyRemoveFiles;
	private final boolean ignoreDirectories;
	private final Collection<String> excludeFiles;

	public FileWriter(File directory, NodeMcuInterpreter interpreter,
					  boolean onlyRemoveFiles, boolean ignoreDirectories,
					  Collection<String> excludeFiles) {

		this.directory = directory;
		this.interpreter = interpreter;
		this.onlyRemoveFiles = onlyRemoveFiles;
		this.ignoreDirectories = ignoreDirectories;
		this.excludeFiles = excludeFiles;
	}


	public void run() throws SerialPortException, IOException, SerialPortTimeoutException {
		checkDir(directory);
	}

	public void runOnlyForOneFile(File file) throws SerialPortException, IOException, SerialPortTimeoutException {
		writeFile(file);
	}

	private void checkDir(File dir) throws IOException, SerialPortException, SerialPortTimeoutException {
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				if (ignoreDirectories) {
					continue;
				}
				checkDir(f);
				continue;
			}
			if (!f.isFile()) {
				continue;
			}
			writeFile(f);

		}
	}

	private void writeFile(File file) throws IOException, SerialPortException, SerialPortTimeoutException {
		if (!file.isFile()) {
			throw new IOException("File '" + file.getAbsolutePath() + "' is not file!");
		}
		String relativePath = FileHelper.getUnixRelativePath(directory, file);

		if (excludeFiles.contains(relativePath)) {
			System.out.println("Exclude file " + file.getAbsolutePath());
			return;
		}

		interpreter.deleteFile(relativePath);
		if (!onlyRemoveFiles) {
			try (InputStream inputStream = new FileInputStream(file)) {
				try {
					interpreter.saveFile(relativePath, inputStream);
				} catch (SerialPortException | SerialPortTimeoutException e) {
					try {
						interpreter.deleteFile(relativePath);
					} catch (SerialPortException | SerialPortTimeoutException e2) {
						e2.printStackTrace();
					}
					throw e;
				}
			}
		}
	}
}
