package rerank.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import object.DigitalObject;

public class RankedListStorage {
	
	private boolean isTestRun = true;
	
	private String rankedListOutputDir;
	private HashMap<String, String> rGT;
	
	private int topicId;
	private ArrayList<DigitalObject> inputList;
	private String topicName;
	private ArrayList<Double> scores;
	
	public RankedListStorage(int topicId, ArrayList<DigitalObject> inputList, String topicName, String descriptorName, ArrayList<Double> scores, String outputDir){
		this.topicId = topicId;
		this.inputList = inputList;
		this.topicName = topicName;
		this.rankedListOutputDir = outputDir + File.separator + descriptorName;
		this.scores = scores;
		
		if(!this.isTestRun)
			this.rGT = this.readRelevanceGT(topicName);
	}
	
	public void run(){						
			//SAVE LIST
		    File rankedListDir= new File(this.rankedListOutputDir);
		    rankedListDir.mkdirs();
		
			String rankedListFileName = this.rankedListOutputDir + File.separator + topicId + "_" + topicName;
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(rankedListFileName));
				for (int i = 0; i < this.inputList.size(); i++) {
					DigitalObject digitalObject = this.inputList.get(i);
					bw.write(i + "," + digitalObject.getId() + "," + String.format("%.10f", scores.get(i)));
					
					if(!this.isTestRun)
						bw.write("," + this.rGT.get(digitalObject.getId()));
							
					bw.write("\n");
				}			
				
				bw.close();				
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
	}
	
	private HashMap<String, String> readRelevanceGT(String topicName) {
		HashMap<String, String> rGT = new HashMap<String, String>();
		String groundTruthFile = "resources/rGT" + File.separator + topicName + " rGT.txt";
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(groundTruthFile));
			String strLine;
			
			while((strLine = br.readLine()) != null) {
				String[] arrayLine = strLine.split(",");
				String id = arrayLine[0].trim();
				String rl = arrayLine[1].trim();
				
				if(rl.equals("-1"))
					rl = "0";
				
				rGT.put(id, rl);
			}
			br.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return rGT;
	}
}