package clusterEff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Properties;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import object.DigitalObject;

public class GTRelFilter {
	
		private String topicName;
		private String groundTruthDir = "resources/rGT";
		private String dataset;
		
		public GTRelFilter(String topicName) {
			
			Properties dataConfigFile = new Properties();
			
			try {
				dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
				this.dataset = dataConfigFile.getProperty("DATASET");
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			this.topicName = topicName;
		}
		
		public ArrayList<DigitalObject> runFiltering(ArrayList<DigitalObject> inputList) {
			
			HashMap<String, String> rGT = readRelevanceGT(topicName);
			ListIterator<DigitalObject> iterator = inputList.listIterator();
			
			while(iterator.hasNext()) {
				DigitalObject obj = iterator.next();
				if(rGT.get(obj.getId()).equals("0"))
					iterator.remove();
			}
			
			return inputList;
		}
		
		private HashMap<String, String> readRelevanceGT(String topicName) {
			
			HashMap<String, String> rGT = new HashMap<String, String>();
			String groundTruthFile = dataset + File.separator + groundTruthDir + File.separator + topicName + " rGT.txt";
			
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
