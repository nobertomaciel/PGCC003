package methods.geneticAlgorithm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import config.FeatureManager;
import interfaces.IAtualizeCentroids;
import interfaces.IDiversify;
import interfaces.IFeatureManager;
import interfaces.ISelectInitialCentroids;
import jmetal.util.wrapper.XReal;
import methods.kmedoids.atualizeCentroids.AverageClusterConectivity;
import methods.kmedoids.atualizeCentroids.MinClusterRadius;
import methods.kmedoids.centroidsInitializer.OffsetSelection;
import methods.kmedoids.centroidsInitializer.RandomSelection;
import object.DigitalObject;

public class KMedoidsMethod implements IDiversify {
	private ArrayList<DigitalObject> inputList;
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private int NUM_CLUSTERS;
	private int CENTROIDS_INITIALIZER;
	private int CENTROIDS_UPDATE_METHOD;
	private int MAX_ITERATIONS;
	private Properties configFile;
	private int totNumIterations;
	
	public ArrayList<ArrayList<DigitalObject>> run(IFeatureManager fm, ArrayList<DigitalObject> inputList, int idLocal, String locName, XReal individual) {
		
		this.fm = (FeatureManager) fm;
		//System.out.println("Executando o KMeans para o local " + idLocal);
		
		configFile = new Properties();
		
		try {
			configFile.load(new FileInputStream("resources/GAConfigFile.properties"));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		//this.NUM_CLUSTERS = Integer.parseInt(configFile.getProperty("K_CLUSTERS"));
		this.CENTROIDS_INITIALIZER = Integer.parseInt(configFile.getProperty("CENTROIDS_INITIALIZER"));
		this.CENTROIDS_UPDATE_METHOD = Integer.parseInt(configFile.getProperty("CENTROIDS_UPDATE_METHOD"));
		this.MAX_ITERATIONS = Integer.parseInt(configFile.getProperty("MAX_ITERATIONS"));
		this.inputList = new ArrayList<DigitalObject>(inputList);
		this.originalList = new ArrayList<DigitalObject>(inputList);
		this.totNumIterations = 0;

		System.out.println("NUM_CLUSTERS KMedoidsMethod.java: "+NUM_CLUSTERS);

		ArrayList<ArrayList<DigitalObject>> clusters = selectInitialMedoids();
		clusters = runClustering(clusters, individual);
		
		//ClusteringFileWriter.writeClusters(clusters, this.getClass().getSimpleName().toLowerCase(), locName);
		
		//System.out.println("# clusters: " + clusters.size());		
		
		//Select representative images
		//RepresentativeSelector selector = new RepresentativeSelector(this.fm, this.originalList, idLocal, locName);
		//ArrayList<DigitalObject> outputList = selector.run(clusters);
		
		return clusters;
	}
	/*
	public ArrayList<ArrayList<DigitalObject>> runForEnsemble() {
		ArrayList<ArrayList<DigitalObject>> clusters = selectInitialMedoids();
		clusters = runClustering(clusters);
		return clusters;
	}
	*/
	private ArrayList<ArrayList<DigitalObject>> selectInitialMedoids() {
		
		ArrayList<ArrayList<DigitalObject>> clusters = null;

		switch (this.CENTROIDS_INITIALIZER) {
		case 1:
			ISelectInitialCentroids offsetSelection = new OffsetSelection();
			clusters = offsetSelection.inicialCentroids(inputList, NUM_CLUSTERS);
			break;
		case 2:
			RandomSelection randomSelection = new RandomSelection();
			randomSelection.setRandomSeed(fm.getRandomSeed());
			clusters = randomSelection.inicialCentroids(inputList, NUM_CLUSTERS);
			break;

		default:
			try {
				throw new Exception("Arquivo mal configurado!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> runClustering(ArrayList<ArrayList<DigitalObject>> clusters, XReal individual) {

		totNumIterations++;
		//System.out.println("Running Kmeans iteration " + totNumIterations);
		//System.out.println("Selecting objects' centroids.");
		
		ArrayList<DigitalObject> centroidsList = new ArrayList<DigitalObject>();
		
		//Clean-up clusters before updating objects' clusters
		for(int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++){
			
			//Saves the new centroid object and add to the new cluster
			DigitalObject newCentroid = clusters.get(clusterIndex).remove(0);			
						
			//Creates an empty cluster
			ArrayList<DigitalObject> newCluster = new ArrayList<DigitalObject>();			
			
			//Adds the new centroid to the new cluster and to the centroids list
			newCluster.add(newCentroid);
			centroidsList.add(newCentroid);
		
			//Removes original cluster
			clusters.remove(clusterIndex);
		
			//Adds the new cluster
			clusters.add(clusterIndex, newCluster);
		}

		inputList = (ArrayList<DigitalObject>) originalList.clone();
		//For each image in the list, define its corresponding centroid
		while (!inputList.isEmpty()) {

			//Gets the next element in the list
			DigitalObject image = inputList.remove(0);
			
			//If it is not a centroid
			if(!centroidsList.contains(image)){
				
				//Computes distance to the centroid of the first cluster
				double minDistToCentroid = getDistance(image, clusters.get(0).get(0), individual);
				int imageClusterIndex = 0;
	
				// For each cluster
				for(int clusterIndex = 1; clusterIndex < NUM_CLUSTERS; clusterIndex++) {
						double distToCentroid = getDistance(image, clusters.get(clusterIndex).get(0), individual);
	
						if (distToCentroid < minDistToCentroid){
							minDistToCentroid = distToCentroid;
							imageClusterIndex = clusterIndex;
						}
				}
				clusters.get(imageClusterIndex).add(image);
			}
		}
		
		//Updating Centroids
		switch (this.CENTROIDS_UPDATE_METHOD) {
		
		case 1:
			IAtualizeCentroids atualizeNN = new AverageClusterConectivity();
			clusters = atualizeNN.updateCentroids(clusters, this.fm);
			if (AverageClusterConectivity.centroidUpdated && totNumIterations < this.MAX_ITERATIONS)
				clusters = runClustering(clusters, individual);
			break;
		
		case 2:
			IAtualizeCentroids atualizeMinMaxPath = new MinClusterRadius();
			clusters = atualizeMinMaxPath.updateCentroids(clusters, this.fm);
			if (MinClusterRadius.centroidUpdated && totNumIterations < this.MAX_ITERATIONS)
				clusters = runClustering(clusters, individual);
			break;
		
		default:
			try {
				throw new Exception("Invalid centroid updating method!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(1);
		}
		
		return clusters;
	}

	public void setNUM_CLUSTERS(int k){
		this.NUM_CLUSTERS = k;
	}

	public double getDistance(DigitalObject obj1, DigitalObject obj2, XReal individual) {
		return this.fm.getGADistance(obj1, obj2, individual);
	}
	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idLocal, String locName) {
		// TODO Auto-generated method stub
		return null;
	}
}