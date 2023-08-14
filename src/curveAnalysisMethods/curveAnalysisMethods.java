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

    public int run(NavigableMap<Integer,Double> mapAuxiliar, int iMethod){
        int k = 0;
        int c = 0;
        int interval;

        double ya,yi,yf,yValue,ma,mi,mf,vFunc;
        double[] v = new double[2];
        double[] ca = new double[3];

        v[0] = 0.0;

        int ki = mapAuxiliar.lastKey(); // é o k inicial que, no hierárquico, é o maior k
        int kf = mapAuxiliar.firstKey(); // é o k final que, no hierárquico, é o menor k
        int ka = ki;

        double mediaMovel = 0.0;
        NavigableMap<Integer,Double> mediaMovelArr = new TreeMap<>();

        NavigableMap<Integer,Double> angularCoefficientArr = new TreeMap<>();

        if(!Double.isInfinite(mapAuxiliar.get(ki))) {yi = mapAuxiliar.get(ki);}
        else {yi = Double.MAX_VALUE;}

        if(!Double.isInfinite(mapAuxiliar.get(kf))) {yf = mapAuxiliar.get(kf);}
        else {yf = Double.MAX_VALUE;}

        readFile();

        if(firstMethodEvaluation == iMethod){
            System.out.println("CURVE_ANALYSIS_METHOD: "+this.curveAnaysisMethod);
            System.out.println("CURVE_ANALYSIS_METHOD_THRESHOLD: "+this.limiar);
        }

        interval =  this.movingAverageInterval;

        // calcula a média móvel de y para cada k em armazena em array
        for(int i = ki;i>=kf;i--){
            yValue = (mapAuxiliar.get(i) == null || mapAuxiliar.get(i) == Double.NaN ? 0 : mapAuxiliar.get(i));
            mediaMovel += yValue;
            if(c == 0){
                yi = mediaMovel;
            }
            else{
                if(c<=interval-1){
                    mediaMovel /= c;
                }
                else{
                    mediaMovel /=interval;
                    c = 0;
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
            // Elbow analysis
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

                    boolean test = (yf < ((yi) * (1 + limiar)) ? true : false);
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
                        continue;
                    }

                    double  coef = (ma-mi)/(ka-ki); // coeficiente do elemento atual, onde: yf = y atual
                    ca[c] = coef;

                    angularCoefficientArr.put(i, coef);

                    if(c == 2){
                        c = -1;
                    }

                    //DB=0, DTRS=1, Dunn=2, Silhouette=3, SSE=4, XB=5
                    boolean test = false;
                    double caTest = ca[1]*limiar;
                    if(iMethod==0 || iMethod==3 || iMethod==4){ //métodos cujo coeficiente angular será selecionado pelo mínimo
                        //DB=0, Silhouette=3, SSE=4
                        if(ca[0] > ca[1] && ca[1] < ca[2]){ // quando o ponto de inflexão é negativo (para baixo)
                            test = (ca[0] > caTest && caTest < ca[2] ? true : false);
                        }
                    }
                    else{
                        //DTRS=1, Dunn=2, XB=5
                        if(ca[0] < ca[1] && ca[1] > ca[2]) { // quando o ponto de inflexão é positivo (para cima)
                            test = (ca[0] < caTest && caTest > ca[2] ? true : false);
                        }
                    }

                    k = ka + 1;

                    if(test){
                        break;
                    }
                }
            }
            // ----------------------------------------------------------------------------------
            // Derivative analysis
            else if (this.curveAnaysisMethod == 4) { //INCOMPLETO
                // Derivative_analysis_method
            }
            // ----------------------------------------------------------------------------------
            // Default
            else{
                for(Double key: valuesIndex.keySet()){
                    mapAuxiliar.put(valuesIndex.get(key), key);
                }
                return valuesIndex.get(valuesIndex.lastKey());
            }
            c += 1;
        }
        // acrescentar controle de validação erro de index
        if(k == 0) {
            k = ka;
        }

        return k;
    }
}
