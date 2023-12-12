package methods.evaluation.daviesBouldin;

import config.FeatureManager;
import curveAnalysisMethods.curveAnalysisMethods;
import interfaces.IEvaluator;
import object.DigitalObject;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

public enum DaviesBouldin implements IEvaluator {
    INSTANCE;
    NavigableMap<Double, Integer> valuesIndex = new TreeMap<>();
    curveAnalysisMethods curveAnalysis = new curveAnalysisMethods();

    DaviesBouldin(){ }

    public static DaviesBouldin getInstance(){ return DaviesBouldin.INSTANCE; }

    public static double max(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    @Override
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {
        double db = 0;
        for(int i = 0; i < clusters.size(); i++) {
            double[] maximum = new double[clusters.size()];
            for(int j = 0; j < clusters.size(); j++) {
                maximum[j] = Double.NEGATIVE_INFINITY;
                if(i!=j){
                    double distance = this.getDistance(clusters.get(i), clusters.get(j), fm);
                    maximum[j] = distance;
                }
            }
            db += max(maximum);
        }

        double value = (db/clusters.size());

        if(Double.isInfinite(value)){
            //System.out.println("DB Index -Infinity **************************************************");
            //System.out.println("clusters.size(): "+clusters.size());
            //System.out.println("value: "+value);
            //System.out.println("db: "+db);
            value = 0;
            //System.out.println("Assuming value=0 ****************************************************");
        }

        if(!valuesIndex.containsKey(value))
            valuesIndex.put(value,clusters.size());

        return value;
    }

    @Override
    public NavigableMap<String, TreeMap<Integer,Double>> bestK(int iMethod) {
        NavigableMap<Integer,Double> mapAuxiliar = new TreeMap<>();
        NavigableMap<String,TreeMap<Integer,Double>> returnArr;
        for(Double key: valuesIndex.keySet()){
            mapAuxiliar.put(valuesIndex.get(key), key);
        }


        returnArr = curveAnalysis.run(mapAuxiliar, iMethod);

        return returnArr;
    }
    @Override
    public void reset() {
        valuesIndex.clear();
    }
    @Override
    public double value() {
        //return valuesIndex.get(valuesIndex.firstKey());
        return valuesIndex.firstKey();
    }

    private double getDistance(ArrayList<DigitalObject> cluster1,
                               ArrayList<DigitalObject> cluster2, FeatureManager fm) {

        double indexDB = 0.0;
        if(cluster1.size() > 1 && cluster2.size() > 1){

            double si = 0.0;
            for(int j = 0; j < cluster1.size(); j++) {
                si += Math.pow(fm.getAvgDistance(cluster1.get(0), cluster1.get(j)),2);
                //System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
            }
            si = Math.sqrt(si);
            si /= cluster1.size();


            double sj = 0.0;
            for(int j = 0; j < cluster2.size(); j++) {
                sj += Math.pow(fm.getAvgDistance(cluster2.get(0), cluster2.get(j)),2); //Calcula o desvio padrao
                //System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
            }
            sj = Math.sqrt(sj);
            sj /= cluster2.size();


            double distanceCentroids;
            if(!Double.isNaN(si) && !Double.isNaN(sj)){
                //Calcula a distância entre os centroids
                distanceCentroids = fm.getAvgDistance(cluster1.get(0), cluster2.get(0));
                double Rij = ((si + sj)/distanceCentroids);
                return Rij;
                //return Math.abs((si + sj)/distanceCentroids);
                      //System.out.println("O valor do index DB "+ indexDB +" Para "+ distanceCentroids);
            }else{
                indexDB = 0;

            }
        }
        return indexDB;
    }

}
