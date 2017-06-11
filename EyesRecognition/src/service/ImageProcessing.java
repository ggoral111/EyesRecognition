package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.google.gson.GsonBuilder;

import model.EyeInfo;
import model.PhotoInfo;

/**
 * A class which process eye images.
 * 
 * @author Jakub Podgórski, Marcin Kwaœnik
 *
 */
public class ImageProcessing {

	FileOperations fo;
	Integer[] lowestResVec;

	/**
	 * A constructor of ImageProcessing class. Responsible for OpenCV library
	 * loading and creating a FileOperations object.
	 */
	public ImageProcessing() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		fo = new FileOperations();
		lowestResVec = new Integer[] { 0, 0 };
	}

	/**
	 * Method which is responsible for parallel image processing.
	 * 
	 * @param radius				the radius of the Gaussian blur which will be applied on an image; the radius value must be odd; larger radius means that the larger neighborhood of pixels will be averaged
	 * @param pathToFolder			the path to folder where images ready to process are stored
	 * @param pathToFolderDelimiter	the delimiter which separates directory path
	 * @param delimitersAmount		the amount of delimiters which should be taken into account when folder name is extracted from folder path
	 * @param fileExtensionsToOmmit	the file extensions which should be omit while scanning file folder
	 * @param threadpool			the number of threads used for image processing; when number of threads are greater than number of images in folder then number of threads will be equal to image files amount
	 * @throws IOException			the IOException which is thrown when operations on files fails
	 */
	public void multithreadPhotoTagging(Integer radius, String pathToFolder, String pathToFolderDelimiter,
			Integer delimitersAmount, String[] fileExtensionsToOmmit, Integer threadpool) throws IOException {
		List<String> eyeImagesList = fo.listFilesInFolder(pathToFolder, fileExtensionsToOmmit);
		lowestResVec = getLowestImgRes(eyeImagesList);
		List<PhotoInfo> photoInfoList = new ArrayList<PhotoInfo>();
		List<EyeInfo> eyeInfoList = new ArrayList<EyeInfo>();
		List<Mat> histogramImages = new ArrayList<Mat>();
		fo.createDirectory(pathToFolder + "/result");

		if (!eyeImagesList.isEmpty()) {

			if (eyeImagesList.size() < threadpool) {
				threadpool = eyeImagesList.size();
			}

			ExecutorService threadPool = Executors.newFixedThreadPool(threadpool);

			for (String s : eyeImagesList) {
				threadPool.execute(new Runnable() {
					public void run() {
						try {
							photoInfoList.add(
									imageProcessing(radius, pathToFolder, s, pathToFolderDelimiter, delimitersAmount));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

			threadPool.shutdown();

			try {
				threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Collections.sort(photoInfoList,
					(PhotoInfo pi1, PhotoInfo pi2) -> pi1.getEyeInfo().getName().compareTo(pi2.getEyeInfo().getName()));

			for (PhotoInfo pi : photoInfoList) {
				eyeInfoList.add(pi.getEyeInfo());
				histogramImages.add(pi.getHistogramImage());
			}

			Imgcodecs.imwrite(
					pathToFolder + "/result/" + "histogram_all"
							+ eyeImagesList.get(0).substring(eyeImagesList.get(0).lastIndexOf('.')),
					verticalImagesConcatenation(histogramImages));

			fo.saveResultToFile(new GsonBuilder().create().toJson(eyeInfoList), pathToFolder);
		}
	}

	/**
	 * Method which draws a histogram.
	 * 
	 * @param eyeImageGrayScale		the image of an eye in gray scale
	 * @param histogram_width		the width of histogram image
	 * @param histogram_height		the height of histogram image
	 * @param isImageInGrayScale	the variable which indicates whether image is in gray scale or not
	 * @return						the histogram image
	 */
	private Mat drawHistogram(Mat eyeImageGrayScale, int histogram_width, int histogram_height,
			boolean isImageInGrayScale) {
		List<Mat> eyeImageGrayScaleImages = new ArrayList<Mat>();
		Imgproc.resize(eyeImageGrayScale, eyeImageGrayScale, new Size(lowestResVec[0], lowestResVec[1]));
		Core.split(eyeImageGrayScale, eyeImageGrayScaleImages);

		MatOfInt histSize = new MatOfInt(256);
		MatOfInt channels = new MatOfInt(0);
		MatOfFloat histRange = new MatOfFloat(0, 256);

		Mat histogram_b = new Mat();
		Mat histogram_g = new Mat();
		Mat histogram_r = new Mat();

		Imgproc.calcHist(eyeImageGrayScaleImages.subList(0, 1), channels, new Mat(), histogram_b, histSize, histRange,
				false);

		if (!isImageInGrayScale) {
			Imgproc.calcHist(eyeImageGrayScaleImages.subList(1, 2), channels, new Mat(), histogram_g, histSize,
					histRange, false);
			Imgproc.calcHist(eyeImageGrayScaleImages.subList(2, 3), channels, new Mat(), histogram_r, histSize,
					histRange, false);
		}

		int bin_w = (int) Math.round(histogram_width / histSize.get(0, 0)[0]);

		Mat histogramImage = new Mat(histogram_height, histogram_width, CvType.CV_8UC3, new Scalar(0, 0, 0));
		Core.normalize(histogram_b, histogram_b, 0, histogramImage.rows(), Core.NORM_MINMAX, -1, new Mat());

		if (!isImageInGrayScale) {
			Core.normalize(histogram_g, histogram_g, 0, histogramImage.rows(), Core.NORM_MINMAX, -1, new Mat());
			Core.normalize(histogram_r, histogram_r, 0, histogramImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		}

		for (int i = 1; i < histSize.get(0, 0)[0]; i++) {
			Imgproc.line(histogramImage,
					new Point(bin_w * (i - 1), histogram_height - Math.round(histogram_b.get(i - 1, 0)[0])),
					new Point(bin_w * (i), histogram_height - Math.round(histogram_b.get(i, 0)[0])),
					new Scalar(255, 255, 255), 2, 8, 0);

			if (!isImageInGrayScale) {
				Imgproc.line(histogramImage,
						new Point(bin_w * (i - 1), histogram_height - Math.round(histogram_g.get(i - 1, 0)[0])),
						new Point(bin_w * (i), histogram_height - Math.round(histogram_g.get(i, 0)[0])),
						new Scalar(0, 255, 0), 2, 8, 0);
				Imgproc.line(histogramImage,
						new Point(bin_w * (i - 1), histogram_height - Math.round(histogram_r.get(i - 1, 0)[0])),
						new Point(bin_w * (i), histogram_height - Math.round(histogram_r.get(i, 0)[0])),
						new Scalar(0, 0, 255), 2, 8, 0);
			}
		}

		return histogramImage;
	}

	/**
	 * Method which is responsible for image processing - finding the brightest spot which allows to assess the side of an eye and drawing a histogram of an image.
	 * 
	 * @param radius				the radius of the Gaussian blur which will be applied on an image; the radius value must be odd; larger radius means that the larger neighborhood of pixels will be averaged
	 * @param pathToFolder			the path to folder where images ready to process are stored
	 * @param pathToFile			the path to image file which is ready to process
	 * @param pathToFolderDelimiter	the delimiter which separates directory path
	 * @param delimitersAmount		the amount of delimiters which should be taken into account when folder name is extracted from folder path
	 * @return						the PhotoInfo object which contains EyeInfo object and histogram image
	 * @throws IOException			the IOException which is thrown when operations on files fails
	 */
	private PhotoInfo imageProcessing(Integer radius, String pathToFolder, String pathToFile,
			String pathToFolderDelimiter, Integer delimitersAmount) throws IOException {
		String eyeSide = null;

		System.out.println("Path: " + pathToFile);

		Mat eyeImageSource = Imgcodecs.imread(pathToFile);
		Mat eyeImageGrayScale = new Mat(eyeImageSource.rows(), eyeImageSource.cols(), eyeImageSource.type());
		Imgproc.cvtColor(eyeImageSource, eyeImageGrayScale, Imgproc.COLOR_RGB2GRAY);

		// drawMask(eyeImageGrayScale, pathToFolder, pathToFile);

		Imgproc.GaussianBlur(eyeImageGrayScale, eyeImageGrayScale, new Size(radius, radius), 0);
		MinMaxLocResult mmlr = Core.minMaxLoc(eyeImageGrayScale);

		System.out.println("Brightest spot location: " + mmlr.maxLoc);

		if (mmlr.maxLoc.x > (eyeImageGrayScale.width() / 3) && mmlr.maxLoc.x < ((eyeImageGrayScale.width() / 3) * 2)) {
			eyeSide = "u";
		} else if (mmlr.maxLoc.x >= eyeImageGrayScale.width() / 2) {
			eyeSide = "l";
		} else {
			eyeSide = "r";
		}

		Imgproc.circle(eyeImageSource, mmlr.maxLoc, radius, new Scalar(255, 0, 0), 2);
		Imgcodecs.imwrite(pathToFolder + "/result/" + pathToFile.substring(pathToFile.lastIndexOf('\\') + 1),
				eyeImageSource);

		Mat histogramImage = drawHistogram(eyeImageGrayScale, 1200, 1200, true);
		Imgcodecs.imwrite(
				pathToFolder + "/result/" + "histogram_" + pathToFile.substring(pathToFile.lastIndexOf('\\') + 1),
				histogramImage);

		return new PhotoInfo(new EyeInfo(fo.getDirectoryName(pathToFolder, pathToFolderDelimiter, delimitersAmount),
				pathToFile.substring(pathToFile.lastIndexOf('\\') + 1), eyeSide, -1, new Double[] {},
				new Integer[] { eyeImageGrayScale.width(), eyeImageGrayScale.height() }), histogramImage);
	}

	/**
	 * Method which vertically concatenates list of images into one piece.
	 * 
	 * @param imagesToConcatenate	the list of images to concatenate
	 * @return						the concatenated image
	 */
	private Mat verticalImagesConcatenation(List<Mat> imagesToConcatenate) {
		Mat concatenatedImage = new Mat();
		Core.vconcat(imagesToConcatenate, concatenatedImage);

		return concatenatedImage;
	}

	/**
	 * Method which draws a contours of an image.
	 * 
	 * @param image			the image of an eye in gray scale
	 * @param pathToFolder	the path to folder where images are stored
	 * @param pathToFile	the path to image file
	 */
	private void drawMask(Mat image, String pathToFolder, String pathToFile) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat ImageMask = new Mat();
		image.convertTo(ImageMask, CvType.CV_8UC3);

		Imgproc.findContours(ImageMask, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat imageContour = new Mat(ImageMask.size(), ImageMask.type());

		for (int i = 0; i < contours.size(); i++) {
			Imgproc.drawContours(imageContour, contours, i, new Scalar(255, 255, 255), -1);
		}

		Imgcodecs.imwrite(
				pathToFolder + "/result/" + "contour_" + pathToFile.substring(pathToFile.lastIndexOf('\\') + 1),
				imageContour);
	}

	/**
	 * Method which returns the lowest resolution of an image in a set.
	 * 
	 * @param eyeImagesList	the list which contains paths to images
	 * @return				the lowest resolution of an image
	 * @throws IOException	the IOException which is thrown when operations on files fails
	 */
	private Integer[] getLowestImgRes(List<String> eyeImagesList) throws IOException {
		Integer[] lowestLocalResVec = new Integer[] { 0, 0 };

		for (String s : eyeImagesList) {
			Mat eyeImage = Imgcodecs.imread(s);

			if (lowestLocalResVec[0] == 0 && lowestLocalResVec[1] == 0) {
				lowestLocalResVec[0] = eyeImage.width();
				lowestLocalResVec[1] = eyeImage.height();
			} else if (lowestLocalResVec[0] >= eyeImage.width() && lowestLocalResVec[1] >= eyeImage.height()) {
				lowestLocalResVec[0] = eyeImage.width();
				lowestLocalResVec[1] = eyeImage.height();
			}
		}

		return lowestLocalResVec;
	}

}
