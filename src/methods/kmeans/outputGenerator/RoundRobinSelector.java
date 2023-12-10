package methods.kmeans.outputGenerator;

import interfaces.ISelectListFinal;

import java.util.ArrayList;

import object.DigitalObject;
import config.FeatureManager;

/**
 * Método que organiza a lista final obtendo, de cada cluster, um elemento por iteração. 
 * @author Vinícius Santana
 */

public class RoundRobinSelector implements ISelectListFinal {

	FeatureManager fm;

	public ArrayList<DigitalObject> selectElements(ArrayList<ArrayList<DigitalObject>> clusters, ArrayList<DigitalObject> inputList, FeatureManager fm) {
		
		//System.out.println("Selecting items by clusters round robin...");
		this.fm = fm;
		
		ArrayList<DigitalObject> finalList = new ArrayList<DigitalObject>(inputList.size());

		while(clusters.size() > 0){
			
			//Gets one element from each cluster
			for (int i = 0; i < clusters.size(); i++) {
				finalList.add(clusters.get(i).remove(0));
				
				//If the cluster is empty, removes it from the cluster list
				if(clusters.get(i).size() == 0)
					clusters.remove(i);
			}
		}
		
		return finalList;
	}
}