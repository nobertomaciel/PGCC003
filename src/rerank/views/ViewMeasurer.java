package rerank.views;

import java.util.ArrayList;
import java.util.HashMap;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class ViewMeasurer implements IDigitalObjectDistanceMeasurer{

	HashMap<String, Double> views;
	ArrayList<String> imgIds;
	ArrayList<String> topic;
	
	public void setTopic(ArrayList<String> topic) {
		this.topic = topic;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		Double numViews = this.views.get(digitalObject.getId());
		
		if(numViews != null)
			return numViews;
		else
			return 0;		
	}

	public void setImgIds(ArrayList<String> imgIds) {
		this.imgIds = imgIds;
	}

	public void setDistances(HashMap<String, Double> distances) {
		this.views = distances;
	}

	public void setDistances(double[][][] distances) {
		//Not used
	}
}