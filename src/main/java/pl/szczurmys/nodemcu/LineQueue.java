package pl.szczurmys.nodemcu;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.isNull;

/**
 * Created by Jakub on 2015-06-20.
 */
public class LineQueue {
	private final Queue<String> lines = new ConcurrentLinkedQueue<>();

	public void addLine(String line) {
		lines.add(line);
	}

	public String getLine() {
		return lines.poll();
	}

	public String waitForLine(int timeout) throws TimeoutException {
		long timeoutTime = System.currentTimeMillis() + timeout;
		String line;
		do {
			if (timeoutTime < System.currentTimeMillis()) {
				throw new TimeoutException("waitForLine timeout: " + timeoutTime);
			}
			line = getLine();
		} while (isNull(line));
		return line;
	}
}
