package methods.ensemble.consensus;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import config.FeatureManager;
import interfaces.IDiversify;
import interfaces.IFeatureManager;
import methods.clusterCommon.RepresentativeSelector;
import object.DigitalObject;

public class CSPA implements IDiversify {
	
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private int numberOfPartitions;
	private String dataset;

	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		
		System.out.println("Executing CSPA algorithm ensemble for the topic " + idTopic);
		
		this.readConfig();
		
		
		
		this.originalList = new ArrayList<DigitalObject>(inputList);
		this.fm = FeatureManager.getInstance(idTopic);
		
		HashMap<Integer, DigitalObject> map = new HashMap<Integer, DigitalObject>();
		for(int i = 0; i < originalList.size(); i++) {
			map.put(i, originalList.get(i));
		}
		
		ArrayList<ArrayList<ArrayList<DigitalObject>>> allClustering = readAllClustering(topicName);
		ArrayList<int[][]> initialSimilarityMatrices = constructInitialSimilarityMatrices(allClustering);
		double[][] finalSimilarityMatrix = constructSimilarityMatrix(initialSimilarityMatrices, allClustering.size());
		
		ArrayList<ArrayList<DigitalObject>> clusters = partitionSimilarityMatrix(finalSimilarityMatrix, topicName);
		
		System.out.println("Clust sizes: ");
		for(ArrayList<DigitalObject> cluster : clusters) {
			System.out.print(cluster.size() + " ");
		}
		
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, this.originalList, idTopic, topicName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);
		
		return outputList;
	}
	
	private void readConfig(){
		try {
			Properties datasetConfigFile = new Properties();
			Properties configFile = new Properties();
			configFile.load(new FileInputStream("resources/ensemble.properties"));
			datasetConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
			this.numberOfPartitions = Integer.parseInt(configFile.getProperty("NUMBER_OF_PARTITIONS"));
			this.dataset = datasetConfigFile.getProperty("DATASET");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<ArrayList<ArrayList<DigitalObject>>> readAllClustering(String topicName) {
		ArrayList<ArrayList<ArrayList<DigitalObject>>> clusterOfClusters = new ArrayList<ArrayList<ArrayList<DigitalObject>>>();
		File clustersDirectory = new File(dataset + "/resources/clusters/");
		String[] algorithms = clustersDirectory.list();
		
		for(String algorithm : algorithms) {
			ArrayList<ArrayList<DigitalObject>> clusters = new ArrayList<ArrayList<DigitalObject>>();
			try {
				File partition = new File(clustersDirectory + "/" + algorithm + "/" + topicName + "_clusters");
				System.out.println("Reading from " + partition);
				BufferedReader br = new BufferedReader(new FileReader(partition));
				String strLine;
				
				while((strLine = br.readLine()) != null) {
					ArrayList<DigitalObject> cluster = new ArrayList<DigitalObject>();
					String[] ids = strLine.split(" ");
					
					for(String id : ids) {
						for(DigitalObject db : originalList) {
							if(db.getId().equals(id)) {
								cluster.add(db);
							}
						}
					}
					
					clusters.add(cluster);
				}
				br.close();
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			clusterOfClusters.add(clusters);
		}
		
		return clusterOfClusters;
	}
	
	private int[][] constructBinaryMembershipIndicatorMatrix(ArrayList<ArrayList<DigitalObject>> clusters) {
		int n = originalList.size();
		int[][] H = new int[n][n];
		
		for(ArrayList<DigitalObject> cluster : clusters) {
			for(int i = 0; i < cluster.size(); i++) {
				for(int j = 0; j < cluster.size(); j++) {
					H[originalList.indexOf(cluster.get(i))][originalList.indexOf(cluster.get(j))] = 1;	
				}
			}
		}
		
		return H;
	}
	
	private ArrayList<int[][]> constructInitialSimilarityMatrices(ArrayList<ArrayList<ArrayList<DigitalObject>>> clusters) {
		ArrayList<int[][]> matricesH = new ArrayList<int[][]>();
		for(ArrayList<ArrayList<DigitalObject>> clustering : clusters) {
			int[][] H = constructBinaryMembershipIndicatorMatrix(clustering);
			matricesH.add(H);
		}
		return matricesH;
	}
	
	private double[][] constructSimilarityMatrix(ArrayList<int[][]> matricesH, int clusterSize) {
		int n = originalList.size();
		double[][] averageH = new double[n][n];
		
		for(int[][] matrix : matricesH)
			for(int i = 0; i < matrix.length; i++)
				for(int j = 0; j < matrix[0].length; j++) {
					if(i == j) {
						averageH[i][j] = 0;
					} else {
						averageH[i][j] += matrix[i][j];
					}
					
				}		

		for(int i = 0; i < averageH.length; i++)
			for(int j = 0; j < averageH[i].length; j++)
				averageH[i][j] /= clusterSize;
		
		return averageH;
	}
	
	private ArrayList<ArrayList<DigitalObject>> partitionSimilarityMatrix(double[][] similarityMatrix, String topicName) {
		ArrayList<ArrayList<DigitalObject>> finalClustering = new ArrayList<ArrayList<DigitalObject>>(numberOfPartitions);
		
		for(int i = 0; i < numberOfPartitions; i++) {
			ArrayList<DigitalObject> cluster = new ArrayList<DigitalObject>();
			finalClustering.add(cluster);
		}
		
		int numberOfVertices = originalList.size();
		int numberOfEdges = 0;
		
		for(int i = 0; i < similarityMatrix.length; i++)
			for(int j = 0; j < similarityMatrix.length; j++)
				if(similarityMatrix[i][j] != 0)
					numberOfEdges++;
		
		numberOfEdges = numberOfEdges / 2;

		File file = new File(dataset + "/resources/metis/ensembleFile_" + topicName + ".graph");
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			System.out.println("The first line: " + originalList.size() + " " + numberOfEdges + " " + "001");
			bw.write(numberOfVertices + " " + numberOfEdges + " " + "001");
			bw.newLine();
			
			for(int i = 0; i < similarityMatrix.length; i++) {
				for(int j = 0; j < similarityMatrix[i].length; j++) {
					if(similarityMatrix[i][j] != 0) {
						System.out.print(j+1 + " " + Math.round(similarityMatrix[i][j] * 100000) + " ");
						bw.write(j+1 + " " + Math.round(similarityMatrix[i][j] * 100000) + " ");
					}
				}
				bw.newLine();
			}
			bw.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		if(numberOfEdges == 0) {
			ArrayList<ArrayList<DigitalObject>> clusters = new ArrayList<ArrayList<DigitalObject>>(originalList.size());
			for(int i = 0; i < originalList.size(); i++) {
				ArrayList<DigitalObject> cluster = new ArrayList<DigitalObject>();
				cluster.add(originalList.get(i));
				clusters.add(cluster);
			}
			
			return clusters;
		}
		
		File f = new File(dataset + "/resources/metis/");
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "gpmetis " + "ensembleFile_" + topicName + ".graph " + Integer.toString(numberOfPartitions));
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
			
			File f2 = new File(dataset + "/resources/metis/ensembleFile_" + topicName + ".graph.part." + Integer.toString(numberOfPartitions));
			
			BufferedReader br = new BufferedReader(new FileReader(f2));
			String strLine;
			
			int i = 0;
			
			while((strLine = br.readLine()) != null) {
				
				DigitalObject d = originalList.get(i);
				if(d == null) {
					continue;
				}
				
				finalClustering.get(Integer.parseInt(strLine)).add(d);
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
		
		//Dealing with empty clusters
		Iterator<ArrayList<DigitalObject>> iterator = finalClustering.iterator();
		while(iterator.hasNext()) {
			ArrayList<DigitalObject> cluster = iterator.next();
			if(cluster.size() == 0)
				iterator.remove();
		}
		
		return finalClustering;
	}
}