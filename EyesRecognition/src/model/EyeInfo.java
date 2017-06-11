package model;

/**
 * A class which stores information about eye image.
 * 
 * @author Jakub Podgórski
 *
 */
public class EyeInfo {

	String dir;
	String name;
	String side;
	Integer phase;
	Double[] histogram;
	Integer[] size;

	/**
	 * A constructor of EyeInfo class.
	 * 
	 * @param dir		the directory name in which image is located
	 * @param name		the name of an image
	 * @param side		the side of an eye; can be specified as: right, left, unknown
	 * @param phase		the eye exposure phase
	 * @param histogram	the vector which stores information about histogram data
	 * @param size		the vector which stores dimensions of an image
	 */
	public EyeInfo(String dir, String name, String side, Integer phase, Double[] histogram, Integer[] size) {
		this.dir = dir;
		this.name = name;
		this.side = side;
		this.phase = phase;
		this.histogram = histogram;
		this.size = size;
	}

	/**
	 * Gets the vector which stores information about histogram data.
	 * 
	 * @return the vector which stores information about histogram data
	 */
	public Double[] getHistogram() {
		return histogram;
	}

	/**
	 * Gets the directory name.
	 * 
	 * @return the directory name
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * Gets the name of an image.
	 * 
	 * @return the name of an image
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the side of an eye.
	 * 
	 * @return the side of an eye
	 */
	public String getSide() {
		return side;
	}

	/**
	 * Gets the eye exposure phase.
	 * 
	 * @return the eye exposure phase
	 */
	public Integer getPhase() {
		return phase;
	}

	/**
	 * Gets the vector which stores dimensions of an image.
	 * 
	 * @return the vector which stores dimensions of an image
	 */
	public Integer[] getSize() {
		return size;
	}

}
