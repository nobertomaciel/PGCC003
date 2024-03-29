package exec;

import config.FeatureManager;
import filter.blur.BlurFilter;
import interfaces.IDiversify;
import interfaces.IEvaluator;
import interfaces.IFeatureManager;
//import methods.agglomerative.AgglomerativeClustering;
import methods.kmedoids.KMedoids;
import methods.evaluation.daviesBouldin.DaviesBouldin;
import methods.evaluation.dtrs.Dtrs;
import methods.evaluation.dunnIndex.DunnIndex;
import methods.evaluation.silhouette.Silhouette;
import methods.evaluation.sumSquaredError.SumSquaredError;
import methods.evaluation.xbIndex.XBIndex;
import methods.evaluationControl.EvaluationControl;
import methods.exp.wkconect.WeightedKConectRanker;
import methods.mmr.MMR;
import object.DigitalObject;
import rerank.cred.exp.CredRankerExp;
import rerank.fusion.borda.BordaFusionReRanker;
import rerank.text.TextDistReRanker;
import rerank.views.ViewRanker;

import java.io.*;
import java.util.*;

public class RunnerClusterPAM {
    private static Map<String, Long> totalTime = new HashMap<String, Long>();
    //private static Object totalTime;
    String configFileName;
    String inputListDir;
    static String inputList;
    //static int totalDescriptors;
    static int initialDescriptorNumber;
    static int finalDescriptorNumber;
    static boolean runArrayDescriptor;
    int kMax;
    int kMin;
    int timerDivisor;
    int firstMethodEvaluation;
    int lastMethodEvaluation;
    int atualMethodEvaluation;
    static String dataset;
    String methodClassName = null;
    IDiversify method = null;
    int[] bestk;

    private static ArrayList<int[]> topicBestK = new ArrayList<int[]>();
    //private static ArrayList<Map<Integer,Integer>> topicBestK = new ArrayList<Map<Integer,Integer>>();
    int outputListSize;
    String runName;
    String curveAnaysisMethod;
    Properties topicMap;
    Properties topicSize;
    EvaluationControl evaluationControl;
    FeatureManager fm;

    String outputFileName;
    static String descritorName;

    private static ArrayList<Map<Integer,Long>> topicExecutionTime = new ArrayList<Map<Integer,Long>>();

    private int RE_SORT_SELECTION_METHOD;

    //Filters
    private boolean runGeoFilter;
    private boolean runFaceFilter;
    private boolean runBlurFilter;
    private boolean runGTRelFilter;
    private boolean runListTrucating;
    private int trucate_size;

    //Rankers
    private boolean runWKConectRanker;
    private boolean runCredRanker;
    private boolean runViewRanker;
    private boolean runTextRanker;
    private boolean runBordaFusionReRanker;
    private String methodName;

    NavigableMap<Integer,TreeMap<Integer,TreeMap<String,TreeMap<Integer,Double>>>> allData = new TreeMap<>();
    NavigableMap<String,TreeMap<Integer,Double>> returnBestKTree = new TreeMap<>();
    //NavigableMap<String,ArrayList<Double>> returnBestKArr = new TreeMap<>();

    //Filters



    public RunnerClusterPAM(int descriptorNumber){

        System.out.println("public RunnerClusterPAM():");

        Properties descriptorConfigFile = new Properties();

        //int num_descriptors = 0;

        String descriptor = "DESCRIPTOR["+descriptorNumber+"]";
        //String descriptor = "DESCRIPTOR[0]";

        try {            //executionTime[k] = (System.nanoTime() - startTime)/1000000;// pega o tempo para a execução de kMin a kMax para cada um dos tópicos individualmente (um tópico por vez)
            descriptorConfigFile.load(new FileInputStream("resources/descriptorConfigFile.properties"));
            //num_descriptors = Integer.parseInt(descriptorConfigFile.getProperty("NUM_DESCRIPTORS"));
            this.descritorName = descriptorConfigFile.getProperty(descriptor).toUpperCase();
            System.out.println("DESCRIPTOR["+descriptorNumber+"]: "+this.descritorName);

            FeatureManager.setDescriptor(descriptorNumber);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.configFileName = "resources/runnerConfigFile.properties";
        this.outputFileName = dataset + "/results/" + "output_run" + this.descritorName;

        System.out.println("Data set equivele " + dataset);
        File outputFile = new File(this.outputFileName);
        System.out.println("Padrao de nomenclaruta dos arquivos: "+this.outputFileName+"clusterValidityName");
        if (outputFile.exists())
            outputFile.delete();

        readConfiguration();
        initDivMethod();

        this.evaluationControl = new EvaluationControl();

        //System.out.println("Done!\n");

    }


    private void initDivMethod(){
        System.out.println("initDivMethod():");
        System.out.print("Initializing diversification method: ");
        try {
            method = (IDiversify) Class.forName(methodClassName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(methodClassName);
    }

    private void readConfiguration(){
        //Leitura de configuracoes
        System.out.println("readConfiguration():");
        System.out.println("Reading config file...");

        try {
            Properties dataConfigFile = new Properties();
            dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));

            Properties configFile = new Properties();
            configFile.load(new FileInputStream(configFileName));
            this.inputListDir = configFile.getProperty("INPUT_LIST_DIR");
            this.timerDivisor = Integer.parseInt(configFile.getProperty("TIMER_DIVISOR"));
            methodClassName = configFile.getProperty("DIVERSIFICATION_METHOD");

            this.runBlurFilter = Boolean.parseBoolean(configFile.getProperty("RUN_BLUR_FILTER"));
            this.runListTrucating = Boolean.parseBoolean(configFile.getProperty("RUN_LIST_TRUNCATING"));
            this.trucate_size = Integer.parseInt(configFile.getProperty("TRUNCATE_SIZE"));

            //dataConfigFile.load(new FileInputStream("resources/kmedoids.properties"));
            dataConfigFile.load(new FileInputStream("resources/agglomerative.properties"));

            //this.kMax = Integer.parseInt(dataConfigFile.getProperty("NUM_CLUSTERS"));
            //this.kMin = Integer.parseInt((dataConfigFile.getProperty("NUM_MIN_CLUSTERS") == null ? Integer.toString(this.kMax) : dataConfigFile.getProperty("NUM_MIN_CLUSTERS")));
            this.kMax = Integer.parseInt(configFile.getProperty("NUM_CLUSTERS"));
            this.kMin = Integer.parseInt((configFile.getProperty("NUM_MIN_CLUSTERS") == null ? Integer.toString(this.kMax) : configFile.getProperty("NUM_MIN_CLUSTERS")));
            this.firstMethodEvaluation = Integer.parseInt(configFile.getProperty("FIRST_METHOD_EVALUATION"));
            this.lastMethodEvaluation = Integer.parseInt(configFile.getProperty("LAST_METHOD_EVALUATION"));
            this.runName = configFile.getProperty("RUN_NAME");
            this.curveAnaysisMethod = configFile.getProperty("CURVE_ANALYSIS_METHOD");
            this.runGeoFilter = Boolean.parseBoolean(configFile.getProperty("RUN_GEO_FILTER"));
            this.runFaceFilter = Boolean.parseBoolean(configFile.getProperty("RUN_FACE_FILTER"));
            this.runBlurFilter = Boolean.parseBoolean(configFile.getProperty("RUN_BLUR_FILTER"));
            this.runGTRelFilter = Boolean.parseBoolean(configFile.getProperty("RUN_GTREL_FILTER"));
            this.runWKConectRanker = Boolean.parseBoolean(configFile.getProperty("RUN_WKCONECT_RANKER"));
            this.runCredRanker = Boolean.parseBoolean(configFile.getProperty("RUN_CRED_RANKER"));
            this.runViewRanker = Boolean.parseBoolean(configFile.getProperty("RUN_VIEW_RANKER"));
            this.runTextRanker = Boolean.parseBoolean(configFile.getProperty("RUN_TEXT_RANKER"));
            this.runBordaFusionReRanker = Boolean.parseBoolean(configFile.getProperty("RUN_BORDA_FUSION_RANKER"));
            this.runListTrucating = Boolean.parseBoolean(configFile.getProperty("RUN_LIST_TRUNCATING"));
//            this.trucate_size = Integer.parseInt(configFile.getProperty("TRUNCATE_SIZE"));
            this.RE_SORT_SELECTION_METHOD = Integer.parseInt(configFile.getProperty("RE_SORT_SELECTION_METHOD"));
            this.outputListSize = Integer.parseInt(configFile.getProperty("OUTPUT_LIST_SIZE"));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        topicMap = new Properties();
        topicSize = new Properties();

        try {
            topicMap.load(new FileInputStream(dataset + "/resources/topics.map"));
            topicSize.load(new FileInputStream(dataset + "/resources/topicsSize.map"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int[] readTopicsIds(String topicsListFileName){
        System.out.println("readTopicsIds():");

        int[] topicsIDs = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(topicsListFileName));
            int numberOfTopics = Integer.parseInt(br.readLine());

            br.readLine(); // Skips empty line of the file

            topicsIDs = new int[numberOfTopics];

            for(int i = 0; i < numberOfTopics; i++){
                int topicID = Integer.parseInt(br.readLine());
                topicsIDs[i] = topicID;
            }

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return topicsIDs;
    }

    public String separator(String sep, int text){
        int l = sep.length() - text;
        return sep.substring(0, l);
    }

    public int getAtualMethodEvaluation(){
        return this.atualMethodEvaluation;
    }

    public void run(int topicId, ArrayList<IEvaluator> methodEvaluation, int descriptorNumber) {

        System.out.println("run()-------------------------------------------------------------------------------------------");
        System.out.println("public void run(int topicId "+topicId+")");
        System.out.println("##############################################");
        System.out.println("Executing Pipeline for topic " + topicId);
        System.out.println("##############################################");

        //Leitura da lista de entrada
        ArrayList<DigitalObject> inputList = readInputList(topicId);
        ArrayList<DigitalObject> originalList = (ArrayList<DigitalObject>) inputList.clone();
        int inputListOrigSize = inputList.size();
        System.out.println("....Init Topic Size: " + inputListOrigSize);

        //Inicializar o gerenciador de configuracoes e dados
        fm = FeatureManager.getInstance(topicId);
        fm.setDescriptor(descriptorNumber);
        fm.setDataset(dataset);

        //FILTERING ----------------------------------------

        System.out.println(">>> Filtering: start! .................................");
        String topicName = topicMap.getProperty("" + topicId);

        if (this.runBlurFilter)
            inputList = runBlurFilter(inputList, topicName);

        //RERANKING ---------------------------------------------------------
        if (this.runWKConectRanker)
            inputList = (new WeightedKConectRanker()).run(this.dataset, fm, inputList, topicId, topicName);

        if (this.runCredRanker)
            inputList = (new CredRankerExp()).run(this.dataset, inputList, topicId, topicName);

        if (this.runViewRanker)
            inputList = (new ViewRanker()).run(this.dataset, inputList, topicId, topicName);

        if (this.runTextRanker)
            inputList = (new TextDistReRanker()).run(this.dataset, inputList, originalList, topicName, topicId);

        if (this.runBordaFusionReRanker)
            inputList = (new BordaFusionReRanker()).run(fm, inputList, topicId, topicName);

        //DIVERSIFY
        //Relevance-filter: truncate list
        System.out.println(">>> Diversify: start! .................................");
        System.out.println("....Filtered input list size: " + inputList.size());
        ArrayList<DigitalObject> truncatedList = new ArrayList<DigitalObject>();
        if (this.runListTrucating) {
            for (int i = 0; i < Math.min(inputList.size(), this.trucate_size); i++){
                truncatedList.add(inputList.get(i));
            }
            inputList = truncatedList;
            System.out.println("....Trucanted input list size: " + inputList.size());
        }

        //System.out.println(">>> Formas para obter o tempo de execução do esquema............................:");
        //System.out.println(".... a) Pegar o tempo até a execução de cada cluster encontrado no algoritmo");
        methodName = ((KMedoids) method).getClass().toString().split("\\.")[2];
        System.out.println("....Algorithm: "+methodName);

        //if(methodName == "KMEans") kMin=kMax;

        ArrayList<ArrayList<DigitalObject>> cluster;
        ArrayList<ArrayList<ArrayList<DigitalObject>>> clusterAuxiliar = new ArrayList<>();
        NavigableMap<String,TreeMap<Integer,Double>> returnArr = new TreeMap<>();
        long[] executionTime = new long[kMax+1];
        long startTime = System.nanoTime();
        for (int k = kMin; k <= kMax; k++) {
            System.out.println(">>> Clustering start! .................................");
            System.out.println("....k = "+k);
            ((KMedoids) method).setNUM_CLUSTERS(k);
            cluster = ((KMedoids) method).run2(this.dataset, fm, inputList, topicId, topicName);
//            executionTime[k] = 10000; //((KMeans) method).getTimeExecution(k);
            executionTime[k] = ((KMedoids) method).getTimeExecution(k);
            clusterAuxiliar.add(cluster);
            auxiliarError(cluster);
        }

        System.out.println("\n>>> Getting Best K:___________________________________________________");
        bestk = new int[methodEvaluation.size()];
        evaluationControl.setClustering(clusterAuxiliar);
        evaluationControl.runEvaluation(methodEvaluation, fm, topicId);
        allData.put(topicId, new TreeMap<Integer,TreeMap<String,TreeMap<Integer,Double>>>());
        for (int i = firstMethodEvaluation; i <= lastMethodEvaluation; i++) {
            this.atualMethodEvaluation = i;
            returnBestKTree = methodEvaluation.get(i).bestK(i);
            TreeMap<String, TreeMap<Integer, Double>> innerMap = new TreeMap<>();

            innerMap.put("k", returnBestKTree.get("k"));
            innerMap.put("mediaMovelArr", returnBestKTree.get("mediaMovelArr"));
            innerMap.put("angularCoefficientArr", returnBestKTree.get("angularCoefficientArr"));

            allData.get(topicId).put(i, innerMap);

            double kd = returnBestKTree.get("k").get(1);
            bestk[i] = (int)kd;
            String methodName = methodEvaluation.get(i).getClass().getSimpleName().toUpperCase();
            String sep = separator("........................................",(methodName.length() + String.valueOf(bestk[i]).length()));
            System.out.format("....%s %s: %d (Time nanoTime/TIMER_DIVISOR: %d)%n", methodName, sep, bestk[i],executionTime[bestk[i]]);
            methodEvaluation.get(i).reset();
            long t = executionTime[bestk[i]];
            totalTime.computeIfPresent(methodName, (k, v) -> v + t);
        }
        System.out.println("\n>>> __________________________________________________________________");

        System.out.println();
        ArrayList<DigitalObject> outputList;
        Map<Integer,Long> bestkExecutionTime = new HashMap<Integer,Long>();
        for (int i = firstMethodEvaluation; i <= lastMethodEvaluation; i++) {

            System.out.println(">>> Running agglomerative for bestk...: "+bestk[i]);
            ((KMedoids)method).setNUM_CLUSTERS(bestk[i]);
            String evaluationName = methodEvaluation.get(i).getClass().getSimpleName();

            bestkExecutionTime.put(i,executionTime[bestk[i]]);

            outputList = method.run(this.dataset, fm, inputList, topicId, topicName);
            outputList = reSortSelection(fm, outputList, topicId, topicName);
            writeOutputList(topicId, outputList, this.outputFileName + evaluationName);
            System.out.println("....Topico " + topicId + " k = " + bestk[i] + " Metodo = " + methodEvaluation.get(i).getClass().getSimpleName());
        }

        topicExecutionTime.add(bestkExecutionTime);
        topicBestK.add(bestk);

        System.out.println("\n>>> Diversify: done! -----------------------------------------------------------------------");
        System.out.println();
        //Resort diverse list

        //OUTPUT
    }

    public int getBestk(int i){
        int[] bestK = bestk;
        return bestK[i];
    }

    private ArrayList<DigitalObject> reSortSelection (IFeatureManager fm, ArrayList<DigitalObject> outputList, int idTopic, String topicName){

        ArrayList<DigitalObject> selection = null;

        System.out.println(">>> reSortSelection:");

        switch (this.RE_SORT_SELECTION_METHOD) {
            case 0: selection = outputList; break;
            case 1:
                selection = new ArrayList<DigitalObject>();
                for (int i = 0; i < outputList.size() && i < this.outputListSize; i++)
                    selection.add(outputList.get(i));

                MMR mmr = new MMR();
                selection = mmr.run(this.dataset, fm, selection, idTopic, topicName);
                break;
            case 2:
                selection = new ArrayList<DigitalObject>();
                for (int i = 0; i < outputList.size() && i < this.outputListSize; i++)
                    selection.add(outputList.get(i));

                KMedoids pam = new KMedoids();
                selection = pam.run(this.dataset, fm, selection, idTopic, topicName);
                break;
            default:
                break;
        }

        return selection;
    }

    private void writeOutputList(int id, ArrayList<DigitalObject> outputList, String outputFileName){

        System.out.println(">>> writeOutputList:");

        int idTopic = id;
        int resultSize = outputList.size();

        try {
            FileWriter fw = new FileWriter(outputFileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            int index = 0;

            for (int i = 0; i < this.outputListSize && i < outputList.size(); i++) {
                DigitalObject dObject = outputList.get(i);
                bw.write(idTopic + " 0 " + dObject.getId() + " " + index + " " + (resultSize - index) + " " + this.runName + "\n");
                index++;
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void print(ArrayList<ArrayList<DigitalObject>> clusters){
        ArrayList<DigitalObject> clustersc;
        for(int i = 0; i < clusters.size(); i++){
            clustersc = clusters.get(i);
            for (int j = 0; j < clustersc.size(); j++){
                System.out.println("Cluster "+ (i + 1) + " "+ clustersc.get(j).getId());
            }
        }

    }

    private void auxiliarError(ArrayList<ArrayList<DigitalObject>> cluster){
        System.out.println("....auxiliarError:");
//        for(int i = 0; i < cluster.size(); i++){
//            // System.out.println("Cluster "+i+" > k = "+cluster.size()+ "------------------------------------------------------->"+cluster.get(i).size());
//        }
    }

    private ArrayList<DigitalObject> runBlurFilter(ArrayList<DigitalObject> inputList, String topicName) {
        System.out.println("runBlurFilter:");
        BlurFilter blurFilter = new BlurFilter(topicName);
        inputList = blurFilter.runFiltering(inputList);

        return inputList;
    }


    private ArrayList<DigitalObject> readInputList(int id) {
        System.out.println(">>> readInputList:");
        String topicName = topicMap.getProperty(""+id);
        int topicLength = Integer.parseInt(topicSize.getProperty(""+id));

        ArrayList<DigitalObject> inputList = new ArrayList<DigitalObject>();
        Properties topicImageList = new Properties();

        try {
            topicImageList.load(new FileInputStream(this.dataset + File.separator + this.inputListDir + File.separator + topicName + ".map"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < topicLength; i++){
            String objectID = topicImageList.getProperty(""+i);

            if(objectID != null) //Allows using sublists without recomputing topicSize
                inputList.add(new DigitalObject(objectID));
        }

        return inputList;   //Lista com todos os objetos digitais.
    }


    public static void main(String[] args) {
        System.out.println("main(String[] args):");
        System.out.println("Initializing...");

        Properties dataConfigFile = new Properties();

        //List<Integer> arrayDescriptors = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15));
        //List<Integer> arrayDescriptors = new ArrayList<Integer>(Arrays.asList());
        //String[] stringDescriptors;

        try {
            dataConfigFile.load(new FileInputStream("resources/descriptorConfigFile.properties"));
            //totalDescriptors = Integer.parseInt(dataConfigFile.getProperty("TOTAL_DESCRIPTORS"));
            runArrayDescriptor = Boolean.parseBoolean(dataConfigFile.getProperty("RUN_ARRAY_DESCRIPTORS"));
            initialDescriptorNumber = Integer.parseInt(dataConfigFile.getProperty("INITIAL_DESCRIPTOR_NUMBER"));
            finalDescriptorNumber = Integer.parseInt(dataConfigFile.getProperty("FINAL_DESCRIPTOR_NUMBER"));
            //dataConfigFile.getProperty("ARRAY_DESCRIPTORS").split(",")

            dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
            dataset = dataConfigFile.getProperty("DATASET").toLowerCase();
            inputList = dataConfigFile.getProperty("INPUT").toLowerCase();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String topicsListFileName = dataset + "/input/" + inputList;
        System.out.println(topicsListFileName);

        int[] topicsIDs = readTopicsIds(topicsListFileName);

//        if(runArrayDescriptor){
//            // run array here
//            int initialDescriptorNumberLocal = 11;
//            int finalDescriptorNumberLocal = 11;
//        }
//        else{
//            int initialDescriptorNumberLocal = 0;
//            int finalDescriptorNumberLocal = totalDescriptors-1;
//        }

        for (int descriptorNumber = initialDescriptorNumber; descriptorNumber <= finalDescriptorNumber; descriptorNumber++) {
            //int descriptorNumber = 0;

            RunnerClusterPAM runner = new RunnerClusterPAM(descriptorNumber);


            //For each topic runs the diversification process
            ArrayList<IEvaluator> methodsEvaluation = new ArrayList<>();
            methodsEvaluation.add(DaviesBouldin.getInstance());
            methodsEvaluation.add(Dtrs.getInstance());
            methodsEvaluation.add(DunnIndex.getInstance());
            methodsEvaluation.add(Silhouette.getInstance());
            methodsEvaluation.add(SumSquaredError.getInstance());
            methodsEvaluation.add(XBIndex.getInstance());
            //methodsEvaluation.add(GapStatistic.getInstance());

            // Initialize deleting output files and seting up localTime
            for (int i = runner.firstMethodEvaluation; i <= runner.lastMethodEvaluation; i++) {
                String method = methodsEvaluation.get(i).getClass().getSimpleName();
                totalTime.put(method.toUpperCase(), Long.valueOf(0));
                String[] file = {"output_run", "run_eval"};
                for (String name : file) {
                    String fileName = "one/results/" + name + descritorName + method;
                    File outputFile = new File(fileName);
                    if (outputFile.delete()) {
                        System.out.println("Arquivo " + fileName + " deletado!");
                    } else {
                        if (!outputFile.exists())
                            System.out.println("Arquivo " + fileName + " nao foi encontrado!");
                    }
                }
            }

            // Executing Pipeline
//            for (int i = 0; i < 3; i++) {
            for (int i = 0; i < topicsIDs.length; i++) {
                runner.run(topicsIDs[i], methodsEvaluation, descriptorNumber);
            }

            // It starts the evaluations
            for (int i = runner.firstMethodEvaluation; i <= runner.lastMethodEvaluation; i++) {
                System.out.println(methodsEvaluation.get(i).getClass().getSimpleName());
                mensurer(methodsEvaluation.get(i).getClass().getSimpleName());
                System.out.print((";" + totalTime.get(methodsEvaluation.get(i).getClass().getSimpleName().toUpperCase())) + ";" + (totalTime.get(methodsEvaluation.get(i).getClass().getSimpleName().toUpperCase()) / (topicsIDs.length)) + "\n");
            }

            // WriteFile for evaluations
            int[] clustersNum = {runner.kMin, runner.kMax, runner.trucate_size, 0};
            for (int i = runner.firstMethodEvaluation; i <= runner.lastMethodEvaluation; i++) {
                clustersNum[3] = runner.allData.get(1).get(i).get("angularCoefficientArr").size(); // changed because issue #17
                for (int j = 0; j < runner.fm.descriptorNames.size(); j++) {
                    runner.evaluationControl.writeFile(i, runner.fm.descriptorNames.get(j), clustersNum, topicExecutionTime,topicBestK,runner.allData);
                }
            }
        }
        //endfor descriptors
    }




    private static void printEffectiveness2(String method) throws FileNotFoundException, IOException {
        BufferedReader bf = new BufferedReader(new FileReader("one"+ "/results/"+"run_eval"+descritorName+method));
        for (int i = 1; i <= 232; i++) {
            bf.readLine();
        }
        System.out.print(bf.readLine());
        bf.close();
    }

    public static void mensurer(String method){
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "div_eval.jar", "-r", "one/results/output_run"+descritorName+method,  "-rgt", "one/resources/rGT", "-dgt", "one/resources/dGT", "-t", "one/resources/one_topics.xml",  "-o", "one/results", "-f", "run_eval"+descritorName+method);
            Process p = pb.start();
            p.waitFor();

            // Printing effectiveness results
            printEffectiveness2(method);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}