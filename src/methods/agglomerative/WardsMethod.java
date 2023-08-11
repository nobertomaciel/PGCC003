package methods.agglomerative;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class WardsMethod implements ILinkage {

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
	public double getDistance(ArrayList<DigitalObject> cluster1,
			ArrayList<DigitalObject> cluster2, FeatureManager fm) {
		double sumOfDistances1 = 0.0;
		double sumOfDistances2 = 0.0;
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0; j < cluster1.size(); j++) {
				sumOfDistances1 += fm.getAvgDistance(cluster1.get(i), cluster1.get(j));
			}
		}
		
		for(int i = 0; i < cluster2.size(); i++) {
			for(int j = 0; j < cluster2.size(); j++) {
				sumOfDistances2 += fm.getAvgDistance(cluster2.get(i), cluster2.get(j));
			}
		}
		
		double weight = (cluster1.size() * cluster2.size()) / (cluster1.size() + cluster2.size());
		double centroid1 = (sumOfDistances1 / (2 * Math.pow(cluster1.size(), 2)));
		double centroid2 = (sumOfDistances2 / (2 * Math.pow(cluster2.size(), 2)));
		
		return weight * Math.pow((centroid1 - centroid2), 2);
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
		double sumOfDistances1 = 0.0;
		double sumOfDistances2 = 0.0;
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0; j < cluster1.size(); j++) {
				sumOfDistances1 += fm.getGADistance(cluster1.get(i), cluster1.get(j), individual);
			}
		}
		
		for(int i = 0; i < cluster2.size(); i++) {
			for(int j = 0; j < cluster2.size(); j++) {
				sumOfDistances2 += fm.getGADistance(cluster2.get(i), cluster2.get(j), individual);
			}
		}
		
		double weight = (cluster1.size() * cluster2.size()) / (cluster1.size() + cluster2.size());
		double centroid1 = (sumOfDistances1 / ( Math.pow(cluster1.size(), 2)));
		double centroid2 = (sumOfDistances2 / ( Math.pow(cluster2.size(), 2)));
		
		return weight * Math.pow((centroid1 - centroid2), 2);
	}	
}