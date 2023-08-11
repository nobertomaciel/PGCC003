package rerank.fusion.borda;

import java.util.ArrayList;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class SingleDistMeasurer implements IDigitalObjectDistanceMeasurer{

	double distances[][][];
	ArrayList<String> imgIds;
	ArrayList<String> topic;
	int descriptorIndex;
	
	public void setDescriptorIndex(int descIndex){
		this.descriptorIndex = descIndex;
	}
	
	public void setTopic(ArrayList<String> topic) {
		this.topic = topic;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		double min = Double.MAX_VALUE;
		
		for(int i = 0; i < topic.size(); i++){
			double dist = this.distances[this.descriptorIndex][i][imgIds.indexOf(digitalObject.getId())];
			
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
}
