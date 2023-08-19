package methods.evaluationControl;
import config.FeatureManager;
import interfaces.IEvaluator;
import methods.evaluation.writeFile.WriteResults;
import object.DigitalObject;

import java.util.*;

public class EvaluationControl {
    /**
     * Armazena todos os grupos que foram formados, na ordem
     * k -> quantidade de grupo correpondente
     * Clustering -> os grupos que foram formados
     * Objetos - > objetos pertencentes ao grupo
     * K->Clustering->Objetos
     */

    private ArrayList<ArrayList<ArrayList<DigitalObject>>> clusterings;

    /**
     * Armazena o resultado dos métodos de avaliação separando-os por tópico
     * A chave indica o tópico em execucao.
     * O valor consiste um array cujo objeto de entrada é da classe EvaluationResult.
     * Este, por sua vez, possui o método de avaliação e os valores medidos para
     * o método atual.
     */
    private NavigableMap<Integer,ArrayList<EvaluationResult>> resultEvaluation;

   public EvaluationControl(){
       this.resultEvaluation = new TreeMap<>();
       this.clusterings = new ArrayList<>();
   }

/*    public Map<Integer,Integer> findBestK(int k, int clusteringAlgorithm, int totalTopic, IEvaluator evaluation){

        Map<Integer,Integer> bestK = new HashMap<>(); // Armazena o valor do tópico com o seu melhor valor de k
        NavigableMap<Double,Integer> auxiliarBestK = new TreeMap<>(); // Auxilia no momento de encontrar o melhor k
        ArrayList<ArrayList<DigitalObject>> clusters = null; //Armazena os clusters pós processo de clustering
        ILinkage referenceAlgorithm;


        switch(clusteringAlgorithm){
            case 1:	referenceAlgorithm = new CompleteLink(); break;
            case 2:	referenceAlgorithm = new MedianMethod(); break;

        }

        for(int i = 1; i < totalTopic; i++){
            clusters = null;
            for(int j = 2; j < k; j++){
                //clusters = linkage.algoritmo de clustering; //Define isso depois de escolher o algoritmo
                auxiliarBestK.put(evaluation.runEvaluation(null, FeatureManager.getInstance(i)),j);
                if(evaluation instanceof GapStatistic || evaluation instanceof SumSquaredError){
                    if(evaluation.bestK(auxiliarBestK)!= 0){
                       break;
                    }
                }
            }
            bestK.put(i, evaluation.bestK(auxiliarBestK));
        }
        return bestK;

        return null;
    }*/

    public void setClustering(ArrayList<ArrayList<ArrayList<DigitalObject>>> cluster){
       this.clusterings = cluster;

    }


    public NavigableMap<Integer,ArrayList<EvaluationResult>> runEvaluation(ArrayList<IEvaluator> methods, FeatureManager fm, int idTopic){
       EvaluationResult evaluationResult;
       ArrayList<EvaluationResult> evaluationResults = new ArrayList<>();

       for(int i = 0; i < methods.size(); i++){
           evaluationResult = new EvaluationResult(methods.get(i));
           //Aqui vem o laço de repetição para os tópicos
           for(int k = 0; k < clusterings.size(); k++){
               //System.out.println("Repeating topics:.......................... k="+k);
               evaluationResult.setValues(evaluationResult.getMethod().runEvaluation(clusterings.get(k),fm));

           }
           evaluationResults.add(evaluationResult); //Armazena os tópicos com as medidas de avaliações

       }
      resultEvaluation.put(idTopic, evaluationResults); //Associa a cada tópico todas as medidas de avaliações;

      return resultEvaluation;

    }

    public void writeFile(int methodEvaluationCurrent, String descritorName, int[] clustersNum, ArrayList<Map<Integer,Long>> topicExecutionTime, ArrayList<int[]> topicBestK, NavigableMap<Integer,TreeMap<Integer,TreeMap<String,TreeMap<Integer,Double>>>> allData){
    //public void writeFile(int methodEvaluationCurrent, String descritorName, int[] clustersNum, Map<String,Long> topicExecutionTime){
       //System.out.println(descritorName);
       WriteResults.getInstance().writeFile(resultEvaluation, methodEvaluationCurrent,descritorName,clustersNum,topicExecutionTime, topicBestK, allData);
   }


   // qual motivo deste método estar aqui com retorno = 0?????
/*   public int bestk(){

       return  0;
   }*/

}
