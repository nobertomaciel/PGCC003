package methods.agglomerative;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class CentroidMethod implements ILinkage {

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
		double sumOfDistances = 0.0;
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
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0; j < cluster2.size(); j++) {
				sumOfDistances += fm.getAvgDistance(cluster1.get(i), cluster2.get(j));
			}
		}
		
		double average = (sumOfDistances / (cluster1.size() * cluster2.size()));
		double centroid1 = (sumOfDistances1 / (2 * Math.pow(cluster1.size(), 2)));
		double centroid2 = (sumOfDistances2 / (2 * Math.pow(cluster2.size(), 2)));
		
		return average - centroid1 - centroid2;
	}

	@Override
	public int[] findClosestPair(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm, XReal individual) {
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
	
	public double getDistance(ArrayList<DigitalObject> cluster1,
			ArrayList<DigitalObject> cluster2, FeatureManager fm, XReal individual) {
		
		int indexCentroid1 = 0;
		int indexCentroid2 = 0;
		double minimum = Double.MAX_VALUE;
		double centroid1 = 0.0;
		double centroid2 = 0.0;
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0; j < cluster1.size(); j++) {
				centroid1 += fm.getGADistance(cluster1.get(i), cluster1.get(j), individual);
				//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
			}
			centroid1 /= cluster1.size(); 
			if(centroid1 < minimum){
				minimum = centroid1;
				indexCentroid1 = i;
			}
			centroid1 = 0;
		}
		//System.out.println(sumOfDistances1);
		minimum = Double.MAX_VALUE;
		for(int i = 0; i < cluster2.size(); i++) {
			for(int j = 0; j < cluster2.size(); j++) {
				centroid2 += fm.getGADistance(cluster2.get(i), cluster2.get(j), individual);
			}
			centroid2 /= cluster2.size(); 
			if(centroid2 < minimum){
				minimum = centroid2;
				indexCentroid2 = i;
			}
			centroid2 = 0;
		}
		
		double distanceCentroids = fm.getGADistance(cluster1.get(indexCentroid1), cluster2.get(indexCentroid2), individual);
		return distanceCentroids;
		
		/*double sumOfDistances = 0.0;
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
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0; j < cluster2.size(); j++) {
				sumOfDistances += fm.getGADistance(cluster1.get(i), cluster2.get(j), individual);
			}
		}
		
		double average = (sumOfDistances / (cluster1.size() * cluster2.size()));
		double centroid1 = (sumOfDistances1 / (2 * Math.pow(cluster1.size(), 2)));
		double centroid2 = (sumOfDistances2 / (2 * Math.pow(cluster2.size(), 2)));
		
		return average - centroid1 - centroid2;*/
	}
}