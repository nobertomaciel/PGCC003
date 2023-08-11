package rerank.fusion.borda;

import java.util.HashMap;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class VoteMeasurer implements IDigitalObjectDistanceMeasurer{
	
	HashMap<String, Double> votes;
	
	public void setVotes(HashMap<String, Double> votes) {
		this.votes = votes;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		return this.votes.get(digitalObject.getId());
	}
}
