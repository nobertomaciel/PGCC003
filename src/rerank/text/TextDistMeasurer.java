package rerank.text;

import java.util.ArrayList;
import java.util.HashMap;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class TextDistMeasurer implements IDigitalObjectDistanceMeasurer{

	HashMap<String, Double> distances;
	ArrayList<String> imgIds;
	ArrayList<String> topic;
	
	public void setTopic(ArrayList<String> topic) {
		this.topic = topic;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		Double dist = this.distances.get(digitalObject.getId());
		
		//if(dist != null)
			return dist;
		//else
			//return Double.MAX_VALUE;
	}

	public void setImgIds(ArrayList<String> imgIds) {
		this.imgIds = imgIds;
	}

	public void setDistances(HashMap<String, Double> distances) {
		this.distances = distances;
	}

	public void setDistances(double[][][] distances) {
		//Not used
	}
}