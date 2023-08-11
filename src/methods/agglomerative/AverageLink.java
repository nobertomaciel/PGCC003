package methods.agglomerative;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class AverageLink implements ILinkage {

	@Override
	public int[] findClosestPair(ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm) {
		int[] indexes = new int[2];
		double minimum = Double.MAX_VALUE;
		
		for(int i = 0; i < clustering.size() - 1; i++) {
			for(int j = i + 1; j < clustering.size(); j++) {
				double distance = getDistance(clustering.get(i), clustering.get(j), fm);
				if(distance < minimum) {
					minimum = distance;
					indexes[0] = i;
					indexes[1] = j;
				}
			}
		}
		
		return indexes;
	}

	@Override
	public double getDistance(ArrayList<DigitalObject> cluster1, ArrayList<DigitalObject> cluster2, FeatureManager fm) {
		double sumOfDistances = 0.0;
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0 ; j < cluster2.size(); j++) {
				double distance = fm.getAvgDistance(cluster1.get(i), cluster2.get(j));
				sumOfDistances += distance;
			}
		}
		
		double average = (sumOfDistances / (cluster1.size() * cluster2.size()));
		
		return average;
	}

	@Override
	public int[] findClosestPair(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal individual) {
		int[] indexes = new int[2];
		double minimum = Double.MAX_VALUE;
		
		for(int i = 0; i < clustering.size() - 1; i++) {
			for(int j = i + 1; j < clustering.size(); j++) {
				double distance = getDistance(clustering.get(i), clustering.get(j), fm, individual);
				if(distance < minimum) {
					minimum = distance;
					indexes[0] = i;
					indexes[1] = j;
				}
			}
		}
		
		return indexes;
	}

	private double getDistance(ArrayList<DigitalObject> cluster1,
			ArrayList<DigitalObject> cluster2, FeatureManager fm,
			XReal individual) {

		double sumOfDistances = 0.0;
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0 ; j < cluster2.size(); j++) {
				double distance = fm.getGADistance(cluster1.get(i), cluster2.get(j), individual);
				sumOfDistances += distance;
			}
		}
		
		double average = (sumOfDistances / (cluster1.size() * cluster2.size()));
		
		return average;
	}
}