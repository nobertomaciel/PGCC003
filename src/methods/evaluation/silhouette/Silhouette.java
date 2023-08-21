package methods.evaluation.silhouette;

import config.FeatureManager;
import curveAnalysisMethods.curveAnalysisMethods;
import interfaces.IEvaluator;
import object.DigitalObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

public enum Silhouette implements IEvaluator {
    INSTANCE;
    NavigableMap<Double, Integer> valuesIndex = new TreeMap<>() ;
    curveAnalysisMethods curveAnalysis = new curveAnalysisMethods();

    Silhouette(){  }

    public static Silhouette getInstance(){
        return Silhouette.INSTANCE;
    }

    @Override
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {

        double silhouette = 0.0;
        double overallImages = 0.0;
        for(int i = 0; i < clusters.size()-1; i++) {
            silhouette += silhouette(clusters.get(i), clusters, fm, i);
            overallImages += clusters.get(i).size();
        }

        Double indice = silhouette/overallImages;
        indice = (indice.isNaN() ? 0.0 : indice);
        //System.out.format("Silhouette........................: %f%n",indice);
        if(!valuesIndex.containsKey(indice))
            valuesIndex.put(indice,clusters.size());

        if(overallImages != 0) // Caso em que k = 1
            return indice ;
        return -1.0;
    }



    private double silhouette(ArrayList<DigitalObject> cluster1,
                              ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm, int currentCluster) {
        double maximumDissimilarityClusters = 0.0;
        double distanceInternal = 0.0;
        double silhouette = 0.0;
        for(int i = 0; i < cluster1.size(); i++) {
            if(cluster1.size() > 1){
                for(int j = 0; j < cluster1.size(); j++) {
                    distanceInternal += fm.getAvgDistance(cluster1.get(i), cluster1.get(j)); //calcula a distância da imagem i para todas as outras imagens j do cluster (a)
                }
                distanceInternal /= cluster1.size(); //calcula a dissimilaridade média da imagem i
                maximumDissimilarityClusters = maximumDissimilarityClusterGA(i,clustering, currentCluster, fm); //calcula o valor da dissimilaridade entre a imagem i para todos os outros clusters (a para b)
                double value = 0;
                if(distanceInternal < maximumDissimilarityClusters){
                    value = 1 - (distanceInternal/maximumDissimilarityClusters);
                    silhouette += value;
                }else if(distanceInternal == maximumDissimilarityClusters){
                    silhouette += 0.0;
                }else{
                    value = (maximumDissimilarityClusters/distanceInternal) - 1;
                    silhouette += value;
                }
            }else{
                silhouette += 0.0;
            }
        }
        return silhouette;
    }


    private double maximumDissimilarityClusterGA(int imageI,
                                                 ArrayList<ArrayList<DigitalObject>> clustering, int currentCluster,
                                                 FeatureManager fm) {

        double maximum = Double.MIN_VALUE;
        //calcula a maior distância de similaridade da imagem i do cluster atual para todos os outros clusters
        //encontra o cluster vizinho
        for(int j = 0; j < clustering.size(); j++) {
            if(j == currentCluster){
                //não deve ser calculado, já foi calculado na similaridade interna do cluster
                continue; // inserido por noberto
            }else{
                double distance = this.getDistance(clustering.get(currentCluster), imageI, clustering.get(j), fm);
                if(distance > maximum){//maior distância da imagem i do cluster A para os demais clusters
                    maximum = distance;
                }
            }
        }
        return maximum;
    }


    private double getDistance(ArrayList<DigitalObject> currentCluster, int imageObject,
                               ArrayList<DigitalObject> otherClusters, FeatureManager fm) {

        double distance = 0.0;
        for(int j = 0 ; j < otherClusters.size(); j++) {
            //soma das distâncias da imagem i para todas as outras imagens do cluster B
            distance += fm.getAvgDistance(currentCluster.get(imageObject), otherClusters.get(j));
        }
        //média da distância da imagem i para todas as outras do cluster B
        return distance/otherClusters.size();

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
    public void reset(){valuesIndex.clear();}
    @Override
    public double value() {
        return valuesIndex.firstKey();
    }



}
