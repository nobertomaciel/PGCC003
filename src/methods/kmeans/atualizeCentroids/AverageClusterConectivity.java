package methods.kmeans.atualizeCentroids;

import interfaces.IAtualizeCentroids;

import java.util.ArrayList;

import object.DigitalObject;
import config.FeatureManager;

public class AverageClusterConectivity implements IAtualizeCentroids {
	public static boolean centroidUpdated = false;

	@Override
	public ArrayList<ArrayList<DigitalObject>> updateCentroids(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {
		
		centroidUpdated = false;

		for(int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++){
			DigitalObject centroid = clusters.get(clusterIndex).get(0);

			//Computes current centroid's connectivity
			double objectDistance = 0;
			int newCentroidIndex = 0;
			for (int j = 0; j < clusters.get(clusterIndex).size(); j++)
				objectDistance = objectDistance + fm.getAvgDistance(centroid, clusters.get(clusterIndex).get(j));
			
			//Computes current centroid's average connectivity and defines it as the best one (the smallest)
			double bestConectivity = objectDistance / clusters.get(clusterIndex).size();
			
			for (int clusterObjIndex = 1; clusterObjIndex < clusters.get(clusterIndex).size(); clusterObjIndex++) {
				DigitalObject candidate = clusters.get(clusterIndex).get(clusterObjIndex);
				
				objectDistance = 0;
				for (int s = 0; s < clusters.get(clusterIndex).size(); s++)
					objectDistance = objectDistance + fm.getAvgDistance(candidate, clusters.get(clusterIndex).get(s));
				
				double candidateConectivity = objectDistance / clusters.get(clusterIndex).size(); 

				if (candidateConectivity < bestConectivity) {
					bestConectivity = candidateConectivity;
					newCentroidIndex = clusterObjIndex;
					centroidUpdated = true;
				}
			}
			
			//Removes the new centroid from its position adding it to the first posistion
			DigitalObject newCentroid = clusters.get(clusterIndex).remove(newCentroidIndex);
			clusters.get(clusterIndex).add(0, newCentroid);
		}
		
		return clusters;
	}
}