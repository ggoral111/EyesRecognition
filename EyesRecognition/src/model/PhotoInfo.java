package model;

import org.opencv.core.Mat;

/**
 * A class which stores EyeInfo object and histogram image.
 * 
 * @author Jakub Podgórski
 *
 */
public class PhotoInfo {

	EyeInfo eyeInfo;
	Mat histogramImage;

	/**
	 * A constructor of PhotoInfo class.
	 * 
	 * @param eyeInfo			the EyeInfo object
	 * @param histogramImage	the histogram image
	 */
	public PhotoInfo(EyeInfo eyeInfo, Mat histogramImage) {
		this.eyeInfo = eyeInfo;
		this.histogramImage = histogramImage;
	}

	/**
	 * Gets the EyeInfo object.
	 * 
	 * @return the EyeInfo object
	 */
	public EyeInfo getEyeInfo() {
		return eyeInfo;
	}

	/**
	 * Gets the histogram image.
	 * 
	 * @return the histogram image
	 */
	public Mat getHistogramImage() {
		return histogramImage;
	}

}
