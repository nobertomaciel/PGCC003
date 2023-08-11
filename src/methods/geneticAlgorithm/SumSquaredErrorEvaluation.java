package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class SumSquaredErrorEvaluation implements ELinkage {

	@Override
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
			FeatureManager fm) {
		double sse = 0;
		for(int i = 0; i < clustering.size(); i++) {
			double distance = this.getDistance(clustering.get(i), fm);
			sse += distance;
			//System.out.println("db: "+db + "minimum: " + minimum);
		}
		
		return sse;
	}

	private double getDistance(ArrayList<DigitalObject> cluster, FeatureManager fm) {
		int indexCentroid = 0;
		double minimum = Double.MAX_VALUE;
		double centroid = 0.0;
		
		for(int i = 0; i < cluster.size(); i++) {
			for(int j = 0; j < cluster.size(); j++) {
				centroid += fm.getAvgDistance(cluster.get(i), cluster.get(j));
				//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
			}
			centroid /= cluster.size(); 
			if(centroid < minimum){
				minimum = centroid;
				indexCentroid = i;
			}
			centroid = 0;
		}
		centroid = minimum;
		double sse = 0.0;
		for(int j = 0; j < cluster.size(); j++) {
			sse += Math.pow(fm.getAvgDistance(cluster.get(indexCentroid), cluster.get(j)) - centroid,2);
			//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
		}
		return sse;
	}

	@Override
	public double runGAEvaluation(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal chromosome) {
		// TODO Auto-generated method stub
		return 0;
	}
}
