package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class SingleLinkEvaluation implements ELinkage{

	
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm) {
		double minimum = Double.MAX_VALUE;
		double singleLinkAvg = 0;
	
		for(int i = 0; i < clustering.size()-1; i++) {
			minimum = Double.MAX_VALUE;
			for(int j = i+1; j < clustering.size(); j++) {
			
				double distance = this.getDistance(clustering.get(i), clustering.get(j), fm);	
				if(distance < minimum) {
					minimum = distance;
				}
							
			}
			
			singleLinkAvg = (singleLinkAvg +  minimum);
			//System.out.println("i: " +i + " singleavg "+singleLinkAvg + " minimum: " + minimum);
			
		}
		//singleLinkAvg = avg.doubleValue();
		return (singleLinkAvg/clustering.size());
	

	}

	
	private double getDistance(ArrayList<DigitalObject> cluster1,
			ArrayList<DigitalObject> cluster2, FeatureManager fm) {
		
		double minimum = Double.MAX_VALUE;
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0 ; j < cluster2.size(); j++) {
				
				double distance = fm.getAvgDistance(cluster1.get(i), cluster2.get(j));
				if(distance < minimum) {
					minimum = distance;
				}
				
			}
		}
		return minimum;
	}


	@Override
	public double runGAEvaluation(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal chromosome) {
		// TODO Auto-generated method stub
		return 0;
	}
}
