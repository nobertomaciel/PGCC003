package rerank.fusion.measurer;

import java.util.ArrayList;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class MinDistanceMeasurer implements IDigitalObjectDistanceMeasurer{

	double distances[][][];
	ArrayList<String> imgIds;
	ArrayList<String> topic;
	
	public void setTopic(ArrayList<String> topic) {
		this.topic = topic;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		double min = Double.MAX_VALUE;
		
		for(int i = 0; i < topic.size(); i++){
			double dist = getProdDescDist(i, imgIds.indexOf(digitalObject.getId()));
			
			if(dist < min)
				min = dist;
		}
		
		return min;
	}

	public void setImgIds(ArrayList<String> imgIds) {
		this.imgIds = imgIds;
	}

	public void setDistances(double distances[][][]) {
		this.distances = distances;
	}
	
	public double getAvgDist(int img, int object){
		double distSum = 0;
		
		for (int i = 0; i < this.distances.length; i++)
			distSum += this.distances[i][img][object];
		
		return distSum / this.distances.length;
	}
	
	public double getProdDescDist(int img, int object){
		double distProd = 1;
		
		for (int i = 0; i < this.distances.length; i++)
			distProd *= this.distances[i][img][object];
		
		return distProd;
	}
}
