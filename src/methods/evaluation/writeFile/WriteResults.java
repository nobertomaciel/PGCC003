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
        String dir = System.getProperty("user.dir");
        File file = new File(dir+"/src/methods/evaluation/writeFile/results/"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");
        File fileMovieAvgCoefficient = new File(dir+"/src/methods/evaluation/writeFile/results/movieAvgCoefficient_"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");
        try{

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(fileMovieAvgCoefficient, false));
            ArrayList<Double> values;

            this.kMin = clustersNum[0];
            this.kMax = clustersNum[1];

            for(int j = this.kMin; j <= this.kMax; j++){
               bw.write(j+",");
               bw2.write(j+",");
            }

            bw.write("bestK,");
            bw2.write("bestK,");

            bw.write("time(nanoTime/TIMER_DIVISOR)");
            bw2.write("time(nanoTime/TIMER_DIVISOR)");

            bw.newLine();
            bw2.newLine();

            for(Integer key: valueMetrics.keySet()){
                Map<Integer,Long> time = new HashMap<Integer,Long>();
                int[] bestK = topicBestK.get(key-1);
                values = valueMetrics.get(key).get(methodEvaluationCurrent).getValues();
                time = topicExecutionTime.get(key-1);
                for(int j = 0; j < values.size(); j++){
                    // puxar os dados da média movel e coeficiente angular (criar array em curveAnalysisMethods.java)
                    bw.write(values.get(j)+",");
                    bw2.write(values.get(j)+",");
                }

                bw.write(bestK[methodEvaluationCurrent]+",");
                bw2.write(bestK[methodEvaluationCurrent]+",");

                bw.write(time.get(methodEvaluationCurrent).toString());
                bw2.write(time.get(methodEvaluationCurrent).toString());

                bw.newLine();
                bw2.newLine();
            }

            bw.close();
            bw2.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
