package pl.szczurmys.nodemcu.event;

import jssc.*;
import pl.szczurmys.nodemcu.LineQueue;

import java.io.ByteArrayOutputStream;

/**
 * @author szczurmys
 */
public class ReadLinesEventListener implements SerialPortEventListener {

	private final LineQueue lineQueue;
	private final SerialPort serialPort;
	private final int timeout;

	private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

	public ReadLinesEventListener(SerialPort serialPort, LineQueue lineQueue, int timeout) {
		this.lineQueue = lineQueue;
		this.serialPort = serialPort;
		this.timeout = timeout;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		if (!serialPort.getPortName().equals(event.getPortName())) {
			System.err.println("WARNING: serialEvent return wrong port name; Port: " + event.getPortName());
		}
		if (event.isRXCHAR() && event.getEventValue() > 0) {
			synchronized (lineBuffer) {
				byte[] buffer;
				try {
					buffer = serialPort.readBytes(event.getEventValue(), timeout);
				} catch (SerialPortException | SerialPortTimeoutException e) {
					e.printStackTrace();
					return;
				}

				for (byte b : buffer) {
					lineBuffer.write(b);
					if (((byte) '\n') == b) {
						String line = new String(lineBuffer.toByteArray());
						lineQueue.addLine(line);
						lineBuffer.reset();
					}
				}
			}
		} else if (event.isBREAK()) {
			System.err.println("WARNING: serialEvent return BREAK;");
		} else if (event.isERR()) {
			System.err.println("ERROR: serialEvent return BREAK;");
		}
	}


}
