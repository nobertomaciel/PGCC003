package interfaces;

import config.FeatureManager;
import object.DigitalObject;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

public interface IEvaluator {

    double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm);

    double value();
    //int calculateBestK(int i);
    //NavigableMap<String, ArrayList<Double>> bestK(int i);
    NavigableMap<String, TreeMap<Integer,Double>> bestK(int i);
    //int bestK(int i) ;
    void reset();



}
