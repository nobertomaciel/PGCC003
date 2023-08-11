package clusterEff;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import object.DigitalObject;

public class ClusteringEvaluation {

	public static float resultSumPurity = 0;
	public static float resultSumMax = 0;
	public static float resultSumFMeasure = 0;
	public static int currNumQueries = 0;
	public static Map<Integer, String> mapPurity = new HashMap<Integer, String>();
	public static Map<Integer, String> mapMax = new HashMap<Integer, String>();
	public static Map<Integer, String> mapFMeasure = new HashMap<Integer, String>();
	
	private String topicsGroundTruth = "resources/dGT";
	private String topics = "resources/topics.map";
	private String topicName;
	private String dataset;
	
	public ClusteringEvaluation(int idTopic) {
		
		Properties topicConfigFile = new Properties();
		Properties dataConfigFile = new Properties();
		
		try {
			dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
			this.dataset = dataConfigFile.getProperty("DATASET");
			topicConfigFile.load(new FileInputStream(dataset + File.separator + topics));
			topicName = topicConfigFile.getProperty("" + idTopic);
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void evaluateClustering(ArrayList<ArrayList<DigitalObject>> clusters) {
		HashMap<String, String> dGT = readGT(topicName);
		double resultPurity, resultMax, resultFMeasure;
		resultPurity = resultMax = resultFMeasure = 0;
		
		System.out.println("Running Cluster Evaluation...");
		
		Purity purity = new Purity();
		resultPurity = purity.runEvaluation(clusters, dGT);
		//System.out.println("Clusters Purity: " + String.format( "%.4f", resultPurity) + "\n");
			
		MaximumMatching matching = new MaximumMatching();
		resultMax = matching.runEvaluation(clusters, dGT);
		//System.out.println("Clusters Maximum Matching: " + String.format( "%.4f", resultMax) + "\n");
				
		FMeasure fmeasure = new FMeasure();
		resultFMeasure = fmeasure.runEvaluation(clusters, dGT);
		//System.out.println("Clusters FMeasure: " + String.format( "%.4f", resultFMeasure) + "\n");
		
		ClusteringEvaluation.resultSumPurity += resultPurity;
		ClusteringEvaluation.resultSumMax += resultMax;
		ClusteringEvaluation.resultSumFMeasure += resultFMeasure;
		ClusteringEvaluation.currNumQueries++;
		ClusteringEvaluation.mapPurity.put(currNumQueries, String.format("%.4f", resultPurity));
		ClusteringEvaluation.mapMax.put(currNumQueries, String.format("%.4f", resultMax));
		ClusteringEvaluation.mapFMeasure.put(currNumQueries, String.format("%.4f", resultFMeasure));
		//System.out.println("Current average evaluation value: " + String.format( "%.4f",this.resultSum/this.currNumQueries));
	}
	
	public void generateSheet() {
		Properties dataConfigFile = new Properties();
		String dataset;
		
		try {
			dataConfigFile.load(new FileInputStream("datasetConfigFile.properties"));
			dataset = dataConfigFile.getProperty("DATASET");
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet("matching_measures");
			int i = 1;
			while(i <= currNumQueries) {
				Row row = worksheet.createRow(i);
				Cell firstCell = row.createCell(0);
				Cell purityCell = row.createCell(2);
				Cell maxCell = row.createCell(4);
				Cell fMeasureCell = row.createCell(6);
				firstCell.setCellValue(i);
				purityCell.setCellValue(mapPurity.get(i));
				maxCell.setCellValue(mapMax.get(i));
				fMeasureCell.setCellValue(mapFMeasure.get(i));
				i++;
			}
			
			FileOutputStream output = new FileOutputStream(new File(dataset + File.separator + "resources/evaluation/matching_measures.xls"));
			workbook.write(output);
			output.close();
			workbook.close();
			System.out.println("Intrinsic cluster evaluation spreadsheet written successfully!");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<String, String> readGT(String topicName) {
		
		HashMap<String, String> dGT = new HashMap<String, String>();
		String groundTruthFile = dataset + File.separator + topicsGroundTruth + File.separator + topicName + " dGT.txt";
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(groundTruthFile));
			String strLine;
			
			while((strLine = br.readLine()) != null) {
				String[] arrayLine = strLine.split(",");
				String id = arrayLine[0].trim();
				String cl = arrayLine[1].trim();
				
				dGT.put(id, cl);
			}
			br.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return dGT;
	}
}
