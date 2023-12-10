package methods.msd;

import interfaces.IDiversify;
import interfaces.IFeatureManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import object.DigitalObject;
import config.FeatureManager;

public class MSD implements IDiversify {
	
	String descriptorConfigFileName = "resources/descriptorConfigFile.properties";
	String topicsMap = "resources/topics.map";
	String topicsSize = "resources/topicsSize.map";
	String msdConfigFile = "resources/MSDConfigFile.properties";
	FeatureManager fm; 
	double diversityFactor;
	ArrayList<String> listOriginal;
	ArrayList<String> listDiverse;	
	
	public MSD(){
		listOriginal = new ArrayList<String>();
		listDiverse = new ArrayList<String>();
		try {
			Properties configFileMSD = new Properties();
			configFileMSD.load(new FileInputStream(msdConfigFile));
			String valueLambda = configFileMSD.getProperty("DIVERSITYFACTOR");
			this.diversityFactor = Double.parseDouble(valueLambda);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		
		this.fm = FeatureManager.getInstance(idTopic);
		
		System.out.println("### Running MSD ###");		
		
		HashMap<String,Double> objectsRelevance = getObjectsRelevanceArray(inputList, idTopic);
		
		ArrayList<DigitalObject> msdCandidate = new ArrayList<DigitalObject>();
		ArrayList<DigitalObject> msdResult = new ArrayList<DigitalObject>();
		
		DigitalObject candidateObject, candidateObject2;
		int index = 0;
		do{
			candidateObject = inputList.get(index); 
			msdCandidate.add(candidateObject);
			index++;
			
		}while(index < inputList.size());
		
		while(!msdCandidate.isEmpty()){
			int i = 0, j = 1, posCandidate1 = 0, posCandidate2 = 0;
			
			Double scoreMax = (double) 0;			
			double relDist = 0;
			double divDist = 0; 
			double score = 0; 		
	
			for (i = 0; i < msdCandidate.size(); i++){
				candidateObject = msdCandidate.get(i);
				
				for (j = i+1; j < msdCandidate.size(); j++){
					candidateObject2 = msdCandidate.get(j);
					
					relDist = objectsRelevance.get(candidateObject.getId()) + objectsRelevance.get(candidateObject2.getId()) ;
					divDist = this.fm.getAvgDistance(candidateObject, candidateObject2);
					score = (((1-diversityFactor)*relDist)/2) + 2 * diversityFactor*divDist;
					
					if(score > scoreMax){
						scoreMax = score;
						posCandidate1 = i;
						posCandidate2 = j;
					}					
				}		
			}			
			
			if (posCandidate2 > posCandidate1){
				msdResult.add(msdCandidate.get(posCandidate1));
				msdResult.add(msdCandidate.get(posCandidate2));
				msdCandidate.remove(posCandidate1);
				msdCandidate.remove(posCandidate2-1);
			}			
			else if (posCandidate1 > posCandidate2){
				msdResult.add(msdCandidate.get(posCandidate1));
				msdResult.add(msdCandidate.get(posCandidate2));
				msdCandidate.remove(posCandidate1);
				msdCandidate.remove(posCandidate2);
			}
						
			if (msdCandidate.size() == 1)
				msdResult.add(msdCandidate.remove(0));
		}		
		
		return msdResult;
	}
	
	private HashMap<String,Double> getObjectsRelevanceArray(ArrayList<DigitalObject> inputList, int idTopic) {
		
		HashMap<String,Double> objectsRelevance = new HashMap<String,Double>();
		
		int sizeImages = inputList.size();
			
		for(int j = 1; j <= sizeImages; j++)
		{						
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