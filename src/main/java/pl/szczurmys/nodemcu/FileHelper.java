package pl.szczurmys.nodemcu;

import java.io.File;

/**
 * @author szczurmys
 */
public class FileHelper {
	public static String getUnixRelativePath(File base, File path) {
		return base.toURI().relativize(path.toURI())
				.getPath().replace("\\", "/");
	}
}
