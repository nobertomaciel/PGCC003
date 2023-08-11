package interfaces;

import java.util.ArrayList;

import config.FeatureManager;
import object.DigitalObject;

public interface ISelectListFinal {
	public ArrayList<DigitalObject> selectElements(ArrayList<ArrayList<DigitalObject>> listaClusters, ArrayList<DigitalObject> inputList, FeatureManager fm);
}