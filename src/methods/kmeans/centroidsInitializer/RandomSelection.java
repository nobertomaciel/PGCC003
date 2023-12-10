package methods.kmeans.centroidsInitializer;

import interfaces.ISelectInitialCentroids;

import java.util.ArrayList;
import java.util.Random;

import object.DigitalObject;

public class RandomSelection implements ISelectInitialCentroids {
	
	private long randomSeed;

	@Override
	public ArrayList<ArrayList<DigitalObject>> inicialCentroids(ArrayList<DigitalObject> inputList, int numClusters) {
		
		if(numClusters > inputList.size()){
			System.err.println("ATENÇÃO: The required number of clusters is bigger than the image list. Is was reduced to the size of the image list: " + inputList.size());
			numClusters = inputList.size();
		}
		
		ArrayList<DigitalObject> list = (ArrayList<DigitalObject>) inputList.clone();
		
		Random rand = new Random();
		rand.setSeed(randomSeed);
		
		ArrayList<ArrayList<DigitalObject>> clusters = new ArrayList<ArrayList<DigitalObject>>();
		
		for (int j = 0; j < numClusters; j++) {
			
			//Selects an object to be the centroid
			int pos = rand.nextInt(list.size());
			DigitalObject selectedCentroid = list.remove(pos);
			
			//Creates a new empty cluster and inserts the selected centroid
			ArrayList<DigitalObject> newCluster = new ArrayList<DigitalObject>();
			newCluster.add(selectedCentroid);
			
			//Adds the new cluster to the cluster list
			clusters.add(newCluster);
		}
		
		return clusters;	
	}
	
	public void setRandomSeed(long seed){
		this.randomSeed = seed;
	}
}