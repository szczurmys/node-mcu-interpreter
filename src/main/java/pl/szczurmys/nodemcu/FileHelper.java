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

	public static String[] getNameAndExtensionFile(String relativePath) {
		String filename = null;
		String ext = null;
		if(relativePath.contains(".")) {
			String[] partFile = relativePath.split("\\.");
			ext = partFile[partFile.length-1];
			filename = relativePath.substring(0, relativePath.length()-ext.length()-1);
			return new String[] {filename, ext};
		}
		return new String[0];
	}
}
