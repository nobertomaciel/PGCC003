// métodos de avaliação que indicam o best k por índices altos:
// Dunn Index
// Silhouette
// métodos de avaliação que indicam o best k por índices baixos:
// Davies Bouldin
// Xie Beni
// DTRS
// SSE (melhor relação entre intra e inter cluster)

package curveAnalysisMethods;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class curveAnalysisMethods {
    int curveAnaysisMethod;
    int movingAverageInterval;
    float limiar;
    int lastMethodEvaluation;
    int firstMethodEvaluation;

    String configFileName;
    NavigableMap<Double, Integer> valuesIndex = new TreeMap<>();
    public void readFile(){
        Properties configFile = new Properties();
        this.configFileName = "resources/runnerConfigFile.properties";
        try {
            configFile.load(Files.newInputStream(Paths.get(configFileName)));
            this.curveAnaysisMethod = Integer.parseInt(configFile.getProperty("CURVE_ANALYSIS_METHOD"));
            this.movingAverageInterval = Integer.parseInt(configFile.getProperty("MOVING_AVERAGE_INTERVAL"));
            this.limiar = Float.parseFloat(configFile.getProperty("CURVE_ANALYSIS_METHOD_THRESHOLD"));
            this.firstMethodEvaluation = Integer.parseInt(configFile.getProperty("FIRST_METHOD_EVALUATION"));
            this.lastMethodEvaluation = Integer.parseInt(configFile.getProperty("LAST_METHOD_EVALUATION"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //public int run(NavigableMap<Integer,Double> mapAuxiliar, int iMethod){
    //public NavigableMap<String,TreeMap<Integer,Double>> run(NavigableMap<Integer,Double> mapAuxiliar, int iMethod){
    public NavigableMap<String,TreeMap<Integer,Double>> run(NavigableMap<Integer,Double> mapAuxiliar, int iMethod){
        int k = 0;
        int c = 0;
        boolean pass = false;
        int interval;

        double ya,yi,yf,yValue,ma,mi,mf,vFunc;
        double[] v = new double[2];
        double[] ca = new double[3];

        v[0] = 0.0;

        int ki = mapAuxiliar.lastKey(); // é o k inicial que, no hierárquico, é o maior k
        int kf = mapAuxiliar.firstKey(); // é o k final que, no hierárquico, é o menor k
        int ka = ki;

        double mediaMovel = 0.0;
        TreeMap<Integer,Double> mediaMovelArr = new TreeMap<>();

        TreeMap<Integer,Double> angularCoefficientArr = new TreeMap<>();

        NavigableMap<String,TreeMap<Integer,Double>> returnArr = new TreeMap<>();

        if(!Double.isInfinite(mapAuxiliar.get(ki))) {yi = mapAuxiliar.get(ki);}
        else {yi = Double.MAX_VALUE;}

        if(!Double.isInfinite(mapAuxiliar.get(kf))) {yf = mapAuxiliar.get(kf);}
        else {yf = Double.MAX_VALUE;}

        readFile();

//        if(firstMethodEvaluation == iMethod){
//            System.out.println("CURVE_ANALYSIS_METHOD: "+this.curveAnaysisMethod);
//            System.out.println("CURVE_ANALYSIS_METHOD_THRESHOLD: "+this.limiar);
//        }

        interval =  this.movingAverageInterval;

        // calcula a média móvel de y para cada k em armazena em array
        c = 1;
        for(int i = ki;i>=kf;i--){
            yValue = (mapAuxiliar.get(i) == null || mapAuxiliar.get(i) == Double.NaN ? 0 : mapAuxiliar.get(i));
            mediaMovel += yValue;
            if(c == 1){
                yi = mediaMovel;
            }
            else{
                if(c<=interval){
                    mediaMovel /= c;
                }
                else{
                    mediaMovel /=interval;
                }
            }
            mediaMovelArr.put(i, mediaMovel);
            c += 1;
        }

        // obtem o best k
        c = 0;
        for(int i = ki;i>=kf;i--){
            ka = i;
            if(mapAuxiliar.get(i) == null || Double.isInfinite(mapAuxiliar.get(i))) {
                ya = Double.MAX_VALUE;
            }
            else if(mapAuxiliar.get(i) == Double.NaN){
                ya = 0;
            }
            else {
                ya = mapAuxiliar.get(i);
            }

            // ----------------------------------------------------------------------------------
            // Elbow analysisyi
            if(this.curveAnaysisMethod == 1) {
                double  h = Math.sqrt(Math.pow((ya - yi), 2) + Math.pow((ka - ki), 2));
                double  H = Math.sqrt(Math.pow((kf - ki), 2) + Math.pow((yf - yi), 2));
                // esses IFs podem estar interferindo nos resultados, estudar alternativa
                if(Double.isInfinite(h)) {h = Double.MAX_VALUE;}
                if(Double.isInfinite(H)) {H = Double.MAX_VALUE;}
                // esses IFs podem estar interferindo nos resultados, estudar alternativa
                v[1] = (h*((kf-ki)/H))-(ka-ki);
                if(v[0]<v[1]){
                    k = ka;
                    v[0] = v[1];
                }
            }
            // ----------------------------------------------------------------------------------
            // Monotonicity analysis
            else if (this.curveAnaysisMethod == 2) {
                double test = v[0]*(1+limiar);
                if(ya < test){
                    break;
                }
                else{
                    v[0] = ya;
                    k = ka-1;
                }
            }
            // ----------------------------------------------------------------------------------
            // Monotonicity analysis with moving average
            else if (this.curveAnaysisMethod == 3 || this.curveAnaysisMethod == 5 || this.curveAnaysisMethod == 7) {
                mi = mediaMovelArr.get(ki);
                ma = mediaMovelArr.get(i);
                if(this.curveAnaysisMethod == 3) { // moving average only
                    // o limiar produz uma distorção quando aplicado e testado diretamente
                    // deve-se testar, inicialmente, a variação da curva de forma natural
                    // em seguida, se passou no teste de variação, testar se essa variação é suficientemente grande

                    boolean test = (yf < ((yi) * (1 + limiar)));
                    if (test) {
                        break;
                    } else {
                        k = ka + 1;
                    }
                }
                else if(this.curveAnaysisMethod == 5){
                    // using Euclidian distance INCOMPLETO
                }
                else{ // using angular coefficient of the tangent line
                    if(ka-ki == 0){
                        angularCoefficientArr.put(i, 0.0);
                        continue;
                    }

                    //double  coef = (ma-mi)/(ka-ki); // coeficiente do elemento atual, onde: yf = y atual
                    double  coef = (ma-mi)/(ki-ka); // coeficiente do elemento atual, onde: yf = y atual
                    ca[c] = coef;

                    angularCoefficientArr.put(i, coef);

//                    if(c == 2){
//                        c = -1;
//                    }

//                    System.out.println("........................................................................");
//                    System.out.println("(ma-mi)/(ki-ka).: "+"("+ma+"-"+mi+")/("+ki+"-"+ka+")");
//                    System.out.println("coef............: "+coef);

                    if(c >= 2){
                            //DB=0, DTRS=1, Dunn=2, Silhouette=3, SSE=4, XB=5
                            boolean test = false;
                            double caTest = 0;
                            double avgPoint = (ca[0]+ca[2])/2;
                            double distInflection = 0;

                            //System.out.println("ca..............: [ "+ca[0]+" "+ca[1]+" "+ca[2]+" ]");
                            //System.out.println("avgPoint........: "+avgPoint+" -> "+((ca[0]+ca[2])/2));

        //                    Best k from minimal values (use negative inflection):
        //                        DTRS,SSE, Xie Beni, Davies Bouldin
        //                    Best k from maximum values (use positive inflection):
        //                        Silhouette,Dunn

                            //métodos cujo best k será selecionado pelo valor mínimo da MovingAVG
                            if(iMethod==2 || iMethod==3){
//                            if(iMethod==3){
                                // Dunn=2, Silhouette=3
                                // quando o ponto de inflexão é positivo (para cima)
                                if(ca[0] < ca[1] && ca[1] > ca[2]) {
                                    distInflection = ca[1]-avgPoint;
                                    caTest = avgPoint+(distInflection * limiar);
                                    // testa se é suficientemente pequeno
                                    // (se 10% do coeficiente atual ainda é maior que o anterior e o posterior)
                                    test = (ca[0] < caTest && caTest > ca[2]);
                                }
                            }
                            else{
                                // o Dunn Index, por conta da sua natureza de maximização para um best K, deveria
                                // estar no if, juntamente com o Silhouette. Porém, devido o comportamento geral
                                // da sua curva, côncava, foi necessário fazer a análise pela inflexão negativa
                                // DB=0, DTRS=1, SSE=4, XB=5
                                // quando o ponto de inflexão é negativo (para baixo)
                                if(ca[0] > ca[1] && ca[1] < ca[2]) {
                                    distInflection = avgPoint-ca[1];
                                    caTest = avgPoint-(distInflection * limiar);
                                    // testa se é suficientemente grande
                                    // (se 10% do coeficiente atual ainda é menor que o anterior e o posterior)
                                    test = (ca[0] > caTest && caTest < ca[2]);
                                }
                            }

                        //System.out.println("distInflection..: "+distInflection+" -> "+(avgPoint-ca[1]));
                        //System.out.println("........................................................................");

                        // não é necessário break, pois, é necessário que todo o processo seja executado para preencher os arrays de relatório
                        if(test && !pass){
                            k = ka;
                            pass = true;
                            //System.out.println("........................................................................");
                            //System.out.println("                              BREAKED");
                            //System.out.println("caTest..........: "+caTest);
                            //System.out.println("........................................................................");
                        }

                        ca[0] = ca[1];
                        ca[1] = ca[2];
                        c = 1;
                    }
                }
            }
            // ----------------------------------------------------------------------------------
            // Derivative analysis
            else if (this.curveAnaysisMethod == 4) { //INCOMPLETO
                // Derivative_analysis_method
                //System.out.println("Derivative_analysis_method");
            }
            // ----------------------------------------------------------------------------------
            // Default
            else{
                for(Double key: valuesIndex.keySet()){
                    mapAuxiliar.put(valuesIndex.get(key), key);
                }
                // implementar aqui o returnArr no lugar de return valuesIndex.get(valuesIndex.lastKey());
                //return valuesIndex.get(valuesIndex.lastKey());
            }
            c += 1;
        }
        // acrescentar controle de validação erro de index
        if(k == 0) {
            k = ka+1;
        }

        returnArr.put("k", new TreeMap<Integer,Double>());
        returnArr.put("mediaMovelArr", new TreeMap<Integer,Double>());
        returnArr.put("angularCoefficientArr", new TreeMap<Integer,Double>());

        double kDouble = k;
        returnArr.get("k").put(1, kDouble);
        returnArr.put("mediaMovelArr", mediaMovelArr);
        returnArr.put("angularCoefficientArr", angularCoefficientArr);

        //System.out.println("\n................................................................................................................");
        //System.out.println("moving average data:");
//        for(int i=1;i<=returnArr.get("mediaMovelArr").size();i++) {
//            System.out.print("("+i+","+returnArr.get("mediaMovelArr").get(i)+"),");
//        }
//        System.out.println("\nangular coefficient data:");
//        for(int i=1;i<=returnArr.get("angularCoefficientArr").size();i++) {
//            System.out.print("("+i+","+returnArr.get("angularCoefficientArr").get(i)+"),");
//        }
//        System.out.println("\nangular coefficient data:");
//        for(int i=1;i<=returnArr.get("angularCoefficientArr").size();i++) {
//            System.out.print(returnArr.get("angularCoefficientArr").get(i)+",");
//        }
//        System.out.println("\nevaluation data:");
//        for(int i=1;i<=mapAuxiliar.size();i++) {
//            System.out.print("("+i+","+mapAuxiliar.get(i)+"),");
//        }
//        System.out.println("\n................................................................................................................");
        return returnArr;
    }
}
