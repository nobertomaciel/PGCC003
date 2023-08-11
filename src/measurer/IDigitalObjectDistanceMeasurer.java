package measurer;

import object.IDigitalObject;

/**
 * This interface allows an object to receive a measure value of any nature that
 * makes possible its comparison to another object. For example, a distance
 * value to a target object. Another application could be the calculation of a
 * distance value from a digital object to a whole set of digital objects.
 * 
 * This measure value is useful to the comparison between objects
 * based on a given measure.
 * 
 * @author Jefersson Alex dos Santos
 * @author Rodrigo Tripodi Calumby
 * 
 */

public interface IDigitalObjectDistanceMeasurer {
	double getMeasureValue(IDigitalObject digitalObject);
}