package clusterEff;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Collections;

import object.DigitalObject;

public class Purity {
	
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, HashMap<String, String> dGT) {
		
		ListIterator<ArrayList<DigitalObject>> iterator = clusters.listIterator();
		double purity = 0;
		int intersections = 0;
		int numberOfPoints = 0;
		
		//System.out.print("Clust purities: ");
		
		while(iterator.hasNext()) {
			ArrayList<DigitalObject> cluster = iterator.next();
			HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
			int maxOccurrence = 0;

			for(int i = 0; i < cluster.size(); i++) {
				int value;
				String key = dGT.get(cluster.get(i).getId());
				
				if(key == null)
					continue;
				
				if(occurrences.containsKey(key)) {
					value = occurrences.get(key);
					occurrences.put(key, value + 1);
				} else {
					occurrences.put(key, 1);
				}				
			}
			
			if(!occurrences.isEmpty()) {
				maxOccurrence = Collections.max(occurrences.values());
				intersections += maxOccurrence;
			} else {
				intersections += 0;
			}
			
			numberOfPoints += cluster.size();
			
			//System.out.print(((double)maxOccurrence / cluster.size()) + " ");
		}
		
		purity = (double) intersections / numberOfPoints;
		
		return purity;
	}
}
