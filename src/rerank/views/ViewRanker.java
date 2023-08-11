package rerank.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import object.DigitalObject;
import rerank.common.ImgMetadata;
import rerank.common.RankedListStorage;

public class ViewRanker {
	
	HashMap<String, ImgMetadata> metadata;
	HashMap<String, Double> views;
	String dataset;
	ViewMeasurer measurer;
	Random rand;
	
	public ArrayList<DigitalObject> run(String dataset, ArrayList<DigitalObject> inputList, int topicID, String topicName) {
		System.out.println("Running View Ranker");
		this.dataset = dataset;
		this.metadata = readMetadata(topicName);
		this.views = new HashMap<String, Double>();
		
		this.rand = new Random();
		this.rand.setSeed(453459);
		
		for (DigitalObject digitalObject : inputList) {
			double numViews = this.metadata.get(digitalObject.getId()).getNumViews();
			this.views.put(digitalObject.getId(), numViews);
		}
		
		this.measurer = new ViewMeasurer();
		this.measurer.setDistances(this.views);
		
		for (Iterator<DigitalObject> iterator = inputList.iterator(); iterator.hasNext();) {
			DigitalObject digitalObject = (DigitalObject) iterator.next();
			digitalObject.setMeasurer(this.measurer);
		}
		
		this.normalizeMinMax(this.views, false);
		
		Collections.sort(inputList);
		
		//Removes image with few views
//		int unpop = 0;
//		while(this.views.get(inputList.get(0).getId()) <= 5){
//			inputList.remove(0);
//			unpop++;
//		}
//		System.out.println("Unpop removed: " + unpop);
		
		//saveRankedList(topicID, topicName, inputList);
		
		return inputList;
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
	
	private double getRandomValue(){
		return this.rand.nextDouble();
	}
	
	private void saveRankedList(int topicID, String topicName, ArrayList<DigitalObject> sortedList) {
		
		ArrayList<Double> scores = new ArrayList<Double>();
		
		for (int i = 0; i < sortedList.size(); i++) {
			DigitalObject digitalObject = sortedList.get(i);
			double score = this.measurer.getMeasureValue(digitalObject);
			scores.add(score);
		}
		
		String outputDir = "/home/rtripodi/pesquisa/MediaEval/MediaEvalDiverseTask2017/ME2017DivTaskCredRankedLists";
		RankedListStorage storage = new RankedListStorage(topicID, sortedList, topicName, "views", scores, outputDir);
		storage.run();
	}
	
	private HashMap<String,ImgMetadata> readMetadata(String topicName){
		String metadataFileName = this.dataset + File.separator + "resources/latlong/" + topicName + "_latlong.txt";
		HashMap<String,ImgMetadata> metadata = new HashMap<String,ImgMetadata>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(metadataFileName));
			String line;
			while((line = br.readLine())!= null){
				ImgMetadata imgData = new ImgMetadata(line.trim());
				metadata.put(imgData.getId(), imgData);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return metadata;
	}
}
