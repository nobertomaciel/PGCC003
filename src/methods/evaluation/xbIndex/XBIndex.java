package methods.evaluation.xbIndex;

import config.FeatureManager;
import curveAnalysisMethods.curveAnalysisMethods;
import interfaces.IEvaluator;
import object.DigitalObject;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;


/**
 * This enum is responsable for create a Instance Singleton patern to allow
 * calculate the XBIndex
 */

public enum XBIndex implements IEvaluator {
    INSTANCE;
    private NavigableMap<Double, Integer> valuesIndex = new TreeMap<>();
    curveAnalysisMethods curveAnalysis = new curveAnalysisMethods();

    XBIndex(){}

    /**
     * This method is responsable for return a Instance of the Enum(Singleton)
     * @return The Instance of the Enum
     */
    public static XBIndex getInstance(){ return INSTANCE;}

    /**
     * This method is responsable for assist in the process of the calculate of the XBIndex
     * @param clusters The groups formed in the clustering
     * @param fm The file of the configuration
     * @return The XB Index
     */
    @Override
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {

        double sumDistance = 0;
        int n = 0;
        for(int i = 0; i < clusters.size(); i++){ // conta todos os clusters
            ArrayList<DigitalObject> currentCluster = clusters.get(i); // pega o cluster atual (i)
            n += currentCluster.size(); // pega o tamanho do cluster atual e soma em n, o total de elementos é o n final
            for(int j = 0; j < currentCluster.size() ; j++){ // pega cada um dos elementos do cluster atual (i)
//              O elemento (0) é o central? a distância abaixo está pegando entre elementos e não entre centros de cluster
//              Além disso, está fazendo a soma das distâncias
//              fm.getAvgDistance está pegando a distância para TODOS os descritores em NUM_DESCRIPTORS
                //sumDistance += Math.pow(fm.getAvgDistance(currentCluster.get(0), currentCluster.get(j)),2); //OLD POSSÍVEL ERRO AQUI:
                sumDistance += fm.getAvgDistance(currentCluster.get(0), currentCluster.get(j));
            }
        }
        // https://cedric.cnam.fr/fichiers/art_1877.pdf
        // observar que: XB index = pi/s, onde pi é a compacidade e s é a distância mínima entre os centros de cluster k e k ao quadrado'
        // ou seja: XB = pi/(d_min)²
        // em pi, temos u_ij como sendo 1 ou 0 (elemento j pertence ou não pertence ao cluster i)
        double pi = sumDistance/n;
        double s = Math.pow(minDistanceCluster(clusters,fm),2);

//        double value = sumDistance/((n*minDistanceCluster(clusters,fm))); //POSSÍVEL ERRO AQUI

        double value = pi/s;

        if(!valuesIndex.containsKey(value))
            valuesIndex.put(value,clusters.size());

        return value;
    }


    /**
     * This method find the shorter distance among the groups
     * @param cluster The groups formed
     * @param fm The file of the configuration
     * @return The shorter distance
     */
    private double minDistanceCluster(ArrayList<ArrayList<DigitalObject>> cluster, FeatureManager fm){

        double shorterDistance = Double.MAX_VALUE;
        double distanceCentroid = 0;


        for(int i = 0; i < cluster.size(); i++){
            for(int j = 0; j < cluster.size(); j++){
                if(i!=j)
                    distanceCentroid = Math.pow(fm.getAvgDistance(cluster.get(i).get(0),cluster.get(j).get(0)),2);
                if(distanceCentroid < shorterDistance){
                    shorterDistance = distanceCentroid + 1;
                }

            }

        }
       return shorterDistance;
    }

    /**
     * This method is utilized to find the best K
     * @return the best k associated
     */
    @Override
    public NavigableMap<String, TreeMap<Integer,Double>> bestK(int iMethod) {
    //public NavigableMap<String, ArrayList<Double>> bestK(int iMethod) {
    //public int bestK(int iMethod) {

        NavigableMap<Integer,Double> mapAuxiliar = new TreeMap<>();

        for(Double key: valuesIndex.keySet()){
            mapAuxiliar.put(valuesIndex.get(key), key);
        }
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
        return valuesIndex.firstKey();
    }


}
