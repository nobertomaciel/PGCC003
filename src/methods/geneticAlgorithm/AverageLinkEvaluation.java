package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class AverageLinkEvaluation implements ELinkage {

	@Override
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
			FeatureManager fm) {
		double minimum = Double.MAX_VALUE;
		double average = 0;
		for(int i = 0; i < clustering.size() - 1; i++) {
			minimum = Double.MAX_VALUE;
			for(int j = i + 1; j < clustering.size(); j++) {
				double distance = getDistance(clustering.get(i), clustering.get(j), fm);
				if(distance < minimum) {
					minimum = distance;
				}
			}
			average += minimum;
		}
		
		return average/clustering.size();
	}

	private double getDistance(ArrayList<DigitalObject> cluster1,
			ArrayList<DigitalObject> cluster2, FeatureManager fm) {
		
		double sumOfDistances = 0;
		
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
	public double runGAEvaluation(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal chromosome) {
		// TODO Auto-generated method stub
		return 0;
	}

}
