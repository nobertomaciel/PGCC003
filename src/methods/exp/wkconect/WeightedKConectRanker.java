package methods.exp.wkconect;

import java.util.ArrayList;
import java.util.Collections;

import interfaces.IDiversify;
import interfaces.IFeatureManager;
import object.DigitalObject;

public class WeightedKConectRanker implements IDiversify{
	
 /** Método para ordenação baseada nos vizinhos mais próximos
 * @see interfaces.IDiversify#run(interfaces.IFeatureManager, java.util.ArrayList)
 */
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,	ArrayList<DigitalObject> inputList, int topicID, String locName) {
		System.out.println("....Running WeightedKConectRanker: " + topicID);
		
		WeightedKConnectDistanceMeasurer measurer = new WeightedKConnectDistanceMeasurer(topicID);
		measurer.setTarget(inputList);
		
		for (DigitalObject digitalObject : inputList)
			digitalObject.setMeasurer(measurer);
		
		Collections.sort(inputList);
		
		return inputList;		
	}
}