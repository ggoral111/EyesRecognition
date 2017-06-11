package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class which helps in files processing.
 * 
 * @author Jakub Podgórski, Marcin Kwaœnik
 *
 */
public class FileOperations {

	/**
	 * Method which lists files in folder considering given conditions.
	 * 
	 * @param path					the path to the directory.
	 * @param fileExtensionsToOmmit	the file extensions which should be omit while scanning file folder
	 * @return						the list of files paths
	 * @throws IOException			the IOException which is thrown when operations on files fails
	 */
	public List<String> listFilesInFolder(String path, String[] fileExtensionsToOmmit) throws IOException {
		final File folder = new File(path);
		List<String> filesPathsList = new ArrayList<String>();
		Integer badExtensionsCounter;
		String tempPath;

		for (final File file : folder.listFiles()) {
			tempPath = file.getPath();
			badExtensionsCounter = 0;

			for (String s : fileExtensionsToOmmit) {
				if (tempPath.endsWith(s)) {
					badExtensionsCounter++;
					break;
				}
			}

			if (badExtensionsCounter == 0) {
				filesPathsList.add(tempPath);
			}
		}

		return filesPathsList;
	}

	/**
	 * Method which creates directory in given path.
	 * 
	 * @param pathToCreateDir	the path in which directory will be created
	 */
	public void createDirectory(String pathToCreateDir) {
		Path path = Paths.get(pathToCreateDir);

		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method which saves a String object to .json file.
	 * 
	 * @param contentToSave				the String object ready to save to file
	 * @param filePath					the path where file should be saved
	 * @throws FileNotFoundException	the FileNotFoundException which indicates on problems with given path
	 */
	public void saveResultToFile(String contentToSave, String filePath) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new FileOutputStream(filePath + "/result.json"));
		pw.println(contentToSave);

		pw.close();

		System.out.println("Done. Result was saved to specified file.");
	}

	/**
	 * Method which role is to get directory name from given path.
	 * 
	 * @param path				the path from which directory name will be extracted
	 * @param delimeter			the delimiter which separates directory path
	 * @param delimetersAmount	the amount of delimiters which should be taken into account when folder name is extracted from folder path
	 * @return					the directory name
	 */
	public String getDirectoryName(String path, String delimeter, Integer delimetersAmount) {
		Integer pos = path.length();

		for (int i = 0; i < delimetersAmount; i++) {
			pos = path.lastIndexOf(delimeter, pos - 1);
		}

		return path.substring(pos + 1);
	}

}
