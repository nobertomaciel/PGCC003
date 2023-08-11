package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public interface ELinkage {

	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm);
	public double runGAEvaluation(ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm, XReal chromosome);
}
