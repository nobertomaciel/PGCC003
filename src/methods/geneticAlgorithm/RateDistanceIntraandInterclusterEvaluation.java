package methods.geneticAlgorithm;

import java.util.ArrayList;

import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class RateDistanceIntraandInterclusterEvaluation implements ELinkage {

	@Override
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
			FeatureManager fm) {
		double total_rate = 0;
		for(int i = 0; i < clustering.size(); i++) {
			//Se tiver apenas uma imagem no cluster da indeterminação, 0/(por algo)
			if(clustering.get(i).size() > 1){
				//calcula a distância interna
				double intra = this.getDistanceIntra(clustering.get(i), fm);
				for(int j = 0; j < clustering.size(); j++) {
					if( i!= j){	
						double inter = this.getDistanceInter(clustering.get(i), clustering.get(j), fm);
						double rate_IntraInter  = (intra/inter);
						total_rate += rate_IntraInter;
					}
				}
			}
		}
		
		return total_rate/clustering.size();
	}
	/**
	 * Calcula a distância interna do cluster pelo número de pares
	 * @param cluster
	 * @param fm
	 * @return
	 */
	private double getDistanceIntra(ArrayList<DigitalObject> cluster, FeatureManager fm){
		double intra = 0;
		int p1 = 0;
		for(int i = 0; i < cluster.size(); i++) {
			for(int j = 0; j < cluster.size(); j++){
				if(i!=j){
					intra += fm.getAvgDistance(cluster.get(i), cluster.get(j));
					p1++;
				}
			}
		}
		
		return intra/p1; 
	}
	/**
	 * Calcula a distância de todas as imagens de um cluster para todas as outras imagens de outro cluster
	 *  pela quantidade de pares total
	 * @param cluster1
	 * @param cluster2
	 * @param fm
	 * @return
	 */
	private double getDistanceInter(ArrayList<DigitalObject> cluster1,
			ArrayList<DigitalObject> cluster2, FeatureManager fm) {
		double inter = 0;
		int p_total = 0;
		
		for(int i = 0; i < cluster1.size(); i++) {
			for(int j = 0; j < cluster2.size(); j++){
				inter += fm.getAvgDistance(cluster1.get(i), cluster2.get(j));
				p_total++;
			}
		}
		
		return inter/p_total;
	}
	@Override
	public double runGAEvaluation(
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm,
			XReal chromosome) {
		// TODO Auto-generated method stub
		return 0;
	}
}
