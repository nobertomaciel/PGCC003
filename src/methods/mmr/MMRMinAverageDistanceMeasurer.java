package methods.mmr;

import java.util.ArrayList;
import java.util.HashMap;

import config.FeatureManager;
import measurer.IDigitalObjectDistanceMeasurer;
import object.DigitalObject;
import object.IDigitalObject;

/**
 * This class returns MMR distance as the average distance from the target to the partial result set using 
 * all available terminals
 * @author rtripodi
 *
 */
public class MMRMinAverageDistanceMeasurer implements IDigitalObjectDistanceMeasurer {

	public ArrayList<DigitalObject> partialResult;
	FeatureManager fm;
	double diversityFactor;
	HashMap<String,Double> objectsRelevance;
	private int numberOfDescriptors;

	
	public MMRMinAverageDistanceMeasurer(int idTopic, HashMap<String,Double> objectsRelevance, double divFactor){
		this.objectsRelevance = objectsRelevance;
		fm = FeatureManager.getInstance(idTopic);
		numberOfDescriptors =  fm.numberOfDesc;
		this.diversityFactor = divFactor;
	}
	
	public double getMeasureValue(IDigitalObject object) {
		
		//------- Calculate relevance to the query pattern			
		double relDist = objectsRelevance.get(object.getId());
		
		//------- Calculates min distance to the partial result set
		// Calculates the average distance to the first item of the partial result set
		double[] divDistances = fm.getDistance(partialResult.get(0), object);
		double meanDivDist = 0;
		for(int j = 0; j < numberOfDescriptors; j++){
			meanDivDist += divDistances[j];
		}
		meanDivDist /= numberOfDescriptors;		
		
		//Sets the distance to the first item as the minimum distance to the partial result set
		double divDist = meanDivDist;
		
		//Calculates the distance to the remaining items of the partial result set ant finds the minimum distance
		for(int i = 1; i < partialResult.size(); i++){
			divDistances = fm.getDistance(partialResult.get(i), object);
			meanDivDist = 0;
			for(int j = 0; j < numberOfDescriptors; j++){
				meanDivDist += divDistances[j];
			}
			meanDivDist /= numberOfDescriptors;
			
			if(meanDivDist < divDist)
				divDist = meanDivDist;
		}	
		
		//System.out.println("rel_dist: " + relDist);
		//System.out.println("div_dist: " + (1.0-divDist));
		//System.out.println("fin_dist: " + ((1-diversityFactor)*relDist + diversityFactor*(1.0-divDist)) + "\n");
		
		return (1-diversityFactor)*relDist + diversityFactor*(1.0-divDist);
	}
	
	public void setTarget(ArrayList<DigitalObject> target){
		// TODO Auto-generated method stub
	}
	
	public void setPartialResult(ArrayList<DigitalObject> partialResult){
		this.partialResult = partialResult;
	}
}
