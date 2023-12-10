package methods.evaluation.gapStatistic;

import object.DigitalObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public enum BootStrap {
    INSTANCE;
    private ArrayList<ArrayList<ArrayList<ArrayList<DigitalObject>>>> sampleBootstrap = new ArrayList<>();
    //ArrayList<ArrayList<ArrayList<DigitalObject>>> clusteringBootStraping = new ArrayList<>();

    BootStrap() {
    }

    public static BootStrap getInstance() {
        return INSTANCE;
    }

    public ArrayList<ArrayList<ArrayList<ArrayList<DigitalObject>>>> getClusteringBootStraping() {
        return sampleBootstrap;
    }


    public ArrayList<ArrayList<DigitalObject>> bootStrap(int B, ArrayList<DigitalObject> objects) {
        Random random = new Random(100);
        ArrayList<ArrayList<DigitalObject>> sample = new ArrayList<>();


        for (int i = 0; i < B; i++) {
            ArrayList<DigitalObject> auxiliar = new ArrayList<>();
            for (int j = 0; j < objects.size(); j++) {
                auxiliar.add(objects.get(random.nextInt(objects.size())));
            }
            sample.add(auxiliar);

        }

        return sample;
    }

    public void setClusteringBootStraping(ArrayList<ArrayList<ArrayList<ArrayList<DigitalObject>>>> bootstrap) {
         this.sampleBootstrap = bootstrap;
    }
}