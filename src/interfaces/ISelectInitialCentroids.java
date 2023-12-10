package interfaces;

import java.util.ArrayList;

import object.DigitalObject;

public interface ISelectInitialCentroids {
	public ArrayList<ArrayList<DigitalObject>> inicialCentroids(ArrayList<DigitalObject> inputList,  int numClusters);
}