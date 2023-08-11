package methods.credibility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;

import interfaces.IDiversify;
import interfaces.IFeatureManager;
import methods.clusterCommon.RepresentativeSelector;
import object.DigitalObject;
import rerank.common.ImgMetadata;

public class CredibilityClustering implements IDiversify {
	
	Properties credConfig;
	String userCredFile;
	String latLongDataDir;
	boolean useRankScore, intraSortByViews;
	int clusterMethod;
	String dataset;
	
	ArrayList<String> users;
	HashMap<String, Double> visualScore, faceProportion, tagSpecificity, locationSimilarity;
	HashMap<String, Double> photoCount, uniqueTags, uploadFrequency, bulkProportion;
	HashMap<String, Double> meanPhotoViews, meanTitleWordCounts, meanTagsPerPhoto, meanTagRank, meanImageTagClarity;
		
	HashMap<String,ImgMetadata> metadata;
	
	//Score
	HashMap<String, Double> credScore;
	HashMap<String, Double> rankScore;
	
	private ArrayList<DigitalObject> originalList;
	
	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		
		System.out.println("Running Credibility Clustering");
		this.dataset = dataset;
		
		this.originalList = new ArrayList<DigitalObject>(inputList);
		
		System.out.println("original list size: " + originalList.size());
		
		credConfig = new Properties();
		try {
			credConfig.load(new FileInputStream("resources/credCluster.properties"));
			this.userCredFile = credConfig.getProperty("USERS_CRED_FILE");
			this.latLongDataDir = credConfig.getProperty("LATLONG_DATA_DIR");
			this.useRankScore =  Boolean.parseBoolean(credConfig.getProperty("USE_RANK_SCORE"));
			this.intraSortByViews = Boolean.parseBoolean(credConfig.getProperty("INTRASORT_CLUSTERS_BY_VIEWS"));
			this.clusterMethod = Integer.parseInt(credConfig.getProperty("CLUSTER_METHOD"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//ReadData
		this.readConfig();
		this.readUserList();
		this.readTopicImageUsers(topicName);
		
		//Run Clustering
		ArrayList<ArrayList<DigitalObject>> clusters = runCredibilityClustering(inputList);
		
		//Intrasort clusters
		if(this.intraSortByViews)
			clusters = intraSortByViews(clusters);
		
		System.out.println("\nClustering: " + clusters.size() + " clusters");
		System.out.print("Clust Sizes: ");
		int sum = 0;
		for (ArrayList<DigitalObject> c: clusters) {
			System.out.print(c.size() + " ");
			sum += c.size();
		}
		System.out.println("\nFinal list size: " + sum);

		//Select representative images
		RepresentativeSelector selector = new RepresentativeSelector(fm, this.originalList, idTopic, topicName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);
		
		return outputList;
	}
	
	private ArrayList<ArrayList<DigitalObject>> runCredibilityClustering(ArrayList<DigitalObject> inputList) {
		
		ArrayList<ArrayList<DigitalObject>> clusters = null;
		
		switch (this.clusterMethod) {
		case 1: clusters = clusterByUser(inputList); break;
		case 2: clusters = clusterByMonth(inputList); break;
		case 3: clusters = clusterByUserAndMonth(inputList); break;
		default:
			break;
		}
		
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> clusterByUser(ArrayList<DigitalObject> inputList){
		ArrayList<ArrayList<DigitalObject>> clusters = new ArrayList<ArrayList<DigitalObject>>();
		
		//Create empty user clusters
		for (int i = 0; i < this.users.size(); i++) {
			ArrayList<DigitalObject> userCluster = new ArrayList<DigitalObject>();
			clusters.add(userCluster);
		}
		
		//Places each image in the corresponding user cluster
		for (int i = 0; i < inputList.size(); i++) {
			String user = this.metadata.get(inputList.get(i).getId()).getUser();
			int userIndex = this.users.indexOf(user);
						
			clusters.get(userIndex).add(inputList.get(i));
		}
		
		//Removes empty clusters
		for (int i = clusters.size()-1; i >=0 ; i--)
			if(clusters.get(i).size() == 0)
				clusters.remove(i);
		
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> clusterByMonth(ArrayList<DigitalObject> inputList){
		ArrayList<ArrayList<DigitalObject>> clusters = new ArrayList<ArrayList<DigitalObject>>();
		
		//Creates one clusters for each month of the year
		for (int i = 0; i <= 11; i++) {
			clusters.add(new ArrayList<DigitalObject>());
		}
		
		for (int i = 0; i < inputList.size(); i++) {
			int month = this.metadata.get(inputList.get(i).getId()).getDate().get(GregorianCalendar.MONTH);
			clusters.get(month).add(inputList.get(i));
		}
		
		//Removes empty clusters
		for (int i = clusters.size()-1; i >=0 ; i--)
			if(clusters.get(i).size() == 0)
				clusters.remove(i);
		
		return clusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> clusterByUserAndMonth(ArrayList<DigitalObject> inputList){
		ArrayList<ArrayList<DigitalObject>> userClusters = new ArrayList<ArrayList<DigitalObject>>();
		userClusters = this.clusterByUser(inputList);
		
		ArrayList<ArrayList<DigitalObject>> dateClusters = new ArrayList<ArrayList<DigitalObject>>();
		for (ArrayList<DigitalObject> cluster : userClusters) {
			dateClusters.addAll(this.clusterByMonth(cluster));
		}
		
		return dateClusters;
	}
	
	private ArrayList<ArrayList<DigitalObject>> intraSortByViews(ArrayList<ArrayList<DigitalObject>> clusters){
		
		for (ArrayList<DigitalObject> cluster : clusters) {	
			//Sorts cluster i
			for(int objIndex = 0; objIndex < cluster.size()-1; objIndex++){
				
				//Sets the first element as the closest to the centroid and its distance as the minimum
				int mostViewed = objIndex;
				int maxViews = this.metadata.get(cluster.get(objIndex).getId()).getNumViews();
				
				//Checks if any other objects is closer than the selected one
				for(int candidateIndex = objIndex+1; candidateIndex < cluster.size(); candidateIndex++)
				{
					int candidateMaxViews = this.metadata.get(cluster.get(candidateIndex).getId()).getNumViews();
					
					if (candidateMaxViews > maxViews){
						maxViews = candidateMaxViews;
						mostViewed = candidateIndex;
					}
				}
				cluster.add(objIndex, cluster.remove(mostViewed));
			}
		}
		
		return clusters;
	}
	
	private void readConfig(){
		credConfig = new Properties();
		try {
			credConfig.load(new FileReader("resources/credRanker.properties"));
			this.userCredFile = this.dataset + File.separator + credConfig.getProperty("USERS_CRED_FILE");
			this.latLongDataDir = this.dataset + File.separator + credConfig.getProperty("LATLONG_DATA_DIR");
			this.useRankScore =  Boolean.parseBoolean(credConfig.getProperty("USE_RANK_SCORE"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readUserList(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.userCredFile));
			br.readLine(); //Skips header

			this.users = new ArrayList<String>();
			String line;
			while((line = br.readLine()) != null){
				String cred[] = line.split(",");								
				String userID = cred[0];
				this.users.add(userID);
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readTopicImageUsers(String topicName){
		String metadataFileName = this.dataset + File.separator + "resources/latlong/" + topicName + "_latlong.txt";
		this.metadata = new HashMap<String,ImgMetadata>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(metadataFileName));
			String line;
			while((line = br.readLine())!= null){
				ImgMetadata imgData = new ImgMetadata(line.trim());
				this.metadata.put(imgData.getId(), imgData);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}