package methods.mmr;

import java.util.ArrayList;
import java.util.HashMap;

import measurer.IDigitalObjectDistanceMeasurer;
import object.DigitalObject;
import object.IDigitalObject;
import config.FeatureManager;

/**
 * This class returns MMR distance as the average distance from the target to the partial result set using 
 * all available terminals
 * @author rtripodi
 *
 */
public class MMRMinMinDistanceMeasurer implements IDigitalObjectDistanceMeasurer {

	private ArrayList<DigitalObject> target;
	public ArrayList<DigitalObject> partialResult;
	FeatureManager fm; // feature manager
	double diversityFactor;
	HashMap<String,Double> objectsRelevance;
	private int numberOfDescriptors;

	
	public MMRMinMinDistanceMeasurer(int idTopic, HashMap<String,Double> objectsRelevance, double divFactor){
		this.objectsRelevance = objectsRelevance;
		fm = FeatureManager.getInstance(idTopic); // feature manager
		numberOfDescriptors =  fm.numberOfDesc;
		this.diversityFactor = divFactor;
	}
	
	public double getMeasureValue(IDigitalObject object) {
		
		//------- Calculate relevance to the query pattern			
		double relDist = objectsRelevance.get(object.getId());
		
		//------- Calculates min distance to the partial result set
		// Calculates the average distance to the first item of the partial result set
		double[] divDistances = fm.getDistance(partialResult.get(0), object);
		double minDist = Double.MAX_VALUE;
		for(int j = 0; j < numberOfDescriptors; j++)
			minDist = Math.min(minDist, divDistances[j]);
		
		//Sets the distance to the first item as the minimum distance to the partial result set
		double divDist = minDist;
		
		//Calculates the distance to the remaining items of the partial result set ant finds the minimum distance
		for(int i = 1; i < partialResult.size(); i++){
			divDistances = fm.getDistance(partialResult.get(i), object);
			minDist = Double.MAX_VALUE;
			
			for(int j = 0; j < numberOfDescriptors; j++)
				minDist = Math.min(minDist, divDistances[j]);
			
			if(minDist < divDist)
				divDist = minDist;
		}	
		
		//System.out.println("rel_dist: " + relDist);
		//System.out.println("div_dist: " + (1.0-divDist));
		//System.out.println("fin_dist: " + ((1-diversityFactor)*relDist + diversityFactor*(1.0-divDist)) + "\n");
		
		return (1-diversityFactor)*relDist + diversityFactor*(1.0-divDist);
	}
	
	public void setTarget(ArrayList<DigitalObject> target){
		this.target = target;
	}
	
	public void setPartialResult(ArrayList<DigitalObject> partialResult){
		this.partialResult = partialResult;
	}
}
