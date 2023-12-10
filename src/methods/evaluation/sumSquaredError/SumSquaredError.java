package methods.evaluation.sumSquaredError;

import config.FeatureManager;
import interfaces.IEvaluator;
import object.DigitalObject;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

import curveAnalysisMethods.curveAnalysisMethods;
public enum SumSquaredError implements IEvaluator {
    INSTANCE;
    private NavigableMap<Double, Integer> valuesIndex = new TreeMap<>();
    curveAnalysisMethods curveAnalysis = new curveAnalysisMethods();
    SumSquaredError(){}

    public static SumSquaredError getInstance(){ return INSTANCE;}

    @Override
//    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {
//        double sum = 0;
//        for(int i = 0; i < clusters.size(); i++) {
//            sum += this.getSk(clusters.get(i),fm);
//        }
//        double sse = sum/2;
//        if(!valuesIndex.containsKey(sse))
//            valuesIndex.put(sse,clusters.size());
//        return sse;
//    }
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {

        double sse = 0;
        double distance;
        for(int i = 0; i < clusters.size(); i++) {
            distance = this.getDistance(clusters.get(i),fm);
            sse += distance;
        }

        if(!valuesIndex.containsKey(sse))
            valuesIndex.put(sse,clusters.size());
        return sse;

    }

    @Override
    public void reset() {
       valuesIndex.clear();
    }
    @Override
    public double value() {
        return valuesIndex.firstKey();
    }

    @Override
//    public int bestK(int iMethod) {
//        NavigableMap<Integer,Double> mapAuxiliar = new TreeMap<>();
//
//        for(Double key: valuesIndex.keySet()){
//            mapAuxiliar.put(valuesIndex.get(key), key);
//        }
//        int k = curveAnalysis.run(mapAuxiliar, iMethod);
//        return k;
//    }
    public NavigableMap<String, TreeMap<Integer,Double>> bestK(int iMethod) {


        NavigableMap<Integer,Double> mapAuxiliar = new TreeMap<>();
        NavigableMap<String,TreeMap<Integer,Double>> returnArr;

        for(Double key: valuesIndex.keySet()){
            mapAuxiliar.put(valuesIndex.get(key), key);
        }

        ArrayList<Double> index = new ArrayList<>();

        for (Integer key: mapAuxiliar.keySet()){
             index.add(mapAuxiliar.get(key));
        }

        double coefficient = coefficientVariation(index);

        for(int i = 0; i < index.size()-1; i++){
            if((Math.abs(index.get(i) - index.get(i+1))) < coefficient)

                valuesIndex.get(index.get(i)); // duplicado com a linha abaixo, retirar

        }

        returnArr = curveAnalysis.run(mapAuxiliar, iMethod);

        return returnArr;
    }


    private double coefficientVariation(ArrayList<Double> values){

        double mean = 0;
        double sum = 0;

        for(int i = 0; i < values.size(); i++){
            mean += values.get(i);
        }
        mean/= values.size();
        for(int i = 0; i < values.size(); i++){
            sum += Math.pow(values.get(i)-mean, 2);
        }

        return  Math.sqrt(sum/values.size())/mean;

    }
    private double getSk(ArrayList<DigitalObject> cluster, FeatureManager fm) {
        double sk = 0.0;
        for(int j = 0; j < cluster.size(); j++) {
            sk += fm.getAvgDistance(cluster.get(0), cluster.get(j)); // Xi - Xj
        }
        double Nk2 = Math.pow(cluster.size(),2);
        return sk/Nk2;
    }
    private double getUkj(ArrayList<DigitalObject> cluster, FeatureManager fm) {
        double ukj = 0.0;
        for(int j = 0; j < cluster.size(); j++) {
            ukj += fm.getAvgDistance(cluster.get(0), cluster.get(j));
        }
        return ukj/Math.pow(cluster.size(),2);
    }
    private double getDistance(ArrayList<DigitalObject> cluster, FeatureManager fm) {
        double minimum = Double.MAX_VALUE;
        double centroid = 0.0;
        int centroidIndex = 0;
        for(int i = 0; i < cluster.size(); i++) {
            for(int j = 0; j < cluster.size(); j++) {
                centroid += fm.getAvgDistance(cluster.get(i), cluster.get(j));
            }
            centroid /= cluster.size();

            if(centroid < minimum){
                centroidIndex = i;
                minimum = centroid;
            }
            centroid = 0;
        }

        centroid = minimum;

        double sse = 0.0;
        for(int j = 0; j < cluster.size(); j++) {
            sse += Math.pow(fm.getAvgDistance(cluster.get(0), cluster.get(j)) - centroid,2);
            //sse += Math.pow(fm.getAvgDistance(cluster.get(centroidIndex), cluster.get(j)),2);
        }
        return sse;

    }
}
