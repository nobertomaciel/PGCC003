package methods.kmeans.outputGenerator;

import java.util.ArrayList;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

/**
 * This class returns MMR distance as the average distance from the target to the partial result set using 
 * all available terminals
 * @author rtripodi
 *
 */
public class PositionMeasurer implements IDigitalObjectDistanceMeasurer {

	ArrayList<String> inputList;
		
	public PositionMeasurer(ArrayList<String> inputList){
		this.inputList = inputList;
	}
	
	public double getMeasureValue(IDigitalObject object) {
		return this.inputList.indexOf(object.getId());
	}
}