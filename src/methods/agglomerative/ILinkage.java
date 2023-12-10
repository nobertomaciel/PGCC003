package methods.agglomerative;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public interface ILinkage {
	
	public int[] findClosestPair(ArrayList<ArrayList<DigitalObject>> clustering, 
			FeatureManager fm);
	
	double getDistance(ArrayList<DigitalObject> cluster1, ArrayList<DigitalObject> cluster2, 
			FeatureManager fm);

	public int[] findClosestPair(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal individual);
}