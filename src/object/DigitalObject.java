/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package object;

import measurer.IDigitalObjectDistanceMeasurer;

/**
 * This class implements a simple digital object that has an id and a simple method 
 * of comparison between two objects that uses a measurer (IDigitalObjectDistanceMeasurer).
 * 
 * @author Jefersson Alex dos Santos
 * @author Rodrigo Tripodi Calumby
 * 
 * @see IDigitalObject
 * @see IDigitalObjectDistanceMeasurer
 */
public class DigitalObject implements IDigitalObject {
    private String id;
    private IDigitalObjectDistanceMeasurer measurer;

	public DigitalObject(String id){
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id value to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Sets the measurer to be used
     * @param measurer the measurer
     * 
     * @see IDigitalObjectDistanceMeasurer
     */
    public void setMeasurer(IDigitalObjectDistanceMeasurer measurer){
    	this.measurer = measurer;
    }
    
    /**
     * Compares two objects based on the measure values from the current and the argument objects.
     * This measure values are obtained using a measurer that needs to be explicitly set using the setMeasurer method.
     * @param iDigitalObject the object to be compared to the current
     * @return 1 if this object has a higher measure value, -1 if the argument object has the higher measure value 
     * or 0 if both of them have the same measure value.
     * 
     * @see IDigitalObject
     * @see IDigitalObjectDistanceMeasurer
     */
	public int compareTo(IDigitalObject iDigitalObject){
		double thisMeasure = measurer.getMeasureValue(this);
		double argumentObjectMeasure = measurer.getMeasureValue(iDigitalObject);
		
		if(thisMeasure > argumentObjectMeasure)
			return 1;
		else
			if (thisMeasure == argumentObjectMeasure)
				return 0;
		
		return -1;
	}
}