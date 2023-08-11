package rerank.fusion.borda;

import interfaces.IFeatureManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import object.DigitalObject;
import rerank.cred.CredRanker;

public class BordaFusionReRanker {

	public ArrayList<DigitalObject> run(IFeatureManager fm, ArrayList<DigitalObject> inputList, int topicId, String topicName) {
		
		ArrayList<ArrayList<DigitalObject>> tempList = new ArrayList<ArrayList<DigitalObject>>();
		
		//tempList.add((new WikiBordaRanker()).run(inputList, topicId, topicName));
		//tempList.add((new CredRanker()).run(inputList, topicId, topicName));
		//tempList.add((new ViewRanker()).run(inputList, topicId, topicName));
		//tempList.add((new WeightedKConectRanker()).run(fm, inputList, topicId));
	
		//Initialize Votes
		HashMap<String, Double> votes = new HashMap<String, Double>();
		for(DigitalObject digitalObject : inputList)
			votes.put(digitalObject.getId(),0.0);
		
		//Conpute votes
		for (ArrayList<DigitalObject> list : tempList) {
			for (int j = 0; j < list.size(); j++) {
				double vote = votes.get(list.get(j).getId());
				vote += getVoteValue(j+1);
				votes.put(list.get(j).getId(), vote);
			}
		}
		
		//Sort by votes
		VoteMeasurer voteMeasurer = new VoteMeasurer();
		voteMeasurer.setVotes(votes);
		for(int i = 0; i < inputList.size(); i++)
			inputList.get(i).setMeasurer(voteMeasurer);
		
		Collections.sort(inputList);
		
		//Reverts after voting for it is sorted in ascending order
		ArrayList<DigitalObject> outputList = new ArrayList<DigitalObject>();
		for(int i = (inputList.size()-1); i >= 0; i--){
			outputList.add(inputList.get(i));
		}
		
		return outputList;
	}
		
	private double getVoteValue(int rank){
		double factor = Math.sqrt(Math.sqrt(rank));
		double vote = 1.0/factor;
		return vote;
	}
}
