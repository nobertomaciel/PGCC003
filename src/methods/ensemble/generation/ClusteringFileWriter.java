package methods.ensemble.generation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import object.DigitalObject;

public class ClusteringFileWriter {
	
	public static void writeClusters(ArrayList<ArrayList<DigitalObject>> clusters, String algorithm, String topicName) {
		String dataset = "devset";
		Properties dataConfigFile = new Properties();
		try {
			dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
			dataset = dataConfigFile.getProperty("DATASET");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		File file = new File(dataset + "/resources/clusters/" + algorithm + "/" + topicName + "_clusters");
		file.getParentFile().mkdirs();
		System.out.println(file);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			for(ArrayList<DigitalObject> cluster : clusters) {
				for(DigitalObject db : cluster)
					bw.write(db.getId() + " ");
				bw.newLine();
			}
			
			bw.close();
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}