package app;

import java.io.IOException;

import service.ImageProcessing;

public class Main {

	public static void main(String[] args) {
		try {
			String[] fileExtensionsToOmmit = new String[] { ".xml" };
			Integer threadpool = 16;

			ImageProcessing ip = new ImageProcessing();
			ip.multithreadPhotoTagging(81, "D:/EyesRecognition_photos/D_1bac6917a45b46871670c14f127f4bb9/2016-03-08", "/", 2, fileExtensionsToOmmit, threadpool);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
