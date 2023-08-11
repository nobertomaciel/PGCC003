package rerank.cred;

import java.util.HashMap;

import measurer.IDigitalObjectDistanceMeasurer;
import object.IDigitalObject;

public class CMeasurer implements IDigitalObjectDistanceMeasurer{
	
	HashMap<String, Double> credScore;
	HashMap<String, Double> rankScore;
	HashMap<String, String> imageUser;
	boolean useRankScore;

	public void setImageUser(HashMap<String, String> imageUser) {
		this.imageUser = imageUser;
	}

	public void setRankScore(HashMap<String, Double> rankScore) {
		this.rankScore = rankScore;
	}

	public void setCredScore(HashMap<String, Double> credScore) {
		this.credScore = credScore;
	}
	
	public void setUseRankScore(boolean useRankScore) {
		this.useRankScore = useRankScore;
	}

	public double getMeasureValue(IDigitalObject digitalObject) {
		
		double cScore = 0.0, rScore = 0.0, finalScore = 0.0;
		
		if(this.imageUser.get(digitalObject.getId()) == null){
			System.out.println("Image user does not exist: " + digitalObject.getId() +" - credibility score set to 0.0");
		}
		else{
			cScore = this.credScore.get(this.imageUser.get(digitalObject.getId()));
			finalScore = cScore;
			
			if(this.useRankScore){
				rScore = this.rankScore.get(digitalObject.getId());
				finalScore *= rScore;	
			}
		}
		
		//System.out.println("cScore:\t" + cScore + "\trScore:\t" + rScore + "\tnew:\t" + finalScore);
		
		return finalScore;
	}
}
