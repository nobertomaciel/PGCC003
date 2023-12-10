package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class WardsEvaluation implements ELinkage {

	@Override
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
			FeatureManager fm) {
		double minimum = Double.MAX_VALUE;
		for(int i = 0; i < clustering.size() - 1; i++) {
			for(int j = i + 1; j < clustering.size(); j++) {
				double distance = getDistance(clustering.get(i), clustering.get(j), fm);
				
				if(distance < minimum) {
					minimum = distance;
				}
			}
		}
		
		return minimum/clustering.size();
	}

	
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
		double centroid1 = (sumOfDistances1 / ( Math.pow(cluster1.size(), 2)));
		double centroid2 = (sumOfDistances2 / ( Math.pow(cluster2.size(), 2)));
		
		return weight * Math.pow((centroid1 - centroid2), 2);
	}


	@Override
	public double runGAEvaluation(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal chromosome) {
		// TODO Auto-generated method stub
		return 0;
	}

}
