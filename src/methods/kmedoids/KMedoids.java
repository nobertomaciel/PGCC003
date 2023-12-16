package methods.kmedoids;

import interfaces.IAtualizeCentroids;
import interfaces.IDiversify;
import interfaces.IFeatureManager;
import interfaces.ISelectInitialCentroids;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import methods.clusterCommon.RepresentativeSelector;
import methods.kmedoids.atualizeCentroids.AverageClusterConectivity;
import methods.kmedoids.atualizeCentroids.MinClusterRadius;
import methods.kmedoids.centroidsInitializer.OffsetSelection;
import methods.kmedoids.centroidsInitializer.RandomSelection;
import object.DigitalObject;
import config.FeatureManager;

public class KMedoids implements IDiversify {
	private ArrayList<DigitalObject> inputList;
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private int NUM_CLUSTERS;
	private int CENTROIDS_INITIALIZER;
	private int CENTROIDS_UPDATE_METHOD;
	private int MAX_ITERATIONS;
	private Properties configFile;
	private int totNumIterations;
	private long[] timeExecution = new long[151];
	private int timerDivisor = 1;

	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
	//public ArrayList<ArrayList<DigitalObject>> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		this.fm = FeatureManager.getInstance(idTopic);
		System.out.println("Run1.....: Executando o KMeans para o topico " + idTopic);
		
		configFile = new Properties();
		
		try {
			configFile.load(new FileInputStream("resources/kmedoids.properties"));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		//this.NUM_CLUSTERS = Integer.parseInt(configFile.getProperty("NUM_CLUSTERS")); //esta linha estava fixando o número de clusters
		this.CENTROIDS_INITIALIZER = Integer.parseInt(configFile.getProperty("CENTROIDS_INITIALIZER"));
		this.CENTROIDS_UPDATE_METHOD = Integer.parseInt(configFile.getProperty("CENTROIDS_UPDATE_METHOD"));
		this.MAX_ITERATIONS = Integer.parseInt(configFile.getProperty("MAX_ITERATIONS"));
		this.inputList = new ArrayList<DigitalObject>(inputList);
		this.originalList = new ArrayList<DigitalObject>(inputList);
		this.totNumIterations = 0;

		System.out.println("NUM_CLUSTERS KMeans.java: "+NUM_CLUSTERS);

		ArrayList<ArrayList<DigitalObject>> clusters = selectInitialMedoids();
		clusters = runClustering(clusters);
		
		//ClusteringFileWriter.writeClusters(clusters, this.getClass().getSimpleName().toLowerCase(), topicName);


		//System.out.println("# clusters: " + clusters.size());
		
		//Select representative images
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, this.originalList, idTopic, topicName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);
		
		return outputList;

	}



	public long getTimeExecution(int k){
		long time = this.timeExecution[k];
		System.out.println("....time execution(ms) for k="+k+": "+time);
		return time;
	}
	public ArrayList<ArrayList<DigitalObject>> run2(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		this.fm = FeatureManager.getInstance(idTopic);

		System.out.println("Run2.....: Executando o KMeans para o topico " + idTopic);

		configFile = new Properties();

		try {
			configFile.load(new FileInputStream("resources/kmedoids.properties"));
			//this.NUM_CLUSTERS = Integer.parseInt(configFile.getProperty("NUM_CLUSTERS"));  //esta linha estava fixando o número de clusters
			this.CENTROIDS_INITIALIZER = Integer.parseInt(configFile.getProperty("CENTROIDS_INITIALIZER"));
			this.CENTROIDS_UPDATE_METHOD = Integer.parseInt(configFile.getProperty("CENTROIDS_UPDATE_METHOD"));
			this.MAX_ITERATIONS = Integer.parseInt(configFile.getProperty("MAX_ITERATIONS"));
			this.inputList = new ArrayList<DigitalObject>(inputList);
			this.originalList = new ArrayList<DigitalObject>(inputList);
			this.totNumIterations = 0;
			configFile.load(new FileInputStream("resources/runnerConfigFile.properties"));
			this.timerDivisor = Integer.parseInt(configFile.getProperty("TIMER_DIVISOR"));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<ArrayList<DigitalObject>> clusters = selectInitialMedoids();
		clusters = runClustering(clusters);


		//ClusteringFileWriter.writeClusters(clusters, this.getClass().getSimpleName().toLowerCase(), topicName);

		//System.out.print(" # clusters: " + clusters.size());

		//Select representative images


		return clusters;

	}


	public void setNUM_CLUSTERS(int k){
		this.NUM_CLUSTERS = k;
	}
	
	public ArrayList<ArrayList<DigitalObject>> runForEnsemble() {
		ArrayList<ArrayList<DigitalObject>> clusters = selectInitialMedoids();
		clusters = runClustering(clusters);
		return clusters;
	}
	
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
	
	private ArrayList<ArrayList<DigitalObject>> runClustering(ArrayList<ArrayList<DigitalObject>> clusters) {

		totNumIterations++;
		System.out.println("Running Kmeans iteration " + totNumIterations);
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
		long startTime = System.nanoTime();
		//For each image in the list, define its corresponding centroid
		while (!inputList.isEmpty()) {

			//Gets the next element in the list
			DigitalObject image = inputList.remove(0);
			
			//If it is not a centroid
			if(!centroidsList.contains(image)){
				
				//Computes distance to the centroid of the first cluster
				double minDistToCentroid = getDistance(image, clusters.get(0).get(0));
				int imageClusterIndex = 0;
	
				// For each cluster
				for(int clusterIndex = 1; clusterIndex < NUM_CLUSTERS; clusterIndex++) {
						double distToCentroid = getDistance(image, clusters.get(clusterIndex).get(0));
	
						if (distToCentroid < minDistToCentroid){
							minDistToCentroid = distToCentroid;
							imageClusterIndex = clusterIndex;
						}
				}
				clusters.get(imageClusterIndex).add(image);
			}
		}

		this.timeExecution[clusters.size()] = (System.nanoTime() - startTime)/timerDivisor;// pega o tempo para a execução de kMin a kMax para cada um dos tópicos individualmente (um tópico por vez)

		//Updating Centroids
		switch (this.CENTROIDS_UPDATE_METHOD) {
		case 1:
			IAtualizeCentroids atualizeNN = new AverageClusterConectivity();
			clusters = atualizeNN.updateCentroids(clusters, this.fm);
			if (AverageClusterConectivity.centroidUpdated && totNumIterations < this.MAX_ITERATIONS)
				clusters = runClustering(clusters);
			break;
		
		case 2:
			IAtualizeCentroids atualizeMinMaxPath = new MinClusterRadius();
			clusters = atualizeMinMaxPath.updateCentroids(clusters, this.fm);
			if (MinClusterRadius.centroidUpdated && totNumIterations < this.MAX_ITERATIONS)
				clusters = runClustering(clusters);
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
	
	public double getDistance(DigitalObject obj1, DigitalObject obj2) {
		return this.fm.getAvgDistance(obj1, obj2);
	}
}