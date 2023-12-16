package methods.clusterCommon;

import interfaces.IFeatureManager;
import interfaces.ISelectListFinal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import methods.kmedoids.outputGenerator.PositionMeasurer;
import methods.kmedoids.outputGenerator.RoundRobinSelector;
import object.DigitalObject;
import clusterEff.ClusteringEvaluation;
import config.FeatureManager;

public class RepresentativeSelector {
	
	private int OUTPUT_GENERATOR;
	private int INTRACLUSTER_SORTING_METHOD;
	private int INTERCLUSTER_SORTING_METHOD;
	private boolean RUN_CLUSTER_EVALUATION;
	
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private int idTopic;
	
	public RepresentativeSelector(IFeatureManager fm, ArrayList<DigitalObject> originalList, int idTopic, String topicName){
		this.originalList = originalList;
		this.fm = FeatureManager.getInstance(idTopic);
		this.idTopic = idTopic;
		
		Properties configFile = new Properties();
		try {
			configFile.load(new FileInputStream("resources/selector.properties"));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	
		this.RUN_CLUSTER_EVALUATION = Boolean.parseBoolean(configFile.getProperty("RUN_CLUSTER_EVALUATION"));
		this.INTRACLUSTER_SORTING_METHOD = Integer.parseInt(configFile.getProperty("INTRACLUSTER_SORTING_METHOD"));
		this.INTERCLUSTER_SORTING_METHOD = Integer.parseInt(configFile.getProperty("INTERCLUSTER_SORTING_METHOD"));
		this.OUTPUT_GENERATOR = Integer.parseInt(configFile.getProperty("OUTPUT_GENEREATOR"));
		
	}
	
	public ArrayList<DigitalObject> run(ArrayList<ArrayList<DigitalObject>> clusters){
		
		//RUNS CLUSTER EVALUATION
		if(this.RUN_CLUSTER_EVALUATION) {
			ClusteringEvaluation evaluation = new ClusteringEvaluation(this.idTopic);
			evaluation.evaluateClustering(clusters);
			evaluation.generateSheet();
		}
		
		//INTRACLUSTER SORTING
		switch (this.INTRACLUSTER_SORTING_METHOD) {
		case 0: break; //No intracluster sorting
		case 1: 
			clusters = intraSortByRelevance(clusters, this.originalList);
			break;
			
		case 2:
			clusters = intraSortByCentroidKNN(clusters);
			break;			
		case 3:
			clusters = intraSortByAverageConectivity(clusters);
			break;
		default:
			try {
				throw new Exception("Método " + INTRACLUSTER_SORTING_METHOD + " para ordenanção INTRACLUSTER não existe!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		
		//INTERCLUSTER SORTING
		switch (this.INTERCLUSTER_SORTING_METHOD) {
			case 0: break; //No intercluster sorting
			case 1: clusters = interSortClustersByAverageRelevance(clusters); break;
			case 2:	clusters = interSortClustersBySize(clusters); break;				
			case 3: clusters = interSortClustersByMaxDissimilarity(clusters); break;
				
			default:
				try {
					throw new Exception("Método " + INTERCLUSTER_SORTING_METHOD + " para ordenanção INTERCLUSTER não existe!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}		

		ArrayList<DigitalObject> selection = null;

		//REPRESENTATIVE SELECTION
		switch (this.OUTPUT_GENERATOR) {
		case 1:
			ISelectListFinal roundRobinSelector = new RoundRobinSelector();
			selection = roundRobinSelector.selectElements(clusters, this.originalList, fm);
			break;

		default:
			try {
				throw new Exception("Método " + OUTPUT_GENERATOR + " para construção de lista final não existe!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(1);
		}

		return selection;		
	}
	
	private ArrayList<ArrayList<DigitalObject>> interSortClustersBySize(ArrayList<ArrayList<DigitalObject>> clusters){
		
		for(int i = 0; i < clusters.size(); i++){
			ArrayList<DigitalObject> c1 = clusters.get(i);
			int biggest = i;
			
			for(int j = i+1; j < clusters.size(); j++){
				ArrayList<DigitalObject> c2 = clusters.get(j);
				
				if(c2.size() > c1.size()){
					biggest = j;
					c1 = c2;
				}
			}
			
			clusters.add(i, clusters.remove(biggest));
		}
		
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> interSortClustersByAverageRelevance(ArrayList<ArrayList<DigitalObject>> clusters){
		
		float clustRel[] = new float[clusters.size()];
		for (int i = 0; i < clusters.size(); i++) {
			ArrayList<DigitalObject> cluster = clusters.get(i);
			clustRel[i] = 0;
			
			for (int j = 0; j < cluster.size(); j++) {
				int objRel = this.originalList.indexOf(cluster.get(j));
				clustRel[i] += objRel;
			}
			
			clustRel[i] /= cluster.size();
		}
		
		for(int i = 0; i < clusters.size(); i++){
			int best = i;
			for(int j = i+1; j < clusters.size(); j++){
				if(clustRel[j] > clustRel[best]){
					best = j;
				}
			}
			
			float aux = clustRel[i];
			clustRel[i] = clustRel[best];
			clustRel[best] = aux;
			
			clusters.add(i, clusters.remove(best));
			
		}
		
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> interSortClustersByMaxDissimilarity(ArrayList<ArrayList<DigitalObject>> clusters){
		ArrayList<ArrayList<DigitalObject>> newClusters = new ArrayList<ArrayList<DigitalObject>>();
		
		clusters = interSortClustersByAverageRelevance(clusters);
		newClusters.add(clusters.remove(0));
		
		while(clusters.size() > 0){
			double maxDist = Double.NEGATIVE_INFINITY;
			int farthestClusterIndex = -1;
			
			for (int i = 0; i < clusters.size(); i++) {
				
				double clustDist = Double.MAX_VALUE;
				for (ArrayList<DigitalObject> newC : newClusters)
					clustDist = Math.min(clustDist, computeClustersDistance(clusters.get(i), newC));
				
				if(clustDist > maxDist){
					maxDist = clustDist;
					farthestClusterIndex = i;
				}
			}	
			
			newClusters.add(clusters.remove(farthestClusterIndex));
		}

		System.out.println("new num clust: " + newClusters.size());
		return newClusters;
	}
	
	private double computeClustersDistance(ArrayList<DigitalObject> cluster1, ArrayList<DigitalObject>cluster2){
		double minimum = Double.MAX_VALUE;		 
		minimum = fm.getAvgDistance(cluster1.get(0), cluster2.get(0));
		
		return minimum;
	}
	
	private ArrayList<ArrayList<DigitalObject>> intraSortByRelevance(ArrayList<ArrayList<DigitalObject>> listaClusters, ArrayList<DigitalObject> inputList){
		
		ArrayList<String> listIDs = new ArrayList<String>();
		for (DigitalObject obj : inputList)
			listIDs.add(obj.getId());
		
		PositionMeasurer measurer = new PositionMeasurer(listIDs);
		for (int i = 0; i < listaClusters.size(); i++) {
			ArrayList<DigitalObject> cluster = listaClusters.get(i);
			
			//Sets measurer
			for(int j = 0; j < cluster.size(); j++)
				cluster.get(j).setMeasurer(measurer);
			
			Collections.sort(cluster);
		}
		
		return listaClusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> intraSortByCentroidKNN(ArrayList<ArrayList<DigitalObject>> clusters)
	{			
		for(int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++){
			ArrayList<DigitalObject> c = clusters.get(clusterIndex);
			
			//Sorts cluster i
			for(int objIndex = 1; objIndex < c.size(); objIndex++){
				
				//Sets the first element as the closest to the centroid and its distance as the minimum
				int closest = objIndex;
				double minDistToCentroid = this.fm.getAvgDistance(c.get(0), c.get(objIndex));
				
				//Checks if any other objects is closer than the selected one
				for(int candidateIndex = objIndex+1; candidateIndex < c.size(); candidateIndex++)
				{
					double candidateDistToCentroid = this.fm.getAvgDistance(c.get(0), c.get(candidateIndex));
					
					if (candidateDistToCentroid < minDistToCentroid){
						minDistToCentroid = candidateDistToCentroid;
						closest = candidateIndex;
					}
				}
				
				c.add(objIndex, c.remove(closest));
			}
		}
			
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> intraSortByAverageConectivity(ArrayList<ArrayList<DigitalObject>> clusters)
	{	
		for(int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++){
			ArrayList<DigitalObject> c = clusters.get(clusterIndex);
			
			double conectivity[] = new double[c.size()];
			
			for(int i = 0; i < conectivity.length; i++){
				DigitalObject o1 = c.get(i); 
				conectivity[i] = 0;
				for (int j = 0; j < conectivity.length; j++) {
					conectivity[i] += fm.getAvgDistance(o1, c.get(j));
				}
				
				conectivity[i] /= conectivity.length;
			}
			
			//Sorts cluster i
			for(int objIndex = 1; objIndex < c.size(); objIndex++){
				
				//Sets the first element as the closest to the centroid and its distance as the minimum
				int closest = objIndex;
				double minConectivity = conectivity[closest];
				
				//Checks if any other objects is closer than the selected one
				for(int candidateIndex = objIndex+1; candidateIndex < c.size(); candidateIndex++)
				{
					double candidateConectivity = conectivity[candidateIndex];
					
					if (candidateConectivity < minConectivity){
						minConectivity = candidateConectivity;
						closest = candidateIndex;
					}
				}
				
				c.add(objIndex, c.remove(closest));
			}
		}
			
		return clusters;
	}
	
	private void printClustersSize(ArrayList<ArrayList<DigitalObject>> clusters){
		
		int totalNumObjects = 0;
		
		for (int i = 0; i < clusters.size(); i++){
			int clusterSize = clusters.get(i).size();
			System.out.println("cluster " + i + ": " + clusterSize + " imgs");
			totalNumObjects += clusterSize;
		}
		System.out.println("Total num objects: " + totalNumObjects);
		System.out.println();
	}
}