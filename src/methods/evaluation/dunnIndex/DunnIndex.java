package methods.evaluation.dunnIndex;

import config.FeatureManager;
import curveAnalysisMethods.curveAnalysisMethods;
import interfaces.IEvaluator;
import object.DigitalObject;

import java.util.*;


/**
 * This enum is responsable for create a Instance Singleton patern to allow
 * calculate the Dunn Index
 */

public enum DunnIndex implements IEvaluator {
    INSTANCE;
    private static double distanceIntra;
    private NavigableMap<Double, Integer> valuesIndex = new TreeMap<>();
    curveAnalysisMethods curveAnalysis = new curveAnalysisMethods();

      DunnIndex(){

    }

    /**
     * This Static method is responsable for return a Instance of the Enum(Singleton)
     * As well as assist in the calculate of the intra distance
     * @return An Instance of Enum
     */

    public static DunnIndex getInstance(){
        return INSTANCE;
    }

    /**
     * This method is responsable for assist in the process of the calculate of the Dunn Index
     * @param clusters The groups formed in the clustering
     * @param fm The file of the configuration
     * @return The Dunn index
     */
    @Override
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {
        double jminimum = Double.MAX_VALUE;
        double iminimum = Double.MAX_VALUE;
        this.distanceIntra = getDistanceIntra(clusters, fm); // não deveria ser distância média? não utilizo
        double distanceCluster;
        double distance;

        for (int i = 0; i < clusters.size(); i++){
            distanceCluster = this.getDistanceCluster(clusters.get(i), fm);
            for(int j = 0; j < clusters.size(); j++){
                if(i != j){
                    distance = this.getDistance(clusters.get(i), clusters. get(j), fm);
                    if (distance < jminimum){
                        jminimum = (distance/distanceCluster);
                    }
                }
            }
            if (jminimum < iminimum){
                iminimum = jminimum;
            }
        }

        if(!valuesIndex.containsKey(iminimum))
            valuesIndex.put(iminimum,clusters.size());

        return iminimum;
    }


    /**
     * The method find the biggest key. The key represent the value of the best k
     * @return The best value of the k
     */
    @Override
    public NavigableMap<String, TreeMap<Integer,Double>> bestK(int iMethod) {
    //public NavigableMap<String, ArrayList<Double>> bestK(int iMethod) {
    //public int bestK(int iMethod) {
        NavigableMap<Integer,Double> mapAuxiliar = new TreeMap<>();

        for(Double key: valuesIndex.keySet()){
            mapAuxiliar.put(valuesIndex.get(key), key);
        }
        //return valuesIndex.get(valuesIndex.lastKey());
        NavigableMap<String,TreeMap<Integer,Double>> returnArr = curveAnalysis.run(mapAuxiliar, iMethod);
        //double kd = returnArr.get("k").get(1);
        //int k = (int)kd;
        ////int k = curveAnalysis.run(mapAuxiliar, iMethod);
        //return k;
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
        double distance = 0;

        for(int i = 0; i < cluster1.size(); i++){
            for (int j = 0; j < cluster2.size(); j++){
                distance += fm.getAvgDistance(cluster1.get(i), cluster2.get(j));
                //System.out.println("Distancia entre a imagem "+cluster1.get(i).getId() +" e "+ cluster2.get(j).getId());
            }

        }

        return (distance/(cluster1.size()*cluster2.size()))/this.getDistanceIntra();

    }

    private double getDistanceCluster(ArrayList<DigitalObject> cluster, FeatureManager fm) {
        double distance = 0;
        for(int i = 0; i < cluster.size(); i++){
                distance += fm.getAvgDistance(cluster.get(0), cluster.get(i));
        }
        return (distance/(cluster.size()));
    }

    /**
     * This method calculate the Intra distance among the object of all the clusters;
     * @param clusters All the clusters formed
     * @param fm The file of the configuration, utilized for find the distances between the objects
     * @return The maximum distance between the objects
     */
    private static double getDistanceIntra(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm){

        double maximumDiameter = -Double.MIN_VALUE;
        double diameter = 0;

        for(int i = 0; i < clusters.size(); i++){
            ArrayList<DigitalObject> currentCluster = clusters.get(i);
            for(int j = 0; j < currentCluster.size(); j++){
                for (int k = 0; k < currentCluster.size(); k++){
                    if (j != k)
                        diameter = fm.getAvgDistance(currentCluster.get(j), currentCluster.get(k)); //or (j+k) +1
                    if(diameter > maximumDiameter)
                        maximumDiameter = diameter;
                }

            }
        }
        return maximumDiameter;
    }


    private double getDistanceIntra(){return distanceIntra;}

}
