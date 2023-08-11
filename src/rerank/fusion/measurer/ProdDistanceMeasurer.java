package rerank.fusion.measurer;

import java.util.ArrayList;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class ProdDistanceMeasurer implements IDigitalObjectDistanceMeasurer{

	double distances[][][];
	ArrayList<String> imgIds;
	ArrayList<String> topic;
	
	public void setTopic(ArrayList<String> topic) {
		this.topic = topic;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		double prod = 1;
		
		for(int i = 0; i < topic.size(); i++)
			prod *= getAvgDescDist(i, imgIds.indexOf(digitalObject.getId()));
		
		return prod;
	}

	public void setImgIds(ArrayList<String> imgIds) {
		this.imgIds = imgIds;
	}

	public void setDistances(double distances[][][]) {
		this.distances = distances;
	}
	
	public double getAvgDescDist(int img, int object){
		double distSum = 0;
		
		for (int i = 0; i < this.distances.length; i++)
			distSum += this.distances[i][img][object];
		
		return distSum / this.distances.length;
	}
}
