package interfaces;

import java.util.ArrayList;

import object.DigitalObject;

public interface IDiversify {
 public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idTopic, String locName);




}