package pl.szczurmys.nodemcu.event;

import jssc.*;

/**
 * @author szczurmys
 */
public class ReadAllAndPrintOutEventListener implements SerialPortEventListener {

	private final SerialPort serialPort;
	private final int timeout;

	private final Object lock = new Object();

	public ReadAllAndPrintOutEventListener(SerialPort serialPort, int timeout) {
		this.serialPort = serialPort;
		this.timeout = timeout;
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