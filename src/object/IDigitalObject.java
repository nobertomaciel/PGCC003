package object;

import measurer.IDigitalObjectDistanceMeasurer;

/**
 * This interface encapsulates a simple concept of digital object
 * This simple object has only an identification and a method that permits its
 * comparison to another object. The measurer is an object that encapsulates the logic 
 * of the comparison between two digital objects.
 * 
 * @author Jefersson Alex dos Santos
 * @author Rodrigo Tripodi Calumby
 *
 * @see IDigitalObject
 * @see IDigitalObjectDistanceMeasurer
 * @see Comparable
 */
public interface IDigitalObject extends Comparable<IDigitalObject> {
	
	public String getId();
	public void setId(String id);
	
	/**
	 * Compares two objects
	 */
	public int compareTo(IDigitalObject digitalObject);
	
	/**
	 * Sets the measurer to be used by the compareTo method
	 * @param measurer the measurer
	 * @see IDigitalObjectDistanceMeasurer
	 */
	public void setMeasurer(IDigitalObjectDistanceMeasurer measurer);
}