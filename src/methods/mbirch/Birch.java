package methods.mbirch;

import interfaces.IDiversify;
import interfaces.IFeatureManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import methods.mbirch.cftree.CFTree;
import methods.clusterCommon.RepresentativeSelector;
import methods.ensemble.generation.ClusteringFileWriter;
import object.DigitalObject;
import config.FeatureManager;

public class Birch implements IDiversify {
	
	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private int maxNodeEntries;
	private double distThreshold;
	private boolean applyMergingRefinement;
	private Properties configFile;
	
	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idTopic, String topicName) {
		
		this.fm = FeatureManager.getInstance(idTopic);
		this.originalList = new ArrayList<DigitalObject>(inputList);
		
		System.out.println("Executing BIRCH for " + idTopic);
		
		configFile = new Properties();
		try {
			configFile.load(new FileInputStream("resources/birch.properties"));
			maxNodeEntries = Integer.parseInt(configFile.getProperty("MAX_NODE_ENTRIES"));
			distThreshold = Double.parseDouble(configFile.getProperty("DIST_THRESHOLD"));
			applyMergingRefinement = Boolean.parseBoolean(configFile.getProperty("APPLY_REFINEMENT"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		CFTree birchTree = new CFTree(maxNodeEntries, distThreshold, this.fm, applyMergingRefinement);
		birchTree.setMemoryLimit(100 * 1024 * 1024);
		
		ArrayList<ArrayList<DigitalObject>> clusters = runClustering(birchTree, inputList);
		
		ClusteringFileWriter.writeClusters(clusters, this.getClass().getSimpleName().toLowerCase(), topicName);
		
		System.out.print("Clust sizes: ");
		for(ArrayList<DigitalObject> c : clusters) {
			System.out.print(c.size() + " ");
		}
		
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, this.originalList, idTopic, topicName);
		ArrayList<DigitalObject> output = selector.run(clusters);
		
		return output;
	}
	
	private ArrayList<ArrayList<DigitalObject>> runClustering(CFTree tree, ArrayList<DigitalObject> inputList) {
		while(inputList.size() > 0)
			tree.insertEntry(inputList.remove(0));
		
		tree.finishedInsertingData();
		
		return tree.getSubclusterMembers();
	}

}
