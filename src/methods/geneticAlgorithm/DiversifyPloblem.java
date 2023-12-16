package methods.geneticAlgorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import config.FeatureManager;
import interfaces.IFeatureManager;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;
import methods.agglomerative.AverageLink;
import methods.agglomerative.CentroidMethod;
import methods.agglomerative.CompleteLink;
import methods.agglomerative.ILinkage;
import methods.agglomerative.MedianMethod;
import methods.agglomerative.SingleLink;
import methods.agglomerative.WardsMethod;
import object.DigitalObject;

public class DiversifyPloblem extends Problem {

	private ArrayList<DigitalObject> originalList;
	ArrayList<DigitalObject> inputList;
	private FeatureManager fm;
	private ILinkage linkage;
	private ELinkage evaluationLinkage;
	private int kClusters;
	private int linkageCriterion;
	private int evaluationCriterion;
	int idLocal;
	String locName;
	String dataset;
	String pathStorage = "devset/resources/dGT/"; 
	static Double fitnessValue;
	/*
	 public DiversifyPloblem(String solutionType) throws ClassNotFoundException {
		 this(solutionType, 3);
	
	 } 
	*/
	public DiversifyPloblem(String solutionType, Integer numberOfVariables, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idLocal, String locName, int evaluationCriterion,
			int kClusters, int linkageCriterion, String dataset) {
		fitnessValue = 0.0;
		this.fm = (FeatureManager) fm;
		this.originalList = new ArrayList<DigitalObject>(inputList);
		this.inputList = inputList;
		this.idLocal = idLocal;
		this.locName = locName;
		this.kClusters = kClusters;
		this.linkageCriterion = linkageCriterion;
		this.evaluationCriterion = evaluationCriterion;//for evaluation the fitness
		this.dataset = dataset;
		
		numberOfVariables_   = numberOfVariables;//number of features
	    numberOfObjectives_  = 1;
	    numberOfConstraints_ = 0;
	    problemName_ = "DiversifyPloblem";
	        
	    upperLimit_ = new double[numberOfVariables_] ;
	    lowerLimit_ = new double[numberOfVariables_] ;
	       
	    for (int i = 0; i < numberOfVariables_; i++) {
	      lowerLimit_[i] = 0.0 ;
	      upperLimit_[i] = 1.0  ;
	    } // for
	    
	    if (solutionType.compareTo("Real") == 0)
	    	solutionType_ = new RealSolutionType(this) ;
	    else if (solutionType.compareTo("ArrayReal") == 0)
	    	solutionType_ = new ArrayRealSolutionType(this) ;
	    else {
	    	System.out.println("Error: solution type " + solutionType + " invalid") ;
	    	System.exit(-1) ;
	    }
	}

	 
	/** 
	  * Evaluates/fitness a solution 
	  * @param solution The solution to evaluate
	   * @throws JMException 
	  */
	public void evaluate(Solution solution) throws JMException {
		//readConfiguration();
		double fitness = 0;
		XReal chromosome = new XReal(solution);
		/*for(int j = 0; j < numberOfVariables_; j++ ){
			System.out.println("chromosome: "+ chromosome.getValue(j) + " ");
		}*/
		inputList = (ArrayList<DigitalObject>) originalList.clone();//porque a lista sempre é zerada
		ArrayList<ArrayList<DigitalObject>> clusters = null;
		int sum = 0;
		if(linkageCriterion <= 6){
			clusters = initializeClustering(inputList);
			clusters = runAgglomerativeClustering(clusters, chromosome);
			//int sum = 0;
			/*for (ArrayList<DigitalObject> c: clusters) {
				System.out.print(c.size() + " ");
				sum += c.size();
			}*/
		}else if(linkageCriterion == 7){
			clusters = new ArrayList<ArrayList<DigitalObject>>();
			KMedoidsMethod km = new KMedoidsMethod();
			clusters =  km.run(fm, inputList, idLocal, locName, chromosome);
		}else if(linkageCriterion == 8){
			clusters = readCluster(locName);
		}
		
		/*for (ArrayList<DigitalObject> c: clusters) {
			System.out.print(c.size() + " ");
			sum += c.size();
		}*/
		//System.out.println("\nFinal list size: " + sum);
		//method for valuation of cluster
		switch(evaluationCriterion) {
			case 1: this.evaluationLinkage = new SingleLinkEvaluation(); break;			
			case 2: this.evaluationLinkage = new CompleteLinkEvaluation(); break;
			case 3:	this.evaluationLinkage = new AverageLinkEvaluation(); break;
			case 4:	this.evaluationLinkage = new CentroidMethodEvaluation(); break;
			case 5: this.evaluationLinkage = new DaviesBouldinEvaluation(); break;
			case 6: this.evaluationLinkage = new SilhouetteEvaluation(); break;
		}
		//fitness = this.evaluationLinkage.runEvaluation(clusters, fm);
		fitness = this.evaluationLinkage.runGAEvaluation(clusters, fm, chromosome);
		//System.out.println("FITNESS: " + fitness);
		//Solution Fun, number of objectives
		solution.setObjective(0, fitness);
		solution.setFitness(fitness);
		if(fitness <= fitnessValue ){
			//printChromosomeToFile(chromosome, fitness, idLocal);
			fitnessValue = fitness;
		}
	}
	
	public void printChromosomeToFile(XReal chromosome, Double fitness, int idLocal){
	    try {
	      FileOutputStream fos   = new FileOutputStream(dataset+"/results/SolutionSetGA/" +idLocal +" BestChromosome.txt")     ;
	      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	      BufferedWriter bw      = new BufferedWriter(osw)        ;            

	      
	        
	      for(int j = 0; j < numberOfVariables_; j++ ){
	            bw.write(chromosome.getValue(j) + " ");	            
	        }
	      bw.write(" FITNESS: "+ fitness + " ");	 
	      bw.close();
	    }catch (IOException e) {
	      Configuration.logger_.severe("Error acceding to the file");
	      e.printStackTrace();
	    } catch (JMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	  } // printVariablesToFile
	
	private ArrayList<ArrayList<DigitalObject>> initializeClustering(ArrayList<DigitalObject> inputList) {
		
		ArrayList<ArrayList<DigitalObject>> clustering = new ArrayList<ArrayList<DigitalObject>>();
		
		while(inputList.size() > 0) {
			ArrayList<DigitalObject> cluster = new ArrayList<DigitalObject>();
			cluster.add(inputList.remove(0));
			clustering.add(cluster);
		}
		
		//System.out.println("init num clusters: " + clustering.size());
		
		return clustering;
	}
	
	private ArrayList<ArrayList<DigitalObject>> runAgglomerativeClustering(ArrayList<ArrayList<DigitalObject>> clustering,
			XReal individual ) {
		
		switch(linkageCriterion) {
			case 1: this.linkage = new SingleLink(); break;			
			case 2:	this.linkage = new CompleteLink(); break;			
			case 3:	this.linkage = new AverageLink(); break;
			case 4:	this.linkage = new CentroidMethod(); break;
			case 5:	this.linkage = new MedianMethod(); break;
			case 6:	this.linkage = new WardsMethod(); break;
		}
		
		while(clustering.size() > kClusters) {
			int[] indexes = linkage.findClosestPair(clustering, this.fm, individual);
			mergeClusters(clustering, indexes);			
		}
		
		return clustering;
	}
	
	private void mergeClusters(ArrayList<ArrayList<DigitalObject>> clustering, int[] indexes) {
		
		ArrayList<DigitalObject> newCluster = new ArrayList<DigitalObject>();
				
		//Populate new cluster
		newCluster.addAll(clustering.get(indexes[0]));
		newCluster.addAll(clustering.get(indexes[1]));
		
		//Removes old clusters. The one with the biggest index first for not changing the position of the other
		clustering.remove(Math.max(indexes[0], indexes[1]));
		clustering.remove(Math.min(indexes[0], indexes[1]));
		
		clustering.add(newCluster);
	}


	private ArrayList<ArrayList<DigitalObject>> readCluster(String locName) {
		   ArrayList<ArrayList<DigitalObject>> groups = new  ArrayList<ArrayList<DigitalObject>>();
			try {
				FileReader fileRead = new FileReader(pathStorage + locName+ " dclusterGT.txt");
			    BufferedReader file = new BufferedReader(fileRead);
			    String linha;
				linha = file.readLine();
				while (linha != null) {
				  	String[] object =linha.split(",");
				  	int key = Integer.parseInt(object[0]);
				  	groups.add(new ArrayList<DigitalObject>());
				  	linha = file.readLine(); // lê da segunda até a última linha
			    }
				fileRead.close();

				FileReader fileRead2 = new FileReader(pathStorage + locName+ " dGT.txt");
			    BufferedReader file2 = new BufferedReader(fileRead2);
				linha = file2.readLine();
				 while (linha != null) {
				  	String[] object =linha.split(",");
				  	int key = Integer.parseInt(object[1]);
				  	String objectId = object[0];
				  	DigitalObject digitalObj = new DigitalObject(objectId);
				  	groups.get(key-1).add(digitalObj);
				  	//System.out.println(groups.get(key));
	               linha = file2.readLine(); // lê da segunda até a última linha
			      }
				fileRead2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(groups.get(groups.size() -1).size() == 0){
				//System.out.println(groups.get(groups.size() -1));
				groups.remove(groups.size() -1);
				//System.out.println(groups.get(groups.size() -1));
			}
			return groups;
		}
}
