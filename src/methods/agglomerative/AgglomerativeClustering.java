package methods.agglomerative;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import config.FeatureManager;
import interfaces.IDiversify;
import interfaces.IFeatureManager;
import methods.clusterCommon.RepresentativeSelector;
import methods.ensemble.generation.ClusteringFileWriter;
import object.DigitalObject;

public class AgglomerativeClustering implements IDiversify {
	
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private ILinkage linkage;
	private int kClusters;
	private int timerDivisor = 1;
	private long[] timeExecution = new long[151];
	private int linkageCriterion;
	private Properties configFile;

	public String separator(String sep, int text){
		int l = sep.length() - text;
		return sep.substring(0, l);
	}

	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		System.out.println("....AgglomerativeClustering.java");
		System.out.println("....run():");

		this.fm = FeatureManager.getInstance(idTopic);
		this.originalList = new ArrayList<DigitalObject>(inputList);
		
		System.out.println("....original list size: " + originalList.size());
		
		configFile = new Properties();
		
		try {
			configFile.load(new FileInputStream("resources/agglomerative.properties"));
			//this.kClusters = Integer.parseInt(configFile.getProperty("K_CLUSTERS")); // foi retirado, pois, estava causando repetição dos valores de performance
			this.linkageCriterion = Integer.parseInt(configFile.getProperty("LINKAGE"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<ArrayList<DigitalObject>> clusters = initializeClustering(inputList);
		clusters = runAgglomerativeClustering(clusters);
		
		//ClusteringFileWriter.writeClusters(clusters, this.getClass().getSimpleName().toLowerCase(), topicName);
		
		System.out.println("\nClustering: " + clusters.size() + " clusters");
		System.out.print("Clust Sizes: ");
		int sum = 0;
		for (ArrayList<DigitalObject> c: clusters) {
			System.out.print(c.size() + " ");
			sum += c.size();
		}
		System.out.println("\nFinal list size: " + sum);

		//Select representative images
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, this.originalList, idTopic, topicName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);

		System.out.println("------------------------------------");

		return outputList;
	}

    public void setNUM_CLUSTERS(int k){
		this.kClusters = k;
		System.out.println("....setNUM_CLUSTERS(k) - Running agglomerative for k="+this.kClusters);
	}

	public long getTimeExecution(int k){
		long time = this.timeExecution[k];
		System.out.println("....time execution(ms) for k="+k+": "+time);
		return time;
	}

	public ArrayList<ArrayList<DigitalObject>> run2(String dataset, IFeatureManager fm,
										ArrayList<DigitalObject> inputList, int idTopic, String topicName) {

		System.out.println("....run2():");

		this.fm = FeatureManager.getInstance(idTopic);

		this.originalList = new ArrayList<DigitalObject>(inputList);

		System.out.println("....original list size: " + originalList.size());

		configFile = new Properties();

		try {
			configFile.load(new FileInputStream("resources/agglomerative.properties"));
			this.linkageCriterion = Integer.parseInt(configFile.getProperty("LINKAGE"));
			configFile.load(new FileInputStream("resources/runnerConfigFile.properties"));
			this.timerDivisor = Integer.parseInt(configFile.getProperty("TIMER_DIVISOR"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

		ArrayList<ArrayList<DigitalObject>> clusters = initializeClustering(inputList);
		clusters = runAgglomerativeClustering(clusters);


		int size = 1;
		if(clusters.size()>9){size = 2;}
		if(clusters.size()>99){size = 3;}
		String sep = separator("...............................................",size);
		System.out.format("....Finish run2%s: cluster.size()=%d%n", sep, clusters.size());

	    return clusters;
	}


	public ArrayList<ArrayList<DigitalObject>> runForEnsemble() {
		ArrayList<ArrayList<DigitalObject>> clusters = initializeClustering(originalList);
		clusters = runAgglomerativeClustering(clusters);
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> initializeClustering(ArrayList<DigitalObject> inputList) {

		System.out.print("....initializeClustering(inputList):");

		ArrayList<ArrayList<DigitalObject>> clustering = new ArrayList<ArrayList<DigitalObject>>();
		ArrayList<DigitalObject> copyInputList = (ArrayList<DigitalObject>) inputList.clone();

		while(copyInputList.size() > 0) {
			ArrayList<DigitalObject> cluster = new ArrayList<DigitalObject>();
			cluster.add(copyInputList.remove(0)); //Ao término a lista estará totalmente vazia
			clustering.add(cluster);
		}

		System.out.println("init num clusters: " + clustering.size());
		
		return clustering;
	}
	
	private ArrayList<ArrayList<DigitalObject>> runAgglomerativeClustering(ArrayList<ArrayList<DigitalObject>> clustering) {

		System.out.print("....runAgglomerativeClustering(clustering):");

		switch(linkageCriterion) {
			case 1: this.linkage = new SingleLink(); break;			
			case 2:	this.linkage = new CompleteLink(); break;			
			case 3:	this.linkage = new AverageLink(); break;
			case 4:	this.linkage = new CentroidMethod(); break;
			case 5:	this.linkage = new MedianMethod(); break;
			case 6:	this.linkage = new WardsMethod(); break;
		}

		System.out.println("....Running agglomerative for k="+this.kClusters);
		long startTime = System.nanoTime();
		while(clustering.size() > this.kClusters) {
			int[] indexes = linkage.findClosestPair(clustering, this.fm);
			try {
				mergeClusters(clustering, indexes);
				this.timeExecution[clustering.size()] = (System.nanoTime() - startTime)/timerDivisor;// pega o tempo para a execução de kMin a kMax para cada um dos tópicos individualmente (um tópico por vez)
				// o best K deve ser utilizado aqui para interromper o algoritmo
				//System.out.println("clustering.size(): "+clustering.size());
			}
			catch (Exception e){
				System.out.println(e);
			}
		}
		
		return clustering;
	}
	
	private void mergeClusters(ArrayList<ArrayList<DigitalObject>> clustering, int[] indexes) {

		ArrayList<DigitalObject> newCluster = new ArrayList<DigitalObject>();
				
		//Populate new cluster
		newCluster.addAll(clustering.get(indexes[0]));
		newCluster.addAll(clustering.get(indexes[1]));
		
		//Removes old clusters. The one with the biggest index first for not changing the position of the other
		clustering.remove(Math.max(indexes[0], indexes[1]));
		clustering.remove(Math.min(indexes[0], indexes[1]));
		
		clustering.add(newCluster);
	}
}