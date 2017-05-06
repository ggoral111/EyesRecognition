package app;

import java.io.IOException;

import service.ImageProcessing;

public class Main {

	public static void main(String[] args) 
	{				
		try 
		{		
			String[] fileExtensionsToOmmit = new String[]{".xml"};
			Integer threadpool = 16;
			
			ImageProcessing ip = new ImageProcessing();
			ip.multithreadPhotoTagging(61, "D:/EyesRecognition_photos/D_0ccf910f7fafc450a7c23785278eec2a/2016-03-03", "/", 2, fileExtensionsToOmmit, threadpool);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}

}
