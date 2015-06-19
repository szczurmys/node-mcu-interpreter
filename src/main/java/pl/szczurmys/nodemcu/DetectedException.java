package pl.szczurmys.nodemcu;

/**
 * @author szczurmys
 */
public class DetectedException extends Exception {
	public DetectedException() {
	}

	public DetectedException(String message) {
		super(message);
	}

	public DetectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DetectedException(Throwable cause) {
		super(cause);
	}

	public DetectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
