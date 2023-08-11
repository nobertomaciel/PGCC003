package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class DaviesBouldinEvaluation implements ELinkage{

	@Override
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
								FeatureManager fm) {
		double maximum;
		double db = 0;
		for(int i = 0; i < clustering.size(); i++) {
			maximum = -Double.MIN_VALUE;
			for(int j = 0; j < clustering.size(); j++) {
				if( i!= j){
					double distance = this.getDistance(clustering.get(i), clustering.get(j), fm);
					//System.out.println("distance: " + distance + " minimum: " + minimum);
					if(distance > maximum) {
						maximum = distance;
					}
				}
			}
			db += maximum;
			//System.out.println("db: "+db + "minimum: " + minimum);
		}
		return db/clustering.size();
	}

	private double getDistance(ArrayList<DigitalObject> cluster1,
							   ArrayList<DigitalObject> cluster2, FeatureManager fm) {

		double indexDB = 0.0;
		if(cluster1.size() > 1 && cluster2.size() > 1){

			double si = 0.0;
			for(int j = 0; j < cluster1.size(); j++) {
				si += Math.pow(fm.getAvgDistance(cluster1.get(0), cluster1.get(j)),2);
				//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
			}
			si /= cluster1.size();
			si = Math.sqrt(si);


			double sj = 0.0;
			for(int j = 0; j < cluster2.size(); j++) {
				sj += Math.pow(fm.getAvgDistance(cluster2.get(0), cluster2.get(j)),2); //Calcula o desvio padrao
				//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
			}
			sj /= cluster2.size();
			sj = Math.sqrt(sj);
			double distanceCentroids;
			if(!Double.isNaN(si) && !Double.isNaN(sj)){
				//Calcula a distância entre os centroids
				distanceCentroids = fm.getAvgDistance(cluster1.get(0), cluster2.get(0));

				return Math.abs((si + sj)/distanceCentroids);
				//System.out.println("O valor do index DB "+ indexDB +" Para "+ distanceCentroids);
			}else{
				indexDB = 0;

			}


		}
		return indexDB;
	}
	/*
	 * *************************************** GA ****************************************************
	 */
	@Override
	public double runGAEvaluation(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal chromosome) {
		double maximum = -Double.MIN_VALUE;
		double db = 0;
		for(int i = 0; i < clustering.size(); i++) {
			maximum = Double.MIN_VALUE;
			for(int j = 0; j < clustering.size(); j++) {
				if( i!= j){
					double distance = this.getDistance(clustering.get(i), clustering.get(j), fm, chromosome);
					//System.out.println("distance: " + distance + " minimum: " + minimum);
					if(distance > maximum) {
						maximum = distance;
					}
				}
			}
			db += maximum;
			//System.out.println("db: "+db + "minimum: " + minimum);
		}

		return db/clustering.size();
	}

	private double getDistance(ArrayList<DigitalObject> cluster1,
							   ArrayList<DigitalObject> cluster2, FeatureManager fm,
							   XReal chromosome) {
		int indexCentroid1 = 0;
		int indexCentroid2 = 0;
		double minimum = Double.MAX_VALUE;
		double centroid1 = 0.0;
		double centroid2 = 0.0;
		double indexDB = 0.0;

		if(cluster1.size() > 1 && cluster2.size() > 1){//Para clusters não unitários
			double si = 0.0;
			for(int j = 0; j < cluster1.size(); j++){//Calcula a distância de todos os objetos para o centroid
				si += fm.getGADistance(cluster1.get(indexCentroid1), cluster1.get(j), chromosome);
				//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
			}
			si /= cluster1.size();
			si = Math.sqrt(si);

			double sj = 0.0;
			for(int j = 0; j < cluster2.size(); j++){//Calcula a distância de todos os objetos para o centroid
				sj += fm.getGADistance(cluster2.get(0), cluster2.get(j), chromosome);
				//System.out.println("i= " +i+" j= "  +j + " index: " +sumOfDistances1);
			}
			sj /= cluster2.size();
			sj = Math.sqrt(sj);

			double distanceCentroids;
			//Caso os genes sejam todos nulos irá gerar um NAN
			if(!Double.isNaN(sj) && !Double.isNaN(si)){
				//Calcula a distância entre os centroids
				distanceCentroids = fm.getGADistance(cluster1.get(0), cluster2.get(0), chromosome);
			//System.out.println(fm.getAvgDistance(cluster1.get(indexCentroid1), cluster2.get(indexCentroid2)));
			//distanceCentroids = Math.sqrt(distanceCentroids);
				return Math.abs((si + sj)/distanceCentroids);
			}else{
				indexDB = 0;
			}
		}
		//System.out.println("index: "+indexDB + "distance1"+sumOfDistances1 +  "distance2"+sumOfDistances2+ "distance: " + distanceCentroids);
		return indexDB;
	}

}
