package clusterEff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

import object.DigitalObject;

public class FMeasure {
	
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, HashMap<String, String> dGT) {
		
		ListIterator<ArrayList<DigitalObject>> iterator = clusters.listIterator();
		double fmeasure = 0;
		double fmeasureParcialSum = 0;
		int clustersSize = 0;
		
		while(iterator.hasNext()) {
			
			double fmeasureParcial, precision, recall, partitionSize;
			fmeasureParcial = precision = recall = partitionSize = 0;
			
			ArrayList<DigitalObject> cluster = iterator.next();
			HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
			int maxOccurrence = 0;
			String keyOccurrence = "";
			
			for(int i = 0; i < cluster.size(); i++) {
				int value;				
				String key = dGT.get(cluster.get(i).getId());
				
				if(key == null) {
					key = "irrel";
				}	
				if(occurrences.containsKey(key)) {
					value = occurrences.get(key);
					occurrences.put(key, value + 1);
				} else {
					occurrences.put(key, 1);
				}	
			}
			
			maxOccurrence = Collections.max(occurrences.values());
			
			for(Entry<String, Integer> entry : occurrences.entrySet()) {
				if(entry.getKey().equals("irrel"))
					continue;
					
				if(entry.getValue() == maxOccurrence) {
					keyOccurrence = entry.getKey();
				}
			}
			
			for(Entry<String, String> entry : dGT.entrySet()) {
				if(entry.getValue().equals(keyOccurrence)) {
					partitionSize++;
				}
			}
			
			if(!keyOccurrence.equals("")) {
	
				precision = (double) maxOccurrence / cluster.size();
				recall = (double) maxOccurrence / partitionSize;
				
				fmeasureParcial = (2 * precision * recall) / (precision + recall);
				
			}
			
			fmeasureParcialSum += fmeasureParcial;
			clustersSize++;
			
		}
		
		fmeasure = fmeasureParcialSum / clustersSize;
		
		return fmeasure;
	}
}