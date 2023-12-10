package methods.chameleon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import config.FeatureManager;
import interfaces.IDiversify;
import interfaces.IFeatureManager;
import methods.clusterCommon.RepresentativeSelector;
import methods.ensemble.generation.ClusteringFileWriter;
import object.DigitalObject;

public class Chameleon implements IDiversify {
	
	private String dataset;
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private int kNeighbor;
	private int numberOfPartitions;
	private double alfa;
	private Properties configFile;

	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		
		this.fm = FeatureManager.getInstance(idTopic);
		this.originalList = new ArrayList<DigitalObject>(inputList);
		
		configFile = new Properties();
		Properties datasetConfigFile = new Properties();
		try {
			configFile.load(new FileInputStream("resources/chameleon.properties"));
			this.kNeighbor = Integer.parseInt(configFile.getProperty("K_NEAREST"));
			this.numberOfPartitions = Integer.parseInt(configFile.getProperty("PARTS"));
			this.alfa = Double.parseDouble(configFile.getProperty("ALFA"));
			
			datasetConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
			this.dataset = datasetConfigFile.getProperty("DATASET");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		HashMap<Integer, DigitalObject> mapGraph = new HashMap<Integer, DigitalObject>();
		SimpleWeightedGraph<DigitalObject, DefaultWeightedEdge> graph = 
				new SimpleWeightedGraph<DigitalObject, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		for(int i = 0; i < inputList.size(); i++) {
			graph.addVertex(inputList.get(i));
			mapGraph.put(i, inputList.get(i));
		}
		
		// Phase I: Finding Initial Sub-clusters
		ArrayList<HashMap<Integer, Long>> graphInformation = constructKNearestNeighborGraph(graph);
		ArrayList<ArrayList<DigitalObject>> clusters = partitionKNearestNeighborGraph(graphInformation, mapGraph);
		
		// Phase II: Merging Sub-clusters using a Dynamic Framework
		mergeSubClusters(clusters, graph);
		
		ClusteringFileWriter.writeClusters(clusters, this.getClass().getSimpleName().toLowerCase(), topicName);
		
		System.out.print("Clust sizes: ");
		for(ArrayList<DigitalObject> cluster : clusters) {
			System.out.print(cluster.size() + " ");
		}
		
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, this.originalList, idTopic, topicName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);
		
		return outputList;
	}
	
	public ArrayList<HashMap<Integer, Long>> constructKNearestNeighborGraph(
			SimpleWeightedGraph<DigitalObject, DefaultWeightedEdge> graph) {
		
		ArrayList<HashMap<Integer, Long>> graphInformation = new ArrayList<HashMap<Integer, Long>>();
		long numEdges = 0;
		
		for(int i = 0; i < originalList.size(); i++) {
			HashMap<Integer, Long> mapGraphFile = new HashMap<Integer, Long>();
			graphInformation.add(mapGraphFile);
		}
		
		double distance = 0.0;
	
		for(int i = 0; i < originalList.size(); i++) {			
			Map<DigitalObject, Double> nearestNeighbors = new LinkedHashMap<DigitalObject, Double>();
			
			for(Map.Entry<DigitalObject, Double> entry: nearestNeighbors.entrySet()) {
			    entry.setValue(Double.MAX_VALUE);
			}
			
			for(int j = 0; j < originalList.size(); j++) {
				
				if(i == j) {
					continue;
				} else {
					distance = fm.getAvgDistance(originalList.get(i), originalList.get(j));
					nearestNeighbors.put(originalList.get(j), distance);
				}
			}
			
			nearestNeighbors = sortByValues(nearestNeighbors);
			int x = 0;
			while(x < kNeighbor) {
				
				DigitalObject db = (DigitalObject) nearestNeighbors.keySet().toArray()[x];
				double weight = nearestNeighbors.get(db);
				
				if(weight == Double.MAX_VALUE) {
					break;
				} else {
					
					if(graph.containsEdge(originalList.get(i), db)) {
						x++;
						continue;
					} else {
						if(weight == 0)
							weight = 1;
						graph.setEdgeWeight(graph.addEdge(originalList.get(i), db), weight);
						graphInformation.get(i).put(originalList.indexOf(db)+1, Math.round(weight * 100000));
						graphInformation.get(originalList.indexOf(db)).put(i+1, Math.round(weight * 100000));
						numEdges++;
					}
				}
				x++;
			}
		}
		
		HashMap<Integer, Long> edges = new HashMap<Integer, Long>();
		edges.put(0, numEdges);
		graphInformation.add(edges);
		
		return graphInformation;
	}
	

	public ArrayList<ArrayList<DigitalObject>> partitionKNearestNeighborGraph(
			ArrayList<HashMap<Integer, Long>> graphInformation, HashMap<Integer, DigitalObject> mapGraph) {
		
		ArrayList<ArrayList<DigitalObject>> clustering = new ArrayList<ArrayList<DigitalObject>>();
		
		for(int i = 0; i < numberOfPartitions; i++) {
			ArrayList<DigitalObject> subCluster = new ArrayList<DigitalObject>();
			clustering.add(subCluster);
		}
		
		long numberOfEdges = graphInformation.get(originalList.size()).get(0);		
		File file = new File(dataset + "/resources/metis/inputFile.graph");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			System.out.println("The first line: " + originalList.size() + " " + numberOfEdges + " " + "001");
			bw.write(originalList.size() + " " + numberOfEdges + " " + "001");
			bw.newLine();
						
			for(int i = 0; i < graphInformation.size()-1; i++) {
				HashMap<Integer, Long> m = graphInformation.get(i);
				for(Map.Entry<Integer, Long> entry : m.entrySet()) {
					bw.write(entry.getKey() + " " + entry.getValue() + " ");
				}
				
				bw.newLine();
			}
			bw.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}		
		
		File f = new File(dataset + "/resources/metis/");
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "gpmetis " + "inputFile.graph " + Integer.toString(numberOfPartitions));
		builder.redirectErrorStream(true);
		builder.directory(f);
		
		try {
			
			Process p = builder.start();
			p.waitFor();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();
			
			File f2 = new File(dataset + "/resources/metis/inputFile.graph.part." + Integer.toString(numberOfPartitions));
			
			BufferedReader br = new BufferedReader(new FileReader(f2));
			String strLine;
			
			int i = 0;
			
			while((strLine = br.readLine()) != null) {
				DigitalObject d = mapGraph.get(i);
				if(d == null)
					continue;
				
				clustering.get(Integer.parseInt(strLine)).add(d);
				i++;
			}
						
			f2.delete();
			br.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		return clustering;
	}
	
	public void mergeSubClusters(ArrayList<ArrayList<DigitalObject>> subClusters,
		SimpleWeightedGraph<DigitalObject, DefaultWeightedEdge> graph) {
		
		double[] internalEC = new double[subClusters.size()]; // each subcluster has its own
		double[] internalSEC = new double[subClusters.size()]; // each subcluster has its own
				
		for(int i = 0; i < subClusters.size(); i++) {
			ArrayList<DigitalObject> subCluster = subClusters.get(i);
			SimpleWeightedGraph<DigitalObject, DefaultWeightedEdge> subGraph = 
					new SimpleWeightedGraph<DigitalObject, DefaultWeightedEdge>(DefaultWeightedEdge.class);
			
			for(int j = 0; j < subCluster.size(); j++) {
				for(int k = j + 1; k < subCluster.size(); k++) {
					 if(graph.containsEdge(subCluster.get(j), subCluster.get(k))) {
						 if(!subGraph.containsEdge(subCluster.get(j), subCluster.get(k))) {
							 subGraph.addVertex(subCluster.get(j));
							 subGraph.addVertex(subCluster.get(k));
							 subGraph.setEdgeWeight(subGraph.addEdge(subCluster.get(j), subCluster.get(k)), 
									 graph.getEdgeWeight(graph.getEdge(subCluster.get(j), subCluster.get(k))));
						 }
						 internalSEC[i] += graph.getEdgeWeight(graph.getEdge(subCluster.get(j), subCluster.get(k)));
					}
				}
			}
			
			if(subGraph.vertexSet().size() < 2) {
				internalEC[i] = 0;
			} else {
				StoerWagnerMinimumCut<DigitalObject, DefaultWeightedEdge> minimumCut =
						new StoerWagnerMinimumCut<DigitalObject, DefaultWeightedEdge>(subGraph);
				internalEC[i] = minimumCut.minCutWeight();
			}
			
			internalSEC[i] /= subCluster.size();			
		}
		
		boolean end = true;
		for(int i = 0; i < subClusters.size(); i++) {
			for(int j = i + 1; j < subClusters.size(); j++) {
				ArrayList<DigitalObject> sub1 = subClusters.get(i);
				ArrayList<DigitalObject> sub2 = subClusters.get(j);
				double absoluteEC, absoluteSEC, RI, RC;
				absoluteEC = absoluteSEC = RI = RC = 0.0;
				
				for(int k = 0; k < sub1.size(); k++) {
					for(int l = 0; l < sub2.size(); l++) {
						if(graph.containsEdge(sub1.get(k), sub2.get(l))) {
							absoluteEC += graph.getEdgeWeight(graph.getEdge(sub1.get(k), sub2.get(l)));
						}
					}
				}
				
				absoluteSEC = absoluteEC / (sub1.size() + sub2.size());
				if(Double.isNaN(absoluteSEC)) {
					absoluteSEC = 0.0;
				}
				
				if(internalEC[i] != 0 || internalEC[j] != 0) {
					RI = Math.abs(absoluteEC) / ((Math.abs(internalEC[i]) + Math.abs(internalEC[j])) / 2);
				} else {
					RI = Math.abs(absoluteEC) / 0.5;
				}
				
				if(absoluteSEC != 0)
					RC = absoluteSEC / ((sub1.size() / sub1.size() + sub2.size()) * internalSEC[i]) +
							((sub2.size() / sub1.size() + sub2.size()) * internalSEC[j]);
						
				if((RI * RC) / 2 < alfa) {
					agglomerateSubClusters(i, j, subClusters);
					end = false;
					break;
				}
			}
		}
		
		if(!end) {
			mergeSubClusters(subClusters, graph);
		}		
	}
	
	public void agglomerateSubClusters(int i, int j, ArrayList<ArrayList<DigitalObject>> subClusters) {
		Iterator<DigitalObject> iterator = subClusters.get(i).iterator();
		while(iterator.hasNext()) {
			subClusters.get(j).add(iterator.next());
		}
		subClusters.remove(i);
	}
	
	@SuppressWarnings("rawtypes")
	public static <K extends Comparable, V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
      
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

            @SuppressWarnings("unchecked")
			@Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
      
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
    }
}