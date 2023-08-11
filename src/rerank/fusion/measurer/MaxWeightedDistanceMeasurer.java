package rerank.fusion.measurer;

import java.util.ArrayList;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class MaxWeightedDistanceMeasurer implements IDigitalObjectDistanceMeasurer{

	double distances[][][];
	ArrayList<String> imgIds;
	ArrayList<String> topic;
	
	public void setTopic(ArrayList<String> topic) {
		this.topic = topic;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		double max = 0.0;
		
		int objectIndex = imgIds.indexOf(digitalObject.getId());
		
		for(int i = 0; i < topic.size(); i++){
			double dist = getAvgDescDist(i, objectIndex);
			
			if(dist > max)
				max = dist;
		}
		
		double factor = Math.sqrt(Math.sqrt(objectIndex));
		double rankRelevance = 1-(1.0/factor);
		
		return max * rankRelevance;
		
		//Another weighing scheme
		//double w = 0.62;
		//return (w*max)+((1-w)*rankRelevance);
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
