package methods.test;

import interfaces.IDiversify;
import interfaces.IFeatureManager;

import java.util.ArrayList;

import object.DigitalObject;
import rerank.common.RankedListStorage;

public class NoDiv implements IDiversify{		

	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm, ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		System.out.println("Running NoDiv");
		System.out.println("List size " + inputList.size() + "\n");
		
		//SAVE RANKED LIST - FOR FUSION ANALYSIS (GP-based Javier's method)
		//saveRankedList(idTopic, topicName, inputList);
				
		return inputList;
	}
	
	private void saveRankedList(int topicID, String topicName, ArrayList<DigitalObject> sortedList) {
		
		ArrayList<Double> scores = new ArrayList<Double>();
		int max = sortedList.size()-1;
		
		for (double i = 0; i < sortedList.size(); i++) {
			scores.add(i/max); //minmax-norm
		}
		
		String outputDir = "/home/rtripodi/pesquisa/MediaEval/MediaEvalDiverseTask2017/rankedLists";
		RankedListStorage storage = new RankedListStorage(topicID, sortedList, topicName, "BM25", scores, outputDir);
		storage.run();
	}
}