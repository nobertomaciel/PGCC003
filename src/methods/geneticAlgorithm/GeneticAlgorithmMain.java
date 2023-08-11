package methods.geneticAlgorithm;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import config.FeatureManager;
import interfaces.IDiversify;
import interfaces.IFeatureManager;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XReal;
import methods.agglomerative.AverageLink;
import methods.agglomerative.CentroidMethod;
import methods.agglomerative.CompleteLink;
import methods.agglomerative.ILinkage;
import methods.agglomerative.MedianMethod;
import methods.agglomerative.SingleLink;
import methods.agglomerative.WardsMethod;
import methods.clusterCommon.RepresentativeSelector;
import object.DigitalObject;

public class GeneticAlgorithmMain implements IDiversify{

	String configFileName;
	String pathStorageSolutionSet;
	String set;
	String numberCopyBest;
	String populationSize;
	String maxEvaluation;
	String mutationProbability;
	String mutatedValue;
	String crossoverProbability;
	String alpha;

	long seed;
	boolean setSeed;

	int nBestIndividual;//number for copy the best individuals/solutions the population
	private int numberFeatures;
	private ILinkage linkage;
	private int evaluationCriterion;
	private int kClusters;
	private int linkageCriterion;
	private double convergence;
	private int generations;

	private int indexPopulation;

	FeatureManager fm;
	public GeneticAlgorithmMain() {

		this.configFileName = "resources/GAConfigFile.properties";

		Properties configFile = new Properties();
		try {
			configFile.load(new FileInputStream(configFileName));
			pathStorageSolutionSet = configFile.getProperty("SOLUTIONSET_STORAGE");
			set = configFile.getProperty("SET");
			numberFeatures = Integer.parseInt(configFile.getProperty("NUMBER_FEATURES"));
			convergence = Double.parseDouble(configFile.getProperty("CONVERGENCE"));
			numberCopyBest = configFile.getProperty("NUMBERCOPYBESTSOLUTION");
			nBestIndividual = Integer.parseInt(numberCopyBest);
			crossoverProbability = configFile.getProperty("CROSSOVER_PROBABILITY");
			mutationProbability = configFile.getProperty("MUTATION_PROBABILITY");
			mutatedValue = configFile.getProperty("MUTATION_INTERVAL");
			alpha = configFile.getProperty("ALPHA");
			populationSize = configFile.getProperty("POPULATION_SIZE");
			maxEvaluation = configFile.getProperty("MAXEVALUATIONS");
			evaluationCriterion = Integer.parseInt(configFile.getProperty("EVALUATION"));
			kClusters = Integer.parseInt(configFile.getProperty("K_CLUSTERS"));
			linkageCriterion = Integer.parseInt(configFile.getProperty("LINKAGE"));
			this.seed = Long.parseLong(configFile.getProperty("RANDOM_SEED"));
			setSeed = Boolean.parseBoolean(configFile.getProperty("SET_SEED"));
			indexPopulation = Integer.parseInt(configFile.getProperty("INDEX"));
			generations = Integer.parseInt(configFile.getProperty("GENERATIONS"));



			if(setSeed){
				DiversifyGenerator dg = new DiversifyGenerator(this.seed);
				PseudoRandom.setRandomGenerator(dg);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
										ArrayList<DigitalObject> inputList, int idLocal, String locName) {
		ArrayList<DigitalObject> outputList = null;
		Problem   problem   ;         // The problem to solve
		Algorithm algorithm ;         // The algorithm to use
		Operator  crossover ;         // Crossover operator
		Operator  mutation  ;         // Mutation operator
		Operator  selection ;         // Selection operator
		this.fm = (FeatureManager) fm;

		HashMap  parameters ; // Operator parameters
		System.out.println("imputlist: " + inputList.size() + " idLocal: " + idLocal+" locName: " + locName);
		problem = new DiversifyPloblem("ArrayReal", numberFeatures, fm, inputList, idLocal,
				locName, evaluationCriterion, kClusters, linkageCriterion, set);

		algorithm = new GAAlgorithm(dataset, problem, nBestIndividual, convergence, generations) ; // Generational GA

		/* Algorithm parameters*/
		algorithm.setInputParameter("populationSize",Integer.parseInt(populationSize));
		algorithm.setInputParameter("maxEvaluations", Integer.parseInt(maxEvaluation));

		try {// Mutation and Crossover for Real codification
			// Crossover
			parameters = new HashMap() ;
			parameters.put("probability", Double.parseDouble(crossoverProbability)) ;
			double alphaValue = Double.parseDouble(alpha);
			parameters.put("alpha", alphaValue);
			crossover = CrossoverFactory.getCrossoverOperator("BLXAlphaCrossover", parameters);

			//Mutation
			parameters = new HashMap() ;
			parameters.put("probability", Double.parseDouble(mutationProbability)) ;
			parameters.put("mutatedValue", Double.parseDouble(mutatedValue));
			mutation = new MutationCreep(parameters);
			//MutationFactory.getMutationOperator("MutationCreep", parameters);


			/* Selection Operator */
			parameters = null ;
			selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;

			/* Add the operators to the algorithm*/
			algorithm.addOperator("crossover",crossover);
			algorithm.addOperator("mutation",mutation);
			algorithm.addOperator("selection",selection);

			/* Execute the Algorithm */
			long initTime = System.currentTimeMillis();
			SolutionSet population = algorithm.execute();
			long estimatedTime = System.currentTimeMillis() - initTime;
			System.out.println("Total execution time: " + estimatedTime);

			/* Log messages */
			//System.out.println("Objectives values have been writen to file FUN");
			//population.printObjectivesToFile(set + pathStorageSolutionSet  + idLocal+ "FUN.txt");
			//System.out.println("Variables values have been writen to file VAR");
			//population.printVariablesToFile(set +  pathStorageSolutionSet  + idLocal + "VAR.txt");
			ArrayList<ArrayList<Double>> normalizedPopulation = normalizeChromossome(population);
			//writePopulationNormalized(normalizedPopulation, idLocal);
			outputList = bestIndividualRun(population,numberFeatures, fm, inputList, idLocal,
					locName, evaluationCriterion, kClusters, linkageCriterion);

			//writeFitnessMeasures(population.get(indexPopulation).getFitness());
			//System.out.println(normalizedPopulation.get(indexPopulation));
			//writeNormalizedWeights(normalizedPopulation.get(indexPopulation));
		} catch (JMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return outputList;
	}

	public void writeFitnessMeasures(Double measure1){
		try {
			DecimalFormat df = new DecimalFormat("0.####");
			FileOutputStream fos   = new FileOutputStream(set +  pathStorageSolutionSet  + "bestSetFitness.txt", true)     ;
			OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
			BufferedWriter bw      = new BufferedWriter(osw)        ;
			bw.write(df.format(measure1) +"\n");
			bw.close();
		}catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();

		} // printVariablesToFile
	}
	public void writeNormalizedWeights(ArrayList<Double> bestChromossome){
		try {
			FileOutputStream fos   = new FileOutputStream(set +  pathStorageSolutionSet  + "bestSetNormalizedWeights.txt", true)     ;
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw      = new BufferedWriter(osw);

			for(int i = 0; i < bestChromossome.size(); i++){
				//System.out.println(bestChromossome.get(i));
				bw.write(bestChromossome.get(i) + " ");
			}
			bw.write("\n");
			bw.close();
		}catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();

		} // printVariablesToFile
	}

	private ArrayList<ArrayList<Double>> normalizeChromossome(SolutionSet population) {
		XReal chromosome;
		ArrayList<ArrayList<Double>> normalizedPopulation = new ArrayList<ArrayList<Double>>();
		//Get total value the solution
		for(int i = 0; i < population.size(); i++){
			try{
				chromosome = new XReal(population.get(i));
				double totalValue = 0;
				for(int j = 0; j < numberFeatures; j++ ){
					totalValue += chromosome.getValue(j);
				}
				//Normalize old solution
				DecimalFormat df = new DecimalFormat("0.#####");
				ArrayList<Double> solution = new ArrayList<Double>();
				for(int j = 0; j < numberFeatures; j++ ){
					double value = chromosome.getValue(j)/totalValue;
					//double value = Double.parseDouble(format.replace(",","."));
					//System.out.println();
					solution.add(j, value);
					//System.out.println("chromosome: "+ chromosome.getValue(j)+ " normalized: "+solution.get(j) + " total:" + totalValue);
				}
				//Add new solution normalized in population
				normalizedPopulation.add(i,solution);
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return normalizedPopulation;
	}

	public void writePopulationNormalized(ArrayList<ArrayList<Double>> population, int idLocal){
		try {
			FileOutputStream fos   = new FileOutputStream(set +  pathStorageSolutionSet  + idLocal +"_VAR_Normalized.txt")     ;
			OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
			BufferedWriter bw      = new BufferedWriter(osw)        ;

			for(int j = 0; j < population.size(); j++ ){
				// System.out.println(population.get(j));
				bw.write(population.get(j).toString() + "\n");
			}
			bw.close();
		}catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	} // printVariablesToFile

	private ArrayList<DigitalObject> bestIndividualRun(SolutionSet population,   int numberFeatures2, IFeatureManager fm,
													   ArrayList<DigitalObject> inputList, int idLocal, String locName,
													   int evaluationCriterion2, int kClusters2, int linkageCriterion2) {
		XReal chromosome = new XReal(population.get(indexPopulation));
		/*for(int j = 0; j < numberFeatures; j++ ){
			try {
				System.out.println("chromosome: "+ chromosome.getValue(j) + " ");
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		ArrayList<ArrayList<DigitalObject>> clusters = null;
		if(linkageCriterion <= 6){
			clusters = initializeClustering(inputList);
			clusters = runAgglomerativeClustering(clusters, chromosome);
			//int sum = 0;
			/*for (ArrayList<DigitalObject> c: clusters) {
				System.out.print(c.size() + " ");
				sum += c.size();
			}*/
		}else{
			clusters = new ArrayList<ArrayList<DigitalObject>>();
			KMeansMethod km = new KMeansMethod();
			clusters =  km.run(fm, inputList, idLocal, locName, chromosome);


		}
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, inputList, idLocal, locName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);

		return outputList;
	}

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

}