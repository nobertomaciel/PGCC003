package rerank.cred.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import object.DigitalObject;
import rerank.common.RankedListStorage;

public class CredRankerExp {
	
	Properties credConfig;
	String userCredFile;
	String latLongDataDir;
	boolean useRankScore;
	String dataset;
	
	HashMap<String, Double> visualScore, faceProportion, tagSpecificity, locationSimilarity;
	HashMap<String, Double> photoCount, uniqueTags, uploadFrequency, bulkProportion;
	HashMap<String, Double> meanPhotoViews, meanTitleWordCounts, meanTagsPerPhoto, meanTagRank, meanImageTagClarity;
	
	HashMap<String, String> imageUser, lat, lon, date, time;
	HashMap<String, Double> views;
	
	//Score
	HashMap<String, Double> credScore;
	HashMap<String, Double> rankScore;
	CMeasurer measurer;
	Random rand;
	
	//Storage
	String credScoreName = "";
	
	private void readConfig(){
		credConfig = new Properties();
		
		try {
			credConfig.load(new FileReader("resources/credRanker.properties"));
			this.userCredFile = this.dataset + File.separator + credConfig.getProperty("USERS_CRED_FILE");
			this.latLongDataDir = this.dataset + File.separator + credConfig.getProperty("LATLONG_DATA_DIR");
			this.useRankScore =  Boolean.parseBoolean(credConfig.getProperty("USE_RANK_SCORE"));
						
			long randomSeed = Long.parseLong(credConfig.getProperty("RANDOM_SEED"));
			this.rand = new Random();
			this.rand.setSeed(randomSeed);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readUsersCred(){
		try {
			
			this.visualScore = new HashMap<String, Double>();
			this.faceProportion = new HashMap<String, Double>();
			this.tagSpecificity = new HashMap<String, Double>();
			this.locationSimilarity = new HashMap<String, Double>();
			this.photoCount = new HashMap<String, Double>();
			this.uniqueTags = new HashMap<String, Double>();
			this.uploadFrequency = new HashMap<String, Double>();
			this.bulkProportion = new HashMap<String, Double>();
			this.meanPhotoViews = new HashMap<String, Double>();
			this.meanTitleWordCounts = new HashMap<String, Double>();
			this.meanTagsPerPhoto = new HashMap<String, Double>();
			this.meanTagRank = new HashMap<String, Double>();
			this.meanImageTagClarity = new HashMap<String, Double>();
			
			this.credScore = new HashMap<String, Double>();
			
			BufferedReader br = new BufferedReader(new FileReader(this.userCredFile));
			br.readLine(); //Skips header

			ArrayList<String> users = new ArrayList<String>();
			String line;
			while((line = br.readLine()) != null){
				
				String cred[] = line.split(",");
				String userID = cred[0];
				users.add(userID);
								
				this.visualScore		.put(userID, toDouble(cred[1]));
				this.faceProportion		.put(userID, toDouble(cred[2]));
				this.tagSpecificity		.put(userID, toDouble(cred[3]));				
				this.locationSimilarity	.put(userID, toDouble(cred[4]));
				this.photoCount			.put(userID, toDouble(cred[5]));
				this.uniqueTags			.put(userID, toDouble(cred[6]));
				this.uploadFrequency	.put(userID, toDouble(cred[7]));
				this.bulkProportion		.put(userID, toDouble(cred[8]));
				this.meanPhotoViews		.put(userID, toDouble(cred[9]));
				this.meanTitleWordCounts.put(userID, toDouble(cred[10]));
				this.meanTagsPerPhoto	.put(userID, toDouble(cred[11]));
				this.meanTagRank		.put(userID, toDouble(cred[12]));
				this.meanImageTagClarity.put(userID, toDouble(cred[13]));
			}
			
			//Normalizes not normalized descriptors
			this.normalizeMinMax(this.bulkProportion, false);
			this.normalizeMinMax(this.locationSimilarity, false); 
			this.normalizeMinMax(this.meanPhotoViews, false);
			this.normalizeMinMax(this.meanTitleWordCounts, false);
			this.normalizeMinMax(this.meanTagsPerPhoto, false);
			this.normalizeMinMax(this.meanTagRank, false);
			this.normalizeMinMax(this.meanImageTagClarity, true);//revert (1-norm)
			this.normalizeMinMax(this.photoCount, false);
			this.normalizeMinMax(this.uploadFrequency, true);//revert (1-norm)
						
			//Renormalize
			this.normalizeMinMax(this.faceProportion, true);//revert (1-norm)
			this.normalizeMinMax(this.tagSpecificity, false);			
			this.normalizeMinMax(this.uniqueTags, true);//revert (1-norm)
			this.normalizeMinMax(this.visualScore, false);
			
			for (String user : users) {
				double score = computeLinearFusionCredScore(user);
				this.credScore.put(user, score);
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double toDouble(String value){
		if(value.equalsIgnoreCase("NULL") || value.equalsIgnoreCase("NA") || value.equalsIgnoreCase("NaN") || value.equals(" ")) 
			return Double.NaN;
		else		
			return Double.parseDouble(value);
	}

	private double computeLinearFusionCredScore(String user) {
		
		this.credScoreName = "linearCredViewFusion";
		
		double bulkProportion 		= 0.5;
		double meanTitleWordCounts 	= 0.7;
		double meanTagsPerPhoto 	= 0.3;
		double meanTagRank 			= 0.9;
		double meanImageTagClarity 	= 1.0;
		double photoCount 			= 0.4;
		double uploadFrequency		= 0.8;		
		double uniqueTags 			= 0.6;
		
		double visualScore 			= 0.0;
		double faceProportion 		= 0.0;
		double tagSpecificity 		= 0.0;
		double locationSimilarity 	= 0.0;
		double meanPhotoViews 		= 0.0;
						
		double score = 0;				
		
		score += bulkProportion*this.bulkProportion.get(user);
		score += visualScore*this.visualScore.get(user);
		score += locationSimilarity*this.locationSimilarity.get(user);
		score += meanPhotoViews*this.meanPhotoViews.get(user);
		score += meanTitleWordCounts*this.meanTitleWordCounts.get(user);
		score += meanTagsPerPhoto*this.meanTagsPerPhoto.get(user);
		score += meanTagRank*this.meanTagRank.get(user);
		score += meanImageTagClarity*this.meanImageTagClarity.get(user);
		score += photoCount*this.photoCount.get(user);
		score += uploadFrequency*this.uploadFrequency.get(user);
		score += faceProportion*this.faceProportion.get(user);
		score += tagSpecificity*this.tagSpecificity.get(user);
		score += uniqueTags*this.uniqueTags.get(user);
		score += visualScore*this.visualScore.get(user);

		return score;
	}
	
	private void readTopicImageUsers(String topicName){
		try {
			this.imageUser = new HashMap<String, String>();
			this.lat = new HashMap<String, String>();	
			this.lon = new HashMap<String, String>();	
			this.views = new HashMap<String, Double>();
			this.date = new HashMap<String, String>();	
			this.time = new HashMap<String, String>();			
			
			String imageDataFile = this.latLongDataDir + File.separator + topicName + "_latlong.txt";
			BufferedReader br = new BufferedReader(new FileReader(imageDataFile));

			String line;
			while((line = br.readLine()) != null){
				String imageData[] = line.split(" ");
				//imageData[0] //sequential number
				String imageId = imageData[2];
				this.imageUser.put(imageId, imageData[1]);
				this.lat.put(imageId, imageData[3]);
				this.lon.put(imageId, imageData[4]);
				this.views.put(imageId, Double.parseDouble(imageData[5]));
				this.date.put(imageId, imageData[6]);
				this.time.put(imageId, imageData[7]);
			}
			
			this.normalizeMinMax(this.views, false);
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void normalizeMinMax(HashMap<String, Double> score, boolean invert){
		Set<String> keys = score.keySet();
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		//Computes min and max
		for (String key : keys) {
			double value = score.get(key);
			
			if(!Double.isNaN(value)){
				if(value > max)
					max = value;
				
				if(value < min)
					min = value;
			}
		}
		
		//Normalizes and inverts (when applicable)
		for (String key : keys) {
			
			double value = score.get(key);
			
			//Checks for empty/null/NA/nan/NaN value and is replaces by a random value
			if(Double.isNaN(value)){ 
				double randomValue = this.getRandomValue();				
				score.put(key, randomValue);
				
			}else{
				
				double norm = (value - min)/(max - min);
				if(invert)
					score.put(key, 1-norm);
				else
					score.put(key, norm);	
			}
		}
	}
	
	private void invertScore(HashMap<String, Double> score){
		
		for (String key : score.keySet()) {
			double value = score.get(key);
			
			if(Double.isNaN(value)){ //It means an empty/null/NA/nan/NaN value and is replaced by a random value
				double randomValue = this.getRandomValue();				
				score.put(key, randomValue);
			}else{
				score.put(key, 1-value);	
			}
		}
	}
	
	private double getRandomValue(){
		return this.rand.nextDouble();
	}
	
	private void calRankScore(ArrayList<DigitalObject> inputList){
		this.rankScore = new HashMap<String, Double>();
		for (int i = 0; i < inputList.size(); i++)
			this.rankScore.put(inputList.get(i).getId(), getRankScoreByPosition(i));
	}
	
	private double getRankScoreByPosition(int rank){
		double factor = Math.sqrt(Math.sqrt(rank+1));
		double score = 1.0/factor;
		return score;
	}
	
	private ArrayList<DigitalObject> reRankList(ArrayList<DigitalObject> inputList){
		
		this.measurer = new CMeasurer();
		this.measurer.setCredScore(this.credScore);
		this.measurer.setRankScore(this.rankScore);
		this.measurer.setImageUser(this.imageUser);
		this.measurer.setViews(this.views);
		this.measurer.setUseRankScore(this.useRankScore);
		
		for (int i = 0; i < inputList.size(); i++)
			inputList.get(i).setMeasurer(this.measurer);
		
		Collections.sort(inputList);
		
		return inputList;
	}
	
	private void saveRankedList(int topicID, String topicName, ArrayList<DigitalObject> sortedList) {
		
		ArrayList<Double> scores = new ArrayList<Double>();
		
		for (int i = 0; i < sortedList.size(); i++) {
			DigitalObject digitalObject = sortedList.get(i);
			double score = this.measurer.getMeasureValue(digitalObject);
			scores.add(score);
		}
		
		String outputDir = "/home/rtripodi/pesquisa/MediaEval/MediaEvalDiverseTask2017/ME2017DivTaskCredRankedLists";
		RankedListStorage storage = new RankedListStorage(topicID, sortedList, topicName, this.credScoreName, scores, outputDir);
		storage.run();
	}

	public ArrayList<DigitalObject> run(String dataset, ArrayList<DigitalObject> inputList, int topicID, String topicName) {
		System.out.println("Running Credibility Re-ranker");
		
		this.dataset = dataset;
		this.readConfig();
		this.readUsersCred();
		this.readTopicImageUsers(topicName);
		this.calRankScore(inputList);
		inputList = this.reRankList(inputList); 
		
		//SAVE RANKED LIST - FOR FUSION ANALYSIS (GP-based Javier's method)
		//saveRankedList(topicID, topicName, inputList);
		
		return inputList;
	}
}