package rerank.text;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import object.DigitalObject;
import rerank.common.RankedListStorage;

public class TextDistReRanker {
	ArrayList<DigitalObject> inputList;
	ArrayList<DigitalObject> originalList;
	
	private boolean normalize = false;
	TextDistMeasurer measurer;
	String descriptor = "";
	String topicTextDir;
	String outputTextDir;
	
	public ArrayList<DigitalObject> run(String dataset, ArrayList<DigitalObject> inputList, ArrayList<DigitalObject> originalList, String topicName, int topicID) {
	
		System.out.println("....Running TextRanker");
		System.out.println("....input list size: " + inputList.size());
		this.inputList = inputList;
		this.originalList = originalList;
		
		readConfig();
		System.out.println("....Text Simil: " + this.descriptor);

		String textDistDir = this.topicTextDir;
		HashMap<String, Double> distances = readTopicTextDistFile(textDistDir + File.separator + topicName + "_" + this.descriptor + "_dist.bin");
				
		this.measurer = new TextDistMeasurer();
		this.measurer.setDistances(distances);
		
		for (Iterator<DigitalObject> iterator = inputList.iterator(); iterator.hasNext();) {
			DigitalObject digitalObject = (DigitalObject) iterator.next();
			digitalObject.setMeasurer(this.measurer);
		}
		
		Collections.sort(inputList);
		
		//inputList = filter(inputList, distances);
		
		//SAVE RANKED LIST - FOR FUSION ANALYSIS (GP-based Javier's method)
		//saveRankedList(topicID, topicName, inputList);
		return inputList;
	}
	
	private ArrayList<DigitalObject> filter (ArrayList<DigitalObject> inputList, HashMap<String, Double> distances){
				
		int k = 20;
		for (int i = 21; i < inputList.size(); i++) {
			double dist = distances.get(inputList.get(i).getId());
			
			if(dist > 0.7){
				k = i;
				break;
			}
		}
		
		ArrayList<DigitalObject> filteredList = new ArrayList<DigitalObject>();
		for (int i = 0; i < k; i++) {
			filteredList.add(inputList.get(i));
		}
		
		System.out.println("k: " + k);
		System.out.println("size: " + filteredList.size());		
		
		return filteredList;
	}	
	
	private void readConfig(){
		Properties configFile = new Properties();
		
		try {
			configFile.load(new FileInputStream("resources/textRanker.properties"));
			this.descriptor = configFile.getProperty("SIMIL_MEASURE");
			this.topicTextDir = configFile.getProperty("DIST_DIR");
			this.outputTextDir = configFile.getProperty("OUTPUT_DIR");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private HashMap<String, Double> readTopicTextDistFile(String distanceFile){
		HashMap<String, Double> distances = new HashMap<String, Double>();
				
		ArrayList<Double> distBin = new ArrayList<Double>();
		try {
			FileInputStream fileInputStream = new FileInputStream(distanceFile);
			DataInputStream inputStream = new DataInputStream(fileInputStream);

			for (int i = 0; i < this.originalList.size(); i++) {

				double dist = inputStream.readDouble();
				distBin.add(dist);
				//System.out.println(this.inputList.get(i) + ": " + dist);
			}
			//System.out.println();

			fileInputStream.close();
			inputStream.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(this.normalize)
			distBin = minMaxNorm(distBin);
		
		for (int i = 0; i < originalList.size(); i++)
			distances.put(this.originalList.get(i).getId(), distBin.get(i));
		
		return distances;
	}
	
	private ArrayList<Double> minMaxNorm(ArrayList<Double> distBin){
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < distBin.size(); i++) {
			if(distBin.get(i) < min)
				min = distBin.get(i);
			
			if(distBin.get(i) > max)
				max = distBin.get(i);
		}
		
		for (int i = 0; i < distBin.size(); i++) {
			double normValue = (distBin.get(i) - min) / (max - min);
			distBin.set(i, normValue);
		}
		
		return distBin;
	}
	
	private void saveRankedList(int topicID, String topicName, ArrayList<DigitalObject> sortedList) {
		
		ArrayList<Double> scores = new ArrayList<Double>();
		
		for (int i = 0; i < sortedList.size(); i++) {
			DigitalObject digitalObject = sortedList.get(i);
			double score = this.measurer.getMeasureValue(digitalObject);
			scores.add(score);
		}
		
		String outputDir = this.outputTextDir;
		RankedListStorage storage = new RankedListStorage(topicID, sortedList, topicName, this.descriptor, scores, outputDir);
		storage.run();
	}
}