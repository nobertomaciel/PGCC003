package evaluateGT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import jmetal.util.Configuration;
import jmetal.util.JMException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Sumif;

import methods.agglomerative.ILinkage;
import methods.clusterCommon.RepresentativeSelector;
import methods.geneticAlgorithm.AverageLinkEvaluation;
import methods.geneticAlgorithm.CentroidMethodEvaluation;
import methods.geneticAlgorithm.CompleteLinkEvaluation;
import methods.geneticAlgorithm.DaviesBouldinEvaluation;
import methods.geneticAlgorithm.ELinkage;
import methods.geneticAlgorithm.RateDistanceIntraandInterclusterEvaluation;
import methods.geneticAlgorithm.SilhouetteEvaluation;
import methods.geneticAlgorithm.SingleLinkEvaluation;
import methods.geneticAlgorithm.SumSquaredErrorEvaluation;
import config.FeatureManager;
import object.DigitalObject;
import interfaces.IDiversify;
import interfaces.IFeatureManager;

public class EvaluateGT implements IDiversify{

	private ArrayList<DigitalObject> originalList;
	private FeatureManager fm;
	private Properties configFile;
	String pathStorage = "devset/resources/dGT/";
	String pathMeasure = "devset/results/SolutionSetGA/";
	private int numberFeatures;
	private int evaluationCriterion;
	private ELinkage evaluationLinkage;
	public static int count = 1;
	
	@Override
	public ArrayList<DigitalObject> run(String dataset, IFeatureManager fm,
			ArrayList<DigitalObject> inputList, int idLocal, String locName) {
		
		this.fm = FeatureManager.getInstance(idLocal);
		this.originalList = new ArrayList<DigitalObject>(inputList);
		
		System.out.println("original list size: " + originalList.size());
		
		configFile = new Properties();
		
		try {
			configFile.load(new FileInputStream("resources/GAConfigFile.properties"));
			numberFeatures = Integer.parseInt(configFile.getProperty("NUMBER_FEATURES"));
			evaluationCriterion = Integer.parseInt(configFile.getProperty("EVALUATION"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		
		
		ArrayList<ArrayList<DigitalObject>> clusters = readCluster(locName);
		/*switch(evaluationCriterion) {
			case 1: this.evaluationLinkage = new SingleLinkEvaluation(); break;			
			case 2: this.evaluationLinkage = new CompleteLinkEvaluation(); break;
			case 3:	this.evaluationLinkage = new AverageLinkEvaluation(); break;
			case 4:	this.evaluationLinkage = new CentroidMethodEvaluation(); break;
			case 5: this.evaluationLinkage = new DaviesBouldinEvaluation(); break;
			case 6: this.evaluationLinkage = new SilhouetteEvaluation(); break;
			case 7: this.evaluationLinkage = new SumSquaredErrorEvaluation(); break;
		}*/
		SilhouetteEvaluation silh = new SilhouetteEvaluation();
		Double silhouette = silh.runEvaluation(clusters, this.fm);
		System.out.println("FITNESS Silhouette: " + silhouette);
		DaviesBouldinEvaluation db = new DaviesBouldinEvaluation();
		Double daviesBouldin = db.runEvaluation(clusters, this.fm);
		System.out.println("FITNESS DB: " + daviesBouldin);
		SumSquaredErrorEvaluation sse =  new SumSquaredErrorEvaluation();
		Double sumSquaredError = sse.runEvaluation(clusters, this.fm);
		RateDistanceIntraandInterclusterEvaluation rate = new RateDistanceIntraandInterclusterEvaluation();
		Double rateIntra_Inter = rate.runEvaluation(clusters, this.fm);
		writeFitnessMeasures(silhouette, daviesBouldin, sumSquaredError, rateIntra_Inter);
		RepresentativeSelector selector = new RepresentativeSelector(this.fm, inputList, idLocal, locName);
		ArrayList<DigitalObject> outputList = selector.run(clusters);
		
		return outputList;
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
	
	public void writeFitnessMeasures(Double measure1, Double measure2, Double measure3, Double measure4){
		try {
			  DecimalFormat df = new DecimalFormat("0.####");
			  FileOutputStream fos   = new FileOutputStream(pathMeasure +"fitness_measure.txt", true)     ;
		      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
		      BufferedWriter bw      = new BufferedWriter(osw)        ;            
              bw.write(count + ": Silhouette: "+ df.format(measure1) + "  David-Bouldin: " + df.format(measure2) +
            		  "  SSE: " + df.format(measure3)  +  "  R_Intra_Inter: " + df.format(measure4) +"\n");	 
		      bw.close();
		      count++;
		    }catch (IOException e) {
		      Configuration.logger_.severe("Error acceding to the file");
		      e.printStackTrace();
		        
		  } // printVariablesToFile
	}
}
