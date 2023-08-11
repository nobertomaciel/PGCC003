package methods.exp.wkconect;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import measurer.IDigitalObjectDistanceMeasurer;
import object.DigitalObject;
import object.IDigitalObject;
import config.FeatureManager;

/**
 * This class returns KConectDistance as the average distance from the object to KNN on the target 
 * all available terminals
 * @author rtripodi
 *
 */
public class WeightedKConnectDistanceMeasurer implements IDigitalObjectDistanceMeasurer {

	private ArrayList<DigitalObject> target;
	String WKConnectConfigFile = "resources/WKConnectConfigFile.properties";
	FeatureManager fm;
	
	//Number of neighbors to use
	double k;
	
	//Weight of the computed kconnect distance
	double w;
	
	public WeightedKConnectDistanceMeasurer(int idTopic){
		fm = FeatureManager.getInstance(idTopic); // feature manager
		try {

		//properties do KConect
		Properties configFileMmr = new Properties();
		configFileMmr.load(new FileInputStream(WKConnectConfigFile));
		this.k = Double.parseDouble(configFileMmr.getProperty("K"));
		this.w = Double.parseDouble(configFileMmr.getProperty("W"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double getMeasureValue(IDigitalObject object) {
		
		Double dists[] = new Double[this.target.size()];
		for (int i = 0; i < this.target.size(); i++) {
			DigitalObject dObject = target.get(i);
			dists[i] = fm.getAvgDistance(object, dObject);
		}
		
		Arrays.sort(dists);
		
		double sum = 0;
		//Skips the first distance value ([0]) for its the object itself 
		for (int i = 1; i <= this.k; i++) {
			sum += dists[i]; 
		}		
		
		int objectIndex = target.indexOf(object);
		double factor = Math.sqrt(Math.sqrt(objectIndex));
		double rankRelevance = 1-(1.0/factor);
		
		double dist = sum / this.k; 
		
		return (this.w*dist)+((1-this.w)*rankRelevance);
	}
	
	public void setTarget(ArrayList<DigitalObject> target){
		this.target = target;
		
		//If the user has chosen a K smaller than the target
		if(target.size() < this.k || this.k == 0)
			k = target.size()-1;   //Reduces 1 since the object's distance to itself must be skiped
	}
}
