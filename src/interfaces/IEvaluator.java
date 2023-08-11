package interfaces;

import config.FeatureManager;
import object.DigitalObject;

import java.util.ArrayList;

public interface IEvaluator {

    double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm);

    double value();
    //int calculateBestK(int i);
    int bestK(int i) ;
    void reset();



}
