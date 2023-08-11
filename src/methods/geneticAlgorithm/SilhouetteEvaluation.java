package methods.geneticAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;

import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;
import object.DigitalObject;
import config.FeatureManager;

public class SilhouetteEvaluation implements ELinkage{

	@Override
	public double runEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
			FeatureManager fm) {
		double silhouette = 0.0;
		double overallImages = 0.0;
		//System.out.println("\n");
		for(int i = 0; i < clustering.size() - 1; i++) {
			silhouette += silhouetteAVGToCluster(clustering.get(i), clustering, fm, i);
			overallImages += clustering.get(i).size();
		}
		//System.out.println("SILHUETTA: " + silhouette/overallImages);
		return silhouette/overallImages;
	}
	/**
	 * Calcula a silhouette do cluster c, utilizando a silhouetta de cada imagem i do cluster c 
	 * para seu cluster vizinho
	 * @param cluster1
	 * @param clustering
	 * @param fm
	 * @param currentCluster
	 * @return
	 */
	private double silhouetteAVGToCluster(ArrayList<DigitalObject> cluster1,
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm, int currentCluster) {

		double maximumDissimilarityClusters = 0.0;
		double distanceInternal = 0.0;
		double silhouette = 0.0;
		for(int i = 0; i < cluster1.size(); i++) {
			if(cluster1.size() > 1){
				for(int j = 0; j < cluster1.size(); j++) {
					//calcula a distância da imagem i para todas as outras do cluster
					distanceInternal += fm.getAvgDistance(cluster1.get(i), cluster1.get(j));
				}
				//calcula a dissimilaridade média da imagem i
				distanceInternal /= cluster1.size();
				//calcula o valor da dissimilaridade entre a imagem i para todos os outros clusters
				maximumDissimilarityClusters = maximumDissimilarityClusterGA(i,clustering, currentCluster, fm);
				double value = 0;
				if(distanceInternal < maximumDissimilarityClusters){
					//System.out.println("cluster: "+ currentCluster +" image: " + i + " melhor vizinho longe: cluster certo ");
					value = 1 - (distanceInternal/maximumDissimilarityClusters);
					silhouette += value;
					//silhouetteValueList.add(value);
				}else if(distanceInternal == maximumDissimilarityClusters){
					//System.out.println("cluster: "+ currentCluster +" image: " + i + " indefinido");
					silhouette += 0.0;
					//silhouetteValueList.add(value);
				}else{
					//System.out.println("cluster: "+ currentCluster +" image: " + i + " melhor vizinho próximo: cluster errado ");
					value = (maximumDissimilarityClusters/distanceInternal) - 1;
					silhouette += value;
					//silhouetteValueList.add(value);
				}
			}else{

				silhouette += 0.0;
			}
		}
		return silhouette;
	}

	private double maximumDissimilarityClusterGA(int imageI,
												 ArrayList<ArrayList<DigitalObject>> clustering, int currentCluster,
												 FeatureManager fm) {

		double maximum = Double.MIN_VALUE;
		//calcula a maior distância de similaridade da imagem i do cluster atual para todos os outros clusters
		//encontra o cluster vizinho
		for(int j = 0; j < clustering.size(); j++) {
			if(j == currentCluster){
				//não deve ser calculado, já foi calculado na similaridade interna do cluster
			}else{
				double distance = this.getDistance(clustering.get(currentCluster), imageI, clustering.get(j), fm);
				if(distance > maximum){//maior distância da imagem i do cluster A para os demais clusters
					maximum = distance;
				}
			}
		}
		return maximum;
	}

	private double getDistance(ArrayList<DigitalObject> currentCluster, int imageObject,
							   ArrayList<DigitalObject> otherClusters, FeatureManager fm) {

		double distance = 0.0;
		for(int j = 0 ; j < otherClusters.size(); j++) {
			//soma das distâncias da imagem i para todas as outras imagens do cluster B
			distance += fm.getAvgDistance(currentCluster.get(imageObject), otherClusters.get(j));
		}
		//média da distância da imagem i para todas as outras do cluster B
		return distance/otherClusters.size();

	}

	//*****************************************GA*********************************
	public double runGAEvaluation(ArrayList<ArrayList<DigitalObject>> clustering,
			FeatureManager fm, XReal chromosome) {
		double silhouette = 0.0;
		double overallImages = 0.0;
		//System.out.println("\n");
		for(int i = 0; i < clustering.size() - 1; i++) {
			silhouette += silhouetteAVGToClusterGA(clustering.get(i), clustering, fm, i, chromosome);
			overallImages += clustering.get(i).size();
		}
		//System.out.println("SILHUETTA: " + silhouette/overallImages);
		return silhouette/overallImages;
	}
	/**
	 * Calcula a silhouette do cluster c, utilizando a silhouetta de cada imagem i do cluster c 
	 * para seu cluster vizinho
	 * @param cluster1
	 * @param clustering
	 * @param fm
	 * @param currentCluster
	 * @return
	 */
	private double silhouetteAVGToClusterGA(ArrayList<DigitalObject> cluster1,
			ArrayList<ArrayList<DigitalObject>> clustering, FeatureManager fm, int currentCluster,
			XReal chromosome) {
		
		double maximumSimilarityClusters= 0.0;
		double distanceInternal = 0.0;
		double silhouette = 0.0;
		for(int i = 0; i < cluster1.size(); i++) {
			if(cluster1.size() > 1){
				for(int j = 0; j < cluster1.size(); j++) {
					//calcula a distância da imagem i para todas as outras do cluster
					 distanceInternal += fm.getGADistance(cluster1.get(i), cluster1.get(j), chromosome);	
				}
				
				//calcula a dissimilaridade média da imagem i
				if(!Double.isNaN(distanceInternal)){	
					distanceInternal /= cluster1.size();
					//calcula o valor da dissimilaridade entre a imagem i para todos os outros clusters
					maximumSimilarityClusters = maximumDissimilarityCluster(i,clustering, currentCluster, fm, chromosome);
					if(!Double.isNaN(maximumSimilarityClusters)){
						double value = 0;
						if(distanceInternal < maximumSimilarityClusters){
							//System.out.println("cluster: "+ currentCluster +" image: " + i + " melhor vizinho longe: cluster certo ");
							value = 1 - (distanceInternal/maximumSimilarityClusters);
							silhouette += value;
							if(Double.isNaN(maximumSimilarityClusters/distanceInternal)){
								try {
									for(int s = 0; s < chromosome.size(); s++){
										System.out.println(chromosome.getValue(s));
									}
								} catch (JMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								System.out.println("DISTÂNCIA INTERNA: " + distanceInternal);
							}
							//silhouetteValueList.add(value);
						}else if(distanceInternal == maximumSimilarityClusters){
							//System.out.println("cluster: "+ currentCluster +" image: " + i + " indefinido");
							silhouette += 0.0;
							//silhouetteValueList.add(value);
						}else{
							//System.out.println("cluster: "+ currentCluster +" image: " + i + " melhor vizinho próximo: cluster errado ");
							value = (maximumSimilarityClusters/distanceInternal) - 1;
							silhouette += value;
							if(Double.isNaN(maximumSimilarityClusters/distanceInternal)){
								try {
									for(int s = 0; s < chromosome.size(); s++){
										System.out.println(chromosome.getValue(s));
									}
								} catch (JMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								System.out.println("SIMILARITYEXTERNA: " + maximumSimilarityClusters );
							}
							//silhouetteValueList.add(value);
						}
					}
				}else{
					//genes todos com pesos 0's geram distância interna igual a 0, e consequentemente
					// a distância interna é um NAN, logo não será computado
					silhouette = 0.0;
					//System.out.println("Não faz nada");
					break;
				}
				distanceInternal = 0.0;
			}else{
				//System.out.println("cluster: "+ currentCluster +" image: " + i + "indefinido: apenas 1 imagem no cluster");
				//se tiver apenas uma imagem no cluster é definido o valor como 0, 
				//já que não pode definir onde ele deveria estar
				silhouette += 0.0;
				//silhouetteValueList.add(0.0);
			}
			
		}
		return silhouette;
	}

	

	private double maximumDissimilarityCluster(int imageI,
			ArrayList<ArrayList<DigitalObject>> clustering, int currentCluster,
			FeatureManager fm, XReal chromosome) {
		
		double maximum = Double.MIN_VALUE;
		//calcula a maior distância de similaridade da imagem i do cluster atual para todos os outros clusters
		//encontra o cluster vizinho
		for(int j = 0; j < clustering.size(); j++) {
			if(j == currentCluster){
				//não deve ser calculado, já foi calculado na similaridade interna do cluster
			}else{
				double distance = this.getDistanceGA(clustering.get(currentCluster), imageI, clustering.get(j), fm, chromosome);
				if(distance > maximum){//maior distância da imagem i do cluster A para os demais clusters
					maximum = distance;
				}
			}
		}
		
		return maximum;
	}
	private double getDistanceGA(ArrayList<DigitalObject> currentCluster, int imageObject,
			ArrayList<DigitalObject> otherClusters, FeatureManager fm, XReal chromosome) {
		
		double distance = 0.0;
		for(int j = 0 ; j < otherClusters.size(); j++) {
		//soma das distâncias da imagem i para todas as outras imagens do cluster B		
			distance += fm.getGADistance(currentCluster.get(imageObject), otherClusters.get(j), chromosome);
		}
		//média da distância da imagem i para todas as outras do cluster B
		return distance/otherClusters.size();
		
	}

}