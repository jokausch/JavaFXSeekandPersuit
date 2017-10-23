package application;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Random;

public class Utils {

	static public final float map(float value,
								  float start1, float stop1,
								  float start2, float stop2) {
		float outgoing =
				start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
		String badness = null;
		if (outgoing != outgoing) {
			badness = "NaN (not a number)";

		} else if (outgoing == Float.NEGATIVE_INFINITY ||
				outgoing == Float.POSITIVE_INFINITY) {
			badness = "infinity";
		}
		if (badness != null) {
			System.out.println("ERROR: " + badness);
		}
		return outgoing;
	}


	/**
	 * Clamp value between min and max
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static double clamp(double value, double min, double max) {

		if (value < min)
			return min;

		if (value > max)
			return max;

		return value;
	}

	/**
	 * Map value of a given range to a target range
	 * @param value
	 * @param currentRangeStart
	 * @param currentRangeStop
	 * @param targetRangeStart
	 * @param targetRangeStop
	 * @return
	 */
	public static double map(double value, double currentRangeStart, double currentRangeStop, double targetRangeStart, double targetRangeStop) {
		return targetRangeStart + (targetRangeStop - targetRangeStart) * ((value - currentRangeStart) / (currentRangeStop - currentRangeStart));
	}

	/**
	 * Snapshot an image out of a node, consider transparency.
	 * 
	 * @param node
	 * @return
	 */
	public static Image createImage(Node node) {

		WritableImage wi;

		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);

		int imageWidth = (int) node.getBoundsInLocal().getWidth();
		int imageHeight = (int) node.getBoundsInLocal().getHeight();

		wi = new WritableImage(imageWidth, imageHeight);
		node.snapshot(parameters, wi);

		return wi;

	}
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randomNumberInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
    public static float randomFloatNumberInRange(float min, float max) {
        Random random = new Random();
        return random.nextFloat() * (max - min) + min;
    }



}
