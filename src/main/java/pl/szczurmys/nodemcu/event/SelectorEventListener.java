package pl.szczurmys.nodemcu.event;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import pl.szczurmys.nodemcu.LineQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;
import static pl.szczurmys.nodemcu.event.SelectorEventListener.EventType.*;

/**
 * @author szczurmys
 */
public class SelectorEventListener implements SerialPortEventListener {
	public enum EventType {
		READ_LINE_MASK,
		READ_ALL_MASK,
		DETECT_MASK,
		UNKNOWN
	}

	AtomicReference<EventType> eventType = new AtomicReference<>(UNKNOWN);

	private final ReadLinesEventListener readLinesEventListener;
	private final ReadAllAndPrintOutEventListener readAllAndPrintOutEventListener;
	private final DetectEventListener detectEventListener;

	public SelectorEventListener(SerialPort serialPort, AtomicBoolean detected, LineQueue lineQueue, int timeout) {
		readLinesEventListener = new ReadLinesEventListener(serialPort, lineQueue, timeout);
		readAllAndPrintOutEventListener = new ReadAllAndPrintOutEventListener(serialPort, timeout);
		detectEventListener = new DetectEventListener(serialPort, detected, timeout);
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
		if (READ_LINE_MASK.equals(eventType.get())) {
			readLinesEventListener.serialEvent(serialPortEvent);
		}
		if (READ_ALL_MASK.equals(eventType.get())) {
			readAllAndPrintOutEventListener.serialEvent(serialPortEvent);
		}
		if (DETECT_MASK.equals(eventType.get())) {
			detectEventListener.serialEvent(serialPortEvent);
		}
	}

	public void setEventType(EventType eventType) {
		if (isNull(eventType)) {
			this.eventType.set(UNKNOWN);
			return;
		}
		this.eventType.set(eventType);
	}
}
