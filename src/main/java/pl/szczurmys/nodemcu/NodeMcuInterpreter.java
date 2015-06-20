package pl.szczurmys.nodemcu;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import pl.szczurmys.nodemcu.event.SelectorEventListener;

import java.io.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.szczurmys.nodemcu.event.SelectorEventListener.EventType.*;

/**
 * @author szczurmys
 */
public class NodeMcuInterpreter implements Closeable {
	public static final int DEFAULT_TIMEOUT = 10000;
	public static final int DEFAULT_BAUD_RATE = SerialPort.BAUDRATE_9600;

	public static final int REPEATED_DETECTED_TIMES = 100;

	private final SelectorEventListener selectorEventListener;

	private String port;
	private SerialPort serialPort;
	private String endCommand;
	private int timeout;
	private boolean closed = false;

	private final LineQueue lineQueue = new LineQueue();
	private final AtomicBoolean detected = new AtomicBoolean(false);


	public NodeMcuInterpreter(String port, String endCommand) throws SerialPortException, DetectedException, SerialPortTimeoutException {

		this(port, endCommand, DEFAULT_BAUD_RATE, DEFAULT_TIMEOUT);
	}

	public NodeMcuInterpreter(String port, String endCommand, int baudRate) throws SerialPortException, DetectedException, SerialPortTimeoutException {

		this(port, endCommand, baudRate, DEFAULT_TIMEOUT);
	}

	public NodeMcuInterpreter(String port, String endCommand, int baudRate, int timeout) throws SerialPortException, DetectedException, SerialPortTimeoutException {
		this.port = port;
		this.endCommand = endCommand;
		this.timeout = timeout;
		this.serialPort = new SerialPort(port);
		this.serialPort.openPort();
		this.serialPort.setParams(baudRate,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		selectorEventListener = new SelectorEventListener(serialPort, detected, lineQueue, timeout);
		serialPort.addEventListener(selectorEventListener);

		testCommand();

	}

	@Override
	public void close() {
		if (nonNull(serialPort)) {
			try {
				serialPort.removeEventListener();
			} catch (SerialPortException e) {
				System.err.println("Error when try removeEventListener, '" + port + "'. Message: " + e.getMessage());
				e.printStackTrace();
			}
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				System.err.println("Error when try close port '" + port + "'. Message: " + e.getMessage());
				e.printStackTrace();
			}
		}
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

	public void deleteFile(String file) throws SerialPortException, SerialPortTimeoutException {
		selectorEventListener.setEventType(READ_LINE_MASK);

		String command = String.format("file.remove(\"%s\");", file);
		String resultCommand = writeAndReadRepeatedCommand(command);
		if (!resultCommand.contains(command)) {
			throw new SerialPortException(port, "deleteFile", String.format(
					"\r\nResult command: %s \r\n" +
							"not equals with command: %s",
					resultCommand.trim(),
					command
			));
		}
		System.out.println(resultCommand.trim());
		return;
	}

	public void saveFile(String file, String content) throws IOException, SerialPortException, SerialPortTimeoutException {
		StringReader stringReader = new StringReader(content);
		saveFile(file, stringReader);
	}


	public void saveFile(String file, InputStream inputStream) throws IOException, SerialPortException, SerialPortTimeoutException {
		InputStreamReader reader = new InputStreamReader(inputStream);
		saveFile(file, reader);
	}

	public void runFile(String file, boolean waitForOutputs) throws SerialPortException, SerialPortTimeoutException {
		selectorEventListener.setEventType(READ_LINE_MASK);

		String command = String.format("dofile(\"%s\");", file);
		String resultCommand = writeAndReadRepeatedCommand(command);
		if (!resultCommand.contains(command)) {
			throw new SerialPortException(port, "runFile", String.format(
					"\r\nResult command: %s \r\n",
					resultCommand.trim()
			));
		}
		System.out.println(resultCommand.trim());

		if (waitForOutputs) {
			selectorEventListener.setEventType(READ_ALL_MASK);

			System.out.println("OUTPUT.");
			System.out.println("If you want exit, press enter.");
			System.out.println("----------------------------------------------------------------");

			BufferedReader br = new BufferedReader(
					new InputStreamReader(System.in));

			try {
				while (!br.ready()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ignore) {
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	public void saveFile(String file, Reader reader) throws IOException, SerialPortException, SerialPortTimeoutException {
		selectorEventListener.setEventType(READ_LINE_MASK);

		String command = String.format("file.open(\"%s\",\"w+\");", file);
		String resultCommand = writeAndReadRepeatedCommand(command);
		if (!command.trim().equals(resultCommand.trim())) {
			tryCloseFile();
			throw new SerialPortException(port, "saveFile", "Cannot open file to write. Device return: " + resultCommand);
		}
		System.out.println(resultCommand.trim());

		command = String.format("w = file.writeline;", file);
		resultCommand = writeAndReadRepeatedCommand(command);
		if (!command.trim().equals(resultCommand.trim())) {
			tryCloseFile();
			throw new SerialPortException(port, "saveFile", "Cannot get writeline. Device return: " + resultCommand);
		}
		System.out.println(resultCommand.trim());

		BufferedReader bufferedReader = new BufferedReader(reader);

		String line;
		while (nonNull(line = bufferedReader.readLine())) {
			command = String.format("w([[%s]]);", line);
			resultCommand = writeAndReadRepeatedCommand(command);
			if (!command.trim().equals(resultCommand.trim())) {
				tryCloseFile();
				throw new SerialPortException(port, "saveFile", "Cannot write line. Device return: " + resultCommand);
			}
			System.out.println(resultCommand.trim());
		}

		command = String.format("w = nil;", file);
		resultCommand = writeAndReadRepeatedCommand(command);
		if (!command.trim().equals(resultCommand.trim())) {
			tryCloseFile();
			throw new SerialPortException(port, "saveFile", "Cannot reset variable 'w'. Device return: " + resultCommand);
		}
		System.out.println(resultCommand.trim());

		tryCloseFile();
	}
	public void compile(String file) throws SerialPortException, SerialPortTimeoutException {
		selectorEventListener.setEventType(READ_LINE_MASK);

		String command = String.format("node.compile(\"%s\");", file);
		String resultCommand = writeAndReadRepeatedCommand(command);
		if (!resultCommand.contains(command)) {
			throw new SerialPortException(port, "compile", String.format(
					"\r\nResult command: %s \r\n" +
							"not equals with command: %s",
					resultCommand.trim(),
					command
			));
		}
		System.out.println(resultCommand.trim());
		return;

	}

	private void tryCloseFile() {
		String command = String.format("file.close();");
		try {
			String resultCommand = writeAndReadRepeatedCommand(command);
			if (!command.trim().equals(resultCommand.trim())) {
				System.err.println("Error when try file.close(); Return: " + resultCommand);
			} else {
				System.out.println(resultCommand.trim());
			}
		} catch (SerialPortException | SerialPortTimeoutException e) {
			e.printStackTrace();
		}
	}


	private void testCommand() throws SerialPortException, SerialPortTimeoutException, DetectedException {
		selectorEventListener.setEventType(DETECT_MASK);
		System.out.print("Wait for device response .");

		for (int i = 0; i < REPEATED_DETECTED_TIMES && !detected.get(); i++) {
			writeLine("");
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			System.out.print(".");
		}
		System.out.println();
		if (!detected.get()) {
			throw new DetectedException("Device not response.");
		}
		System.out.println("OK");

		selectorEventListener.setEventType(READ_LINE_MASK);

		String command = "majorVer, minorVer, devVer, chipid, flashid, flashsize, flashmode, flashspeed = node.info();";

		String resultCommand1 = writeAndReadRepeatedCommand(command);

		command = "print(string.format(\"NodeMCU v.: %13s\", majorVer..\".\"..minorVer..\".\"..devVer));";
		String resultCommand2 = writeAndReadRepeatedCommand(command);

		String version = readLine();
		if (isNull(version) || !version.startsWith("NodeMCU v.: ")) {
			throw new DetectedException("Not detect NodeMCU, received data: " + version);
		}
		System.out.println(version.trim());


	}

	private void writeLine(String command) throws SerialPortException, SerialPortTimeoutException {
		if (!serialPort.writeString(command + endCommand)) {
			throw new SerialPortException(port, "writeLine", "serialPort.writeString return false");
		}
	}


	private String writeAndReadRepeatedCommand(String command) throws SerialPortException, SerialPortTimeoutException {
		writeLine(command);
		String readLine = readLine();

		if (!command.startsWith("> ") && readLine.startsWith("> ")) {
			readLine = readLine.substring(2);
		}
		return readLine;
	}


	private String readLine() throws SerialPortTimeoutException {
		try {
			return lineQueue.waitForLine(timeout);
		} catch (TimeoutException e) {
			throw new SerialPortTimeoutException(port, "waitForLine", timeout);
		}
	}


}
