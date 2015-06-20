package pl.szczurmys.nodemcu.event;

import jssc.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.nonNull;

/**
 * @author szczurmys
 */
public class DetectEventListener implements SerialPortEventListener {

	private final SerialPort serialPort;
	private final int timeout;

	private final Object lock = new Object();

	private final AtomicBoolean detected;

	public DetectEventListener(SerialPort serialPort, AtomicBoolean detected, int timeout) {
		this.serialPort = serialPort;
		this.timeout = timeout;
		this.detected = detected;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (!serialPort.getPortName().equals(event.getPortName())) {
			System.err.println("WARNING: serialEvent return wrong port name; Port: " + event.getPortName());
		}
		if (event.isRXCHAR() && event.getEventValue() > 0) {
			String buffer;
			synchronized (lock) {
				try {
					buffer = serialPort.readString(event.getEventValue(), timeout);
					if (nonNull(buffer) && buffer.contains("\r\n> ")) {
						detected.set(true);
					}
				} catch (SerialPortException | SerialPortTimeoutException e) {
					e.printStackTrace();
					return;
				}
			}
			System.out.print(buffer);
		} else if (event.isBREAK()) {
			System.err.println("WARNING: serialEvent return BREAK;");
		} else if (event.isERR()) {
			System.err.println("ERROR: serialEvent return BREAK;");
		}
	}
}