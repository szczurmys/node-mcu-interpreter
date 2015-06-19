package pl.szczurmys.nodemcu;

/**
 * @author szczurmys
 */
public enum ErrorCode {
	SUCCESS(0),
	LACK_PARAMETERS(1),
	FILE_TO_RUN_NOT_EXISTS(2),
	FILE_TO_RUN_IS_NOT_FILE(3),
	NOT_FIND_PORT(4),
	NOT_FIND_ANY_PORT(5),
	PARENT_DIR_MUST_BE_ALSO_PARENT_FOR_MAIN_FILE(6),
	SERIAL_PORT_EXCEPTION(7),
	SERIAL_PORT_TIMEOUT_EXCEPTION(8),
	DETECTION_EXCEPTION(9),
	IO_EXCEPTION(10),
	FILE_TO_RUN_IN_DIRECTORIES_WHEN_ONLY_ONE_AND_IGNORE_DIRECTORIES(11),;
	int code;

	ErrorCode(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}
