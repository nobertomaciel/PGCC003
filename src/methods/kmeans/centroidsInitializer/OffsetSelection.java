package methods.kmeans.centroidsInitializer;

import interfaces.ISelectInitialCentroids;

import java.util.ArrayList;

import object.DigitalObject;

public class OffsetSelection implements ISelectInitialCentroids {

	private int offset;

	@Override
	public ArrayList<ArrayList<DigitalObject>> inicialCentroids(ArrayList<DigitalObject> inputList, int numClusters) {
		
		//If the number of clusters is bigger the half of the collection -> Uses Random selection
		//Uses time as random seed
		if(numClusters >= inputList.size()/2){
			RandomSelection randomSelection = new RandomSelection();
			randomSelection.setRandomSeed(System.currentTimeMillis());
			return randomSelection.inicialCentroids(inputList, numClusters);
		}
		
		//System.out.println("Running Offset-based selection of initial medoids");
		offset = (int) Math.floor(inputList.size() / numClusters);
		
		ArrayList<ArrayList<DigitalObject>> clusters = new ArrayList<ArrayList<DigitalObject>>();
		
		//Creates empty clusters
		for (int j = 0; j < numClusters; j = j + 1)
			clusters.add(new ArrayList<DigitalObject>());

		clusters.get(0).add(inputList.get(0));

		for (int i = 1; i < numClusters; i++) 
			clusters.get(i).add(inputList.get(i * offset));
		
		return clusters;
	}
}