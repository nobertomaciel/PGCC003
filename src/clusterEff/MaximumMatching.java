package clusterEff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ListIterator;
import java.util.Set;
import java.util.HashSet;

import object.DigitalObject;

public class MaximumMatching {
	
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, HashMap<String, String> dGT) {
		
		ListIterator<ArrayList<DigitalObject>> iterator = clusters.listIterator();
		Set<String> filteredOccurrences = new HashSet<String>();
		double match = 0;
		int intersections = 0;
		int numberOfPoints = 0;
		
		while(iterator.hasNext()) {
			ArrayList<DigitalObject> cluster = iterator.next();
			HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
			int maxOccurrence = 0;
			String keyOccurrence = "";
			int valueOccurrence = 0;
			
			for(int i = 0; i < cluster.size(); i++) {
				int value;
				String key = dGT.get(cluster.get(i).getId());
				
				if(key == null)
					continue;
				
				if(occurrences.containsKey(key)) {
					value = occurrences.get(key);
					occurrences.put(key, value + 1);
					//filteredOccurrences.add(key);
				} else {
					occurrences.put(key, 1);
				}

			}
			
			do {
				
				valueOccurrence = 0;
				String removeKey = "";
				
				/*
				System.out.println();
				for(String e : filteredOccurrences) {
					System.out.print(" " + e);
				}
				System.out.println();
				*/
				
				if(occurrences.isEmpty()) {
					break;
				} else {
					maxOccurrence = Collections.max(occurrences.values());
					
					for(Entry<String, Integer> entry : occurrences.entrySet()) {
						//System.out.println("key, value: " + entry.getKey() + " " + entry.getValue());
						
						if(entry.getValue() == maxOccurrence) {
							
							keyOccurrence = entry.getKey();
							//System.out.println("Key Occurrence: " + keyOccurrence);
							
							if(filteredOccurrences.contains(keyOccurrence)) {
								removeKey = keyOccurrence;
								valueOccurrence = occurrences.get(keyOccurrence);
								break;
							} else {
								//System.out.println("Adding Key " + keyOccurrence+ " To Filtered Ocurrences");
								filteredOccurrences.add(keyOccurrence);
								break;
							}
						}
					}
					
					if(!removeKey.equals("")) {
						//System.out.println("Removing Key From Occurrences: " + removeKey);
						occurrences.remove(removeKey);
					}
				}
				
			} while(valueOccurrence != 0);
			
			
			if(!occurrences.isEmpty()) {
				intersections += maxOccurrence;
				//System.out.println("Max. Occurrence: " + maxOccurrence);
			}	
			else {
				intersections += 0;
				//System.out.println("Max. Occurrence: " + 0);
			}
			numberOfPoints += cluster.size();
			
			//System.out.println("\n");
			//System.out.println(keyOccurrence);
			//System.out.println("First match: " + (double) maxOccurrence / cluster.size());
		}
		
		//System.out.println("Total intersection: " + intersections);
		//System.out.println("Number Of Points: " + numberOfPoints);
		
		match = (double) intersections / numberOfPoints;
		
		return match;
	}

}
