package interfaces;

import java.util.ArrayList;

import object.DigitalObject;
import config.FeatureManager;

public interface IAtualizeCentroids {
	public ArrayList<ArrayList<DigitalObject>> updateCentroids(ArrayList<ArrayList<DigitalObject>> listClusters, FeatureManager fm);
}