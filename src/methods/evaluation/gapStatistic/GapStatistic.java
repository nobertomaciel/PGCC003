package methods.evaluation.gapStatistic;

import config.FeatureManager;
import interfaces.IEvaluator;
import methods.evaluation.sumSquaredError.SumSquaredError;
import object.DigitalObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * This Enum is responsable to calculate the GapStatistic of the clustering
 */
public enum GapStatistic implements IEvaluator {
    INSTANCE;
    private ArrayList<Double> standardDeviation = new ArrayList<>();
    private int indexClusterB = 0;
    private NavigableMap<Double,Integer> valuesIndex = new TreeMap<>();


    GapStatistic(){}

    /**
     * This method is responsable for return a Instance of the Enum(Singleton)
     * @return The Instance of the Enum
     */
    public static GapStatistic getInstance(){ return INSTANCE; }


    /**
     * This method is responsable for assist in the process of the calculate of the gap statistic
     * @param clusters
     * @param fm
     * @return
     */
    @Override
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {

        double wk = calculateWK(clusters,fm);
        double [] wkb = calculateWKB(fm, clusters) ;

        double value = gapStatistic(wk, wkb, clusters);
        valuesIndex.put(value,clusters.size());
        return value;

    }


    private double calculateWK(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm){
        double sumDistance;
        double wk = 0;
        ArrayList<DigitalObject> currentCluster;

        for(int i = 0; i < clusters.size(); i++){
            currentCluster = clusters.get(i);
            sumDistance = getDistance(currentCluster, fm);
            wk += sumDistance/(2*currentCluster.size());
        }

        //SumSquaredError.getInstance().runEvaluation(clusters,fm);
        return wk;
    }

    private double [] calculateWKB(FeatureManager fm, ArrayList<ArrayList<DigitalObject>> clusters){
        ArrayList<ArrayList<ArrayList<ArrayList<DigitalObject>>>> clusteringBootStraping = BootStrap.getInstance().getClusteringBootStraping();

        double [] wkb = new double[clusteringBootStraping.size()];

        for(int i = 0; i < clusteringBootStraping.size(); i++){
            wkb[i] = calculateWK(clusteringBootStraping.get(i).get(indexClusterB), fm);
        }

        indexClusterB++;

        return wkb;
    }
    /**
     * This method find the partial distance for the specific cluster
     * @param cluster with objects to find distance among them
     * @param fm the file of the configuration
     * @return the distance of cluster (parameter)
     */
    private double getDistance(ArrayList<DigitalObject> cluster,  FeatureManager fm){

        double partialDistance = 0;
        for(int i = 0; i < cluster.size(); i++){
            for (int j = 0; j < cluster.size(); j++)
                partialDistance += fm.getAvgDistance(cluster.get(i), cluster.get(j));
        }

        return partialDistance;
    }



    /**
     * This method is responsable for calculate the GapStatistic of the Clustering
     * @param wk sum of the distances among the clusters of dataset initial
     * @param wkb sum of the distances among the clusters of dataset bootstraping
     * @param clusters clusters formed
     * @return the value for the gap statistic
     */
    private double gapStatistic(double wk, double [] wkb, ArrayList<ArrayList<DigitalObject>> clusters){

        double gap = 0;
        double w;
        double sumLogWkb = 0;
        double [] log10wkb = new double[wkb.length];
        double sumToSd = 0;

        for (int i = 0; i < wkb.length; i++){

            if(wkb[i] == 0 || wk == 0){
                log10wkb[i] = 0;
                gap += 0;
                sumLogWkb += 0;
            }
            else {
                log10wkb[i] = Math.log10(wkb[i]);
                gap += log10wkb[i] - Math.log10(wk);
                sumLogWkb += log10wkb[i];
            }

        }
        w = sumLogWkb/wkb.length;

        for(int j = 0; j < wkb.length; j++){
            sumToSd += Math.pow(log10wkb[j]- w,2);
        }
        setStandardDeviation(Math.sqrt(sumToSd/wkb.length)* Math.sqrt(1+(1/wkb.length)));


        return gap/wkb.length;
    }

    private void setStandardDeviation(double value){
        this.standardDeviation.add(value);
    }

    /*This methods suppose that the map is ordering(sort) by value*/
    /**
     * The method find the biggest k. The value of the map represent the value of the best k
     * @return The best value of the k
     */
    @Override
    public int bestK(int iMethod) {
//        double current;
//        double previous = 0.0;
//        int bestK = -1;
//        int indice = 1;
//
//        for (Double key: valuesIndex.keySet()){
//            current = (valuesIndex.get(key));
//
//            if(!(previous == 0.0)){
//                if(previous >= current + standardDeviation.get(indice)){
//                    return bestK;
//                }
//
//                indice++;
//
//            }
//
//            System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> k = "+ key);
//            System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> bestk = "+ bestK);
//            System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> current = "+ current);
//            System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> previous = "+ previous);
//
//            previous = current;
//            bestK = key;
//
//
//        }
        return valuesIndex.get(valuesIndex.lastKey()); //In this case, indicate some problem
    }

    @Override
    public void reset() {
        valuesIndex.clear();
    }
    @Override
    public double value() {return valuesIndex.get(valuesIndex.firstKey());}
    public void resetDeviation(){
        standardDeviation.clear();
    }

    public void setIndexClusterB(){
        this.indexClusterB = 0;
    }

}
