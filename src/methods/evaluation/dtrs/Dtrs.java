package methods.evaluation.dtrs;

import config.FeatureManager;
import interfaces.IEvaluator;
import object.DigitalObject;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import curveAnalysisMethods.curveAnalysisMethods;

public enum Dtrs implements IEvaluator {
    INSTANCE;
    private final NavigableMap<Double, Integer> valuesIndex = new TreeMap<>();

    curveAnalysisMethods curveAnalysis = new curveAnalysisMethods();
    Dtrs(){}

    public static Dtrs getInstance(){ return INSTANCE;}

    @Override
    public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm) {
        double risk = risk(clusters, fm, 0);
        if(!valuesIndex.containsKey(risk))
            //System.out.println("Risk....................."+risk);
            valuesIndex.put(risk,clusters.size());
        return risk;
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
    @Override
    public int bestK(int iMethod) {

        NavigableMap<Integer,Double> mapAuxiliar = new TreeMap<>();

        for(Double key: valuesIndex.keySet()){
            mapAuxiliar.put(valuesIndex.get(key), key);
        }

        ArrayList<Double> index = new ArrayList<>();

        for (Integer key: mapAuxiliar.keySet()){
             index.add(mapAuxiliar.get(key));
        }
        //int k = valuesIndex.get(valuesIndex.firstKey());
        //selecionar aqui o bestK, implementar o método e instanciar
        // utilizar o mapAuxiliar.keySet() para calcular diretamente pelo método do cotovelo e pelo método usando derivadas
        //for(Integer  key: mapAuxiliar.keySet()){
            //mapAuxiliar.get(key);
        //}
        //System.out.println(mapAuxiliar.get(2));
        //System.out.println(mapAuxiliar.keySet());
        //System.out.println(valuesIndex.keySet());

        int k = curveAnalysis.run(mapAuxiliar,iMethod);

        return k;
    }

    private double val(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm, int descriptor){
        double simSomatory = 0;
        double val;
        double sim;
        int images = 0;
        for (ArrayList<DigitalObject> cluster : clusters) {
            images += cluster.size();
            for (ArrayList<DigitalObject> digitalObjects : clusters) {
                double d = this.getClusterSumDistance(cluster, digitalObjects, fm, descriptor);
                // considerar a distância máxima e mínima?
                if(d > 0){sim = 1/d;}
                else{sim = 1;}
                simSomatory += sim;
            }
        }
        //System.out.printf("SimSomatory.................................%f\n",simSomatory);
        //System.out.printf("Images......................................%d\n",images);
        val = ((1.0/(images^2))*simSomatory);
        //return val/100; // qualificação
        return val; // dissertação
    }

    private double risk(ArrayList<ArrayList<DigitalObject>> clusters, FeatureManager fm, int descriptor){
        double sim;
        double risk = 0;
        double val = this.val(clusters,fm,descriptor);
        int size = clusters.size();
        ArrayList <object.DigitalObject> images_ci;
        ArrayList <object.DigitalObject> images_cj;
        object.DigitalObject xi;
        object.DigitalObject xj;
        for(int ci = 0; ci < size; ci++){
            images_ci = clusters.get(ci);
            for(int cj = 0; cj < size; cj++){
                images_cj = clusters.get(cj);
                for(int i = 0; i< images_ci.size(); i++){
                    xi = images_ci.get(i);
                    for(int j = 0; j<images_cj.size(); j++){
                        xj = images_cj.get(j);
                        double d = fm.getDistance(xi,xj,descriptor);
                        if(d > 0){sim = 1/d;}
                        else{sim = 1;}

                        //System.out.printf("\nSimilaridade(%s,%s)................%f\n",xi.getId(),xj.getId(),sim);
                        //System.out.printf("Val.................................%f\n",val);

                        if(sim >= val && ci == cj){
                            risk += (0.5-((sim-val)/(2-2*val)));
                        }
                        else if(sim < val && ci == cj){
                            risk += (0.5+((val-sim)/(2*val)));
                        }
                        else if(sim >= val && ci != cj){
                            risk += (0.5+((sim-val)/(2-2*val)));
                        }
                        else if(sim < val && ci != cj){
                            risk += (0.5-((val-sim)/(2*val)));
                        }
                    }
                }
            }
        }

        //risk = 1-(100/risk);
        //risk = (1/risk);
        //System.out.format("\nRisk..............................: %f%n",risk);
        return risk;
    }
    //soma das distâncias da imagem i para todas as outras imagens do cluster j
    private double getClusterSumDistance(ArrayList<DigitalObject> cluster_i,ArrayList<DigitalObject> cluster_j,FeatureManager fm, int descriptor) {
        double distance = 0;
        for(int i = 0 ; i < cluster_i.size(); i++) {
            for(int j = 0 ; j < cluster_j.size(); j++) {
                if(!Objects.equals(cluster_i.get(i).getId(), cluster_j.get(j).getId())){
                    distance += fm.getDistance(cluster_i.get(i), cluster_j.get(j),descriptor);
                }
            }
        }
        return distance;
    }

}
