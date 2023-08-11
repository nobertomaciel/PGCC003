package filter.blur;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Properties;

import object.DigitalObject;

public class BlurFilter {
	
	private String topicName;
	private String blurDataDir = "resources/blurData";
	private double threshold;
	private Properties configFile;
	private String dataset;
	
	public BlurFilter(String topicName) {
		configFile = new Properties();
		Properties dataConfigFile = new Properties();
		try {
			configFile.load(new FileReader("resources/blur.properties"));
			threshold = Double.parseDouble(configFile.getProperty("BLUR_THRESHOLD"));
			
			dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
			dataset = dataConfigFile.getProperty("DATASET");
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		this.topicName = topicName;
	}
	
	public ArrayList<DigitalObject> runFiltering(ArrayList<DigitalObject> inputList) {
		
		System.out.println("### Running BLUR Filter ###");
		
		HashMap<String, Double> blurExtents = readBlurData(topicName);
		ListIterator<DigitalObject> iterator = inputList.listIterator();
		
		int numFiltered = 0;
		
		while(iterator.hasNext()) {
			DigitalObject obj = iterator.next();
			
			if(blurExtents.get(obj.getId()) > threshold){
				iterator.remove();
				numFiltered++;
			}
		}
		
		System.out.println("Total Images Filtered: " + numFiltered);
		System.out.println("done.\n");
		return inputList;
	}
	
	private HashMap<String, Double> readBlurData(String topicName) {
		
		HashMap<String, Double> blurData = new HashMap<String, Double>();
		String blurDataFile = dataset + File.separator + blurDataDir + File.separator + topicName + ".txt";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(blurDataFile));
			String line;
			
			while((line = reader.readLine()) != null) {
				String[] array = line.split(",");
				String id = array[0].trim();
				String blur = array[1].trim();
				double blurExtent = Double.parseDouble(blur);
				
				blurData.put(id, blurExtent);
			}
			reader.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return blurData;
	}
}