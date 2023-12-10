package methods.mmr;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import interfaces.IDiversify;
import interfaces.IFeatureManager;
import object.DigitalObject;

public class MMR implements IDiversify{

	ArrayList<String> listOriginal;
	ArrayList<String> listDiverse;
	String mmrConfigFile = "resources/MMRConfigFile.properties";
	String descriptorConfigFileName = "resources/descriptorConfigFile.properties";
	String topicsMap = "resources/topics.map";
	String topicsSize = "resources/topicsSize.map";
	double diversityFactor;
	int reRankingDepth;
	
	public MMR(){
		listOriginal = new ArrayList<String>();
		listDiverse = new ArrayList<String>();
		
		Properties configFile = new Properties();
		try {
			configFile.load(new FileReader(this.mmrConfigFile));
			this.diversityFactor = Double.parseDouble(configFile.getProperty("DIVERSITYFACTOR"));
			this.reRankingDepth = Integer.parseInt(configFile.getProperty("RE_RANKING_DEPTH"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
/** Método pra diversificação, recebe um array com a lista das imagens e 
 * as caracteristicas dos descritores
 * (non-Javadoc)
 * @see interfaces.IDiversify#run(interfaces.IFeatureManager, java.util.ArrayList)
 */
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		System.out.println("### Running MMR ###");		
		
		if(this.reRankingDepth == 0)
			this.reRankingDepth = inputList.size();
		
		//Array com o valor da relevancia das imagens
		HashMap<String,Double> objectsRelevance = getObjectsRelevanceArray(inputList, idTopic);
		
		//Cria o measurer
		ArrayList<DigitalObject> mmrCandidate = new ArrayList<DigitalObject>();
		ArrayList<DigitalObject> mmrResult = new ArrayList<DigitalObject>();
		MMRMinMinDistanceMeasurer mmrMeasurer = new MMRMinMinDistanceMeasurer(idTopic, objectsRelevance, this.diversityFactor);

		//Seta o measurer em todos objetos do array
		DigitalObject candidateObject;
		int index = 0;
		do{
			candidateObject = inputList.get(index); 
			candidateObject.setMeasurer(mmrMeasurer);
			
			//Adds object to the CAM for it's going to be used in the measurer
			mmrCandidate.add(candidateObject);

			index++;
			
		}while(index < inputList.size() && index < this.reRankingDepth); //Iterates until the given depth or the end of the list
				
		//Start MMR re-rank			
		mmrResult.add(mmrCandidate.remove(0));
		mmrMeasurer.setTarget(null);
		mmrMeasurer.setPartialResult(mmrResult);
				
		while(!mmrCandidate.isEmpty()){
			Collections.sort(mmrCandidate);
			mmrResult.add(mmrCandidate.remove(0));
			mmrMeasurer.setPartialResult(mmrResult);
		}
		
		//Appends the items that were not reranked because they were in too deep rank position
		for (DigitalObject digitalObject : inputList) {
			if(!mmrResult.contains(digitalObject))
				mmrResult.add(digitalObject);
		}
		
		System.out.println("### MMR done! ###");
		
		return mmrResult;
	}

	private HashMap<String,Double> getObjectsRelevanceArray(ArrayList<DigitalObject> inputList, int idTopic) {

		//array com a relevancia das imagens
		HashMap<String,Double> objectsRelevance = new HashMap<String,Double>();
		int sizeImages = inputList.size();
			
		for(int j = 1; j <= sizeImages; j++){						
			Double relevanceImage = calcRelevancebyPosition(j); 
			objectsRelevance.put(inputList.get(j-1).getId(), relevanceImage);
		}
		
		return objectsRelevance;
	
	}
	
	public double calcRelevancebyPosition(int rank){
		double factor = Math.sqrt(Math.sqrt(rank));
		double relevance = 1-(1.0/factor);
		
		return relevance;
	}	
}