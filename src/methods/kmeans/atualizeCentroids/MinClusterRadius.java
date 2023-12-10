package methods.kmeans.atualizeCentroids;

import interfaces.IAtualizeCentroids;

import java.util.ArrayList;

import object.DigitalObject;
import config.FeatureManager;

/**
 * Seleciona novos centróides com base na menor distância máxima
 * de um elemento para todos os outros.
 */

public class MinClusterRadius implements IAtualizeCentroids {
	public static boolean centroidUpdated = false;

	@Override
	public ArrayList<ArrayList<DigitalObject>> updateCentroids(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {
		
		System.out.println("Updating centroids by conectivity...");
		centroidUpdated = false;

		for(int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++){
			DigitalObject centroid = clusters.get(clusterIndex).get(0);

			//Computes current centroid's minMaxDist
			double maxDist = Double.NEGATIVE_INFINITY;
			int newCentroidIndex = 0;
			for (int j = 0; j < clusters.get(clusterIndex).size(); j++)
				maxDist = Math.max(maxDist, fm.getAvgDistance(centroid, clusters.get(clusterIndex).get(j)));
			
			//Computes current centroid's max connectivity and defines it as the best one (the smallest)
			double minMaxDist = maxDist;
			
			//Computes the max connectivity of the other centroids			
			maxDist = Double.NEGATIVE_INFINITY;
			for (int clusterObjIndex = 1; clusterObjIndex < clusters.get(clusterIndex).size(); clusterObjIndex++) {
				DigitalObject candidate = clusters.get(clusterIndex).get(clusterObjIndex);

				for (int s = 0; s < clusters.get(clusterIndex).size(); s++)
					maxDist = Math.max(maxDist, fm.getAvgDistance(candidate, clusters.get(clusterIndex).get(s)));

				if (maxDist < minMaxDist) {
					minMaxDist = maxDist;
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