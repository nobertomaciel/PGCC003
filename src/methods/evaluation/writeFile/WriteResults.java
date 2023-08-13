package methods.evaluation.writeFile;
import exec.RunnerClusterAgglomerative;
import methods.evaluationControl.EvaluationResult;

import java.io.*;
import java.util.*;

public enum WriteResults {
    INSTANCE;
    private int kMin = 0;
    private int kMax = 0;
    WriteResults(){}

    public static WriteResults getInstance(){
        return INSTANCE;
    }

    public void writeFile(NavigableMap<Integer, ArrayList<EvaluationResult>> valueMetrics, int methodEvaluationCurrent, String descritorName, int[] clustersNum, ArrayList<Map<Integer,Long>> topicExecutionTime, ArrayList<int[]> topicBestK){
    //public void writeFile(NavigableMap<Integer, ArrayList<EvaluationResult>> valueMetrics, int methodEvaluationCurrent, String descritorName, int[] clustersNum, Map<String,Long> topicExecutionTime){
        String dir = System.getProperty("user.dir");
        //System.out.println(dir+"/src/methods/evaluation/writeFile/results/"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");
        File file = new File(dir+"/src/methods/evaluation/writeFile/results/"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");


        try{

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
            ArrayList<Double> values;

            this.kMin = clustersNum[0];
            this.kMax = clustersNum[1];

            for(int j = this.kMin; j <= this.kMax; j++){
               bw.write(j+",");
            }

            bw.write("bestK,");
            bw.write("time(nanoTime/TIMER_DIVISOR)");

            bw.newLine();

            for(Integer key: valueMetrics.keySet()){
                Map<Integer,Long> time = new HashMap<Integer,Long>();
                int[] bestK = topicBestK.get(key-1);
                values = valueMetrics.get(key).get(methodEvaluationCurrent).getValues();
                time = topicExecutionTime.get(key-1);
                for(int j = 0; j < values.size(); j++){
                    bw.write(values.get(j)+",");
                }

                bw.write(bestK[methodEvaluationCurrent]+",");
                bw.write(time.get(methodEvaluationCurrent).toString());
                bw.newLine();
            }

            bw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
