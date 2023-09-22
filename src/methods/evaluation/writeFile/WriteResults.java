package methods.evaluation.writeFile;
import exec.RunnerClusterAgglomerative;
import methods.evaluationControl.EvaluationResult;

import java.io.*;
import java.util.*;

public enum WriteResults {
    INSTANCE;
    WriteResults(){}

    public static WriteResults getInstance(){
        return INSTANCE;
    }

    public void writeFile(NavigableMap<Integer, ArrayList<EvaluationResult>> valueMetrics, int methodEvaluationCurrent, String descritorName, int[] clustersNum, ArrayList<Map<Integer,Long>> topicExecutionTime, ArrayList<int[]> topicBestK, NavigableMap<Integer,TreeMap<Integer,TreeMap<String,TreeMap<Integer,Double>>>> allData){
        String dir = System.getProperty("user.dir");
        File file = new File(dir+"/src/methods/evaluation/writeFile/results/evaluation_"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");
        File fileMovieAvg = new File(dir+"/src/methods/evaluation/writeFile/results/movingAvg_"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");
        File fileCoefficient = new File(dir+"/src/methods/evaluation/writeFile/results/coefficient_"+valueMetrics.get(valueMetrics.firstKey()).get(methodEvaluationCurrent).getMethod().getClass().getSimpleName()+descritorName+".txt");
        try{

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(fileMovieAvg, false));
            BufferedWriter bw3 = new BufferedWriter(new FileWriter(fileCoefficient, false));

            ArrayList<Double> values,mediaMovelArr,angularCoefficientArr;
            double mediaMovelValues,angularCoefficientValues;

            int kMin = clustersNum[0];
            int kMax = clustersNum[1];
            int truncateSize = clustersNum[2];
            int realSize = clustersNum[3];

            for(int j = kMin; j <= kMax; j++){
//            for(int j = truncateSize; j >= kMin; j--){ //this line was used when TRUNCATE value was different of NUM_CLUSTERS
                    bw.write(j+",");
                    bw2.write(j+",");
                    bw3.write(j+",");
            }

            bw.write("bestK,");
            bw2.write("bestK,");
            bw3.write("bestK,");

            bw.write("time(nanoTime/TIMER_DIVISOR)");
            bw2.write("time(nanoTime/TIMER_DIVISOR)");
            bw3.write("time(nanoTime/TIMER_DIVISOR)");

            bw.newLine();
            bw2.newLine();
            bw3.newLine();

            for(Integer key: valueMetrics.keySet()){
                Map<Integer,Long> time = new HashMap<Integer,Long>();

                int[] bestK = topicBestK.get(key-1);

                values = valueMetrics.get(key).get(methodEvaluationCurrent).getValues();
                mediaMovelArr = new ArrayList<>(allData.get(key).get(methodEvaluationCurrent).get("mediaMovelArr").values());
                angularCoefficientArr = new ArrayList<>(allData.get(key).get(methodEvaluationCurrent).get("angularCoefficientArr").values());


                time = topicExecutionTime.get(key-1);
                int jFinal = 0;
                int arrListSize = mediaMovelArr.size();
                //for(int j = 0; j < values.size(); j++){
                //for(int j = 0; j < arrListSize; j++){
                for(int j = 0; j < truncateSize; j++){
                    bw.write(values.get(j)+",");
                    if(j < arrListSize){
                         mediaMovelValues = mediaMovelArr.get(j);
                         angularCoefficientValues = angularCoefficientArr.get(j);
                         jFinal = j;
                    }
                    else{
                        mediaMovelValues = mediaMovelArr.get(jFinal);
                        angularCoefficientValues = angularCoefficientArr.get(jFinal);
                    }
                    bw2.write(mediaMovelValues+",");
                    bw3.write(angularCoefficientValues+",");
                }

                int newBestK = (truncateSize-arrListSize)+bestK[methodEvaluationCurrent];

                if(newBestK > truncateSize){
                    newBestK = bestK[methodEvaluationCurrent];
                }

                bw.write(newBestK+",");
                bw2.write(newBestK+",");
                bw3.write(newBestK+",");

                if(newBestK > 99){
                    System.out.println("Best K outlier: "+newBestK);
                }
                else{
                    System.out.println("Best K OK: "+newBestK);
                }

                //bw.write(bestK[methodEvaluationCurrent]+",");
                //bw2.write(bestK[methodEvaluationCurrent]+",");
                //bw3.write(bestK[methodEvaluationCurrent]+",");

                bw.write(time.get(methodEvaluationCurrent).toString());
                bw2.write(time.get(methodEvaluationCurrent).toString());
                bw3.write(time.get(methodEvaluationCurrent).toString());

                bw.newLine();
                bw2.newLine();
                bw3.newLine();
            }

            bw.close();
            bw2.close();
            bw3.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
