package config;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import interfaces.IFeatureManager;
import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;
import object.IDigitalObject;

public class FeatureManager implements IFeatureManager{
	String dataset;
	String descriptorConfigFileName = "resources/descriptorConfigFile.properties";

	String datasetConfigFileName = "datasetConfigFile.properties";
	String topicsSizeFileName = "resources/topicsSize.map";
	String topics = "resources/topics.map";
	int initTopicId, lastTopicId;
	HashMap<Integer, String> topicsMap = new HashMap<Integer, String>();

	public int numberOfDesc;
	public static int descriptorNumber;
	public ArrayList<String> getDescriptorNames() {
		return descriptorNames;
	}

	public ArrayList<String > descriptorNames;
	int idTopic;
	String collection_Dir;
	public HashMap<Integer, Integer> numberOfImages = new HashMap<Integer,Integer>();
	static FeatureManager fmInstance;
	ArrayList<double[][]> arrayMatrix;
	HashMap<String, Integer> imageIndex;

	private boolean normalize;
	private long randomSeed;

	public static FeatureManager getInstance(int id){
		if(fmInstance != null){
			if(fmInstance.idTopic != id)
				fmInstance = new FeatureManager(id);
		}
		else
			fmInstance = new FeatureManager(id);

		return fmInstance;
	}

	public static void setDescriptor(int dn) {
		descriptorNumber = dn;
	}
	public int getIdTopic(){
		return this.idTopic;
	}
	//Recebe o id do tópico no map para recuperar as informações desse t�pico
	private FeatureManager(int idTopicMap){
		arrayMatrix = new ArrayList<double[][]>();
		imageIndex = new HashMap<String, Integer>();
		idTopic = idTopicMap;

		Properties configFile = new Properties();

		try {
			// mudança noberto 21/11/2022
//				configFile.load(new FileInputStream(datasetConfigFileName));
//				String numberOfDescriptors  =  configFile.getProperty("TOTAL_DESCRIPTORS");
			// mudança noberto

			configFile.load(new FileInputStream(descriptorConfigFileName));
			String numberOfDescriptors  =  configFile.getProperty("NUM_DESCRIPTORS");


			numberOfDesc = Integer.parseInt(numberOfDescriptors);
			descriptorNames = new ArrayList<String>();
			collection_Dir = configFile.getProperty("DISTBIN_STORAGE");
			dataset = configFile.getProperty("DATASET");
			this.normalize = Boolean.parseBoolean(configFile.getProperty("NORMALIZE"));
			this.randomSeed = Long.parseLong(configFile.getProperty("RANDOM_SEED"));


			// este for foi descomentado em 21/11/2022
			//Ler nomes dos descritores for 0 até N ...
//				for(int i = 0; i < numberOfDesc; i++){
//					descriptorNames.add( configFile.getProperty("DESCRIPTOR["+ i +"]"));
//				}
			// este for foi descomentado em 21/11/2022

			descriptorNames.add( configFile.getProperty("DESCRIPTOR["+ this.descriptorNumber +"]"));
			System.out.println("configFile.getProperty(DESCRIPTOR["+ this.descriptorNumber +"])");


			//Abre o arquivo topics.map
			Properties topicsConfig = new Properties();
			topicsConfig.load(new FileInputStream(dataset + File.separator + topics));

			this.initTopicId = Integer.parseInt(topicsConfig.getProperty("INIT_TOPIC_ID"));
			this.lastTopicId = Integer.parseInt(topicsConfig.getProperty("LAST_TOPIC_ID"));

			for(int topicId = initTopicId; topicId <= lastTopicId; topicId++){
				String topic = topicsConfig.getProperty("" + topicId);
				topicsMap.put(topicId, topic);
			}

			//Ler o arquivo topicsSize e recupera a quantidade de tópicos
			Properties properties = new Properties();
			properties.load(new FileInputStream(dataset + File.separator + topicsSizeFileName));
			for(int topicId = initTopicId; topicId <= lastTopicId; topicId++){
				int topicSize = Integer.parseInt(properties.getProperty("" + topicId));
				numberOfImages.put(topicId, topicSize);
			}

			int size = numberOfImages.get(idTopic);

			System.out.println("...."+descriptorNames);

			for(int i = 0; i < numberOfDesc; i++) {
				String topicName = topicsMap.get(idTopic);

				String distBinFileName = (collection_Dir + topicName + "_" + descriptorNames.get(i) + "_dist.bin");

				System.out.println("....distbin: " + distBinFileName);

				//FileInputStream fileInputStream = new FileInputStream(dataset + File.separator + distBinFileName);
				FileInputStream fileInputStream = new FileInputStream(distBinFileName);
				DataInputStream dataInput = new DataInputStream(fileInputStream);

				ArrayList<Double> distBin = new ArrayList<Double>();
				int numDists = size * size;

				for(int j = 0; j < numDists; j++){
					double dist = dataInput.readDouble();
					distBin.add(dist);
					//System.out.println(j + ":" + dist);
				}
				fileInputStream.close();
				dataInput.close();

				//Conversão da matriz inline para uma matriz bidimensional
				double[][] matrix = new double[size][size];
				int index = 0;

				for(int l = 0; l < size; l++){
					for(int c = 0; c < size; c++){
						matrix[l][c] = distBin.get(index);
						index++;

						if(Double.isNaN(matrix[l][c])){
							System.err.println("NaN value in descriptor: " + index);
							System.exit(1);
						}
					}
				}

				if(this.normalize)
					matrix = minMaxNorm(matrix);

				arrayMatrix.add(matrix);

				// tentar aninhar apenas a primeira linha de cada tópico no arquivo objetos
				// tentar aninhar apenas a primeira linha de cada tópico no arquivo objetos
				// tentar aninhar apenas a primeira linha de cada tópico no arquivo objetos
				String dir = System.getProperty("user.dir");
				File file = new File(dir+"/one/results/objectsDist/objects_"+descriptorNames.get(i)+"_topic_"+idTopic+".txt");
				try{
					BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
					for(int l = 0; l < size; l++){
						String sep = ",";
						if(l>=(size-1))
							sep = "";
						bw.write(l+sep);
					}
					bw.newLine();
//					for(int l = 0; l < size; l++){
						String sep = ",";
						for(int c = 0; c < size; c++){
							if(c>=(size-1))
								sep = "";
							bw.write(matrix[0][c]+sep);
						}
						bw.newLine();
//					}
					bw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			Properties topicMap = new Properties();
			topicMap.load(new FileInputStream(dataset + File.separator + "resources/maps" + File.separator + topicsMap.get(idTopic) + ".map"));

			for(int i = 0; i < numberOfImages.get(idTopic); i++){
				String imgId = topicMap.getProperty("" + i);
				imageIndex.put(imgId, i);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private double[][] minMaxNorm(double matrix[][]){
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if(matrix[i][j] < min)
					min = matrix[i][j];

				if(matrix[i][j] > max)
					max = matrix[i][j];
			}
		}

		int index = 0;
		//System.out.println("Norm Matrix");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				double normValue = (matrix[i][j] - min) / (max - min);
				matrix[i][j] = normValue;

				//System.out.println(index + "=" + normValue);
				index++;
			}
		}

		return matrix;
	}

	public String getDataset() {
		return this.dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	/**
	 * Obtém a média das distâncias de uma imagem para outra utilizando todos os descritores
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public double getAvgDistance(IDigitalObject obj1, IDigitalObject obj2){

		double[] valuesDistance = this.getDistance(obj1, obj2);
		int size = valuesDistance.length;
		double meanDistance = 0;
		for(int i = 0; i < size; i++){
			meanDistance += valuesDistance[i];
		}
		//System.out.println("Distancia objeto "+ obj1.getId() +" e "+ obj2.getId());

		//System.out.println("Distancia média: "+meanDistance/size);
		return meanDistance/size;
	}

	public double getVetorDifference(IDigitalObject obj1, IDigitalObject obj2){
		// precisa pegar os obj1 e obj2 e retirar a diferença entre as normas dos dois
		double[] valuesDistance = this.getDistance(obj1, obj2);
		int size = valuesDistance.length;
		double difference = 0;
		for(int i = 0; i < size; i++){
			difference -= valuesDistance[i];
		}
		return difference;
	}

	/**
	 * Obtém a média das distâncias de uma imagem para outra utilizando todos os descritores
	 * @param id1
	 * @param id2
	 * @return
	 */
	public double getAvgDistance(String id1, String id2){

		double[] valuesDistance = this.getDistance(id1, id2);
		int size = valuesDistance.length;
		double meanDistance = 0;
		for(int i = 0; i < size; i++){
			meanDistance += valuesDistance[i];
		}

		return meanDistance/size;
	}

	/**
	 * Obtém as distâncias de uma imagem para outra utilizando todos os descritores
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public double[] getDistance(IDigitalObject obj1, IDigitalObject obj2){

		double[] valueDistance = new double[numberOfDesc];

		//Recupera a distância das imagens passadas no parâmetro
		int target = imageIndex.get(obj1.getId());
		int idObject = imageIndex.get(obj2.getId());
		//System.out.println("Aqui o objeto "+ target +" "+idObject);

		for(int i = 0; i < numberOfDesc; i++) {
			double[][] matrix = arrayMatrix.get(i);
			valueDistance[i] = matrix[target][idObject];
			//System.out.println("Aqui o objeto "+ matrix[target][idObject]);
		}

		return valueDistance;
	}

	/**
	 * Obtém as distâncias de uma imagem para outra utilizando todos os descritores
	 * @param id1
	 * @param id2
	 * @return
	 */
	public double[] getDistance(String id1, String id2){

		double[] valueDistance = new double[numberOfDesc];

		//Recupera a distância das imagens passadas no parâmetro
		int target = imageIndex.get(id1);
		int idObject = imageIndex.get(id2);

		for(int i = 0; i < numberOfDesc; i++) {
			double[][] matrix = arrayMatrix.get(i);
			valueDistance[i] = matrix[target][idObject];
		}

		return valueDistance;
	}

	public long getRandomSeed(){
		return this.randomSeed;
	}

	/**
	 * Recupera a distância de uma imagem para outra, utilizando um descritor especifico
	 * @param id1
	 * @param id2
	 * @param descriptor
	 * @return
	 */
	public double getDistance(int id1, int id2, int descriptor){

		double[][] matrix = arrayMatrix.get(descriptor);
		double valueDistance = 0;

		//Recupera o valor da distancia entre as imagens
		valueDistance = matrix[id1][id2];

		return valueDistance;
	}

	/**
	 * Recupera a distância de uma imagem para outra, utilizando um descritor especifico
	 * @param obj1
	 * @param obj2
	 * @param descriptor
	 * @return
	 */
	public double getDistance(IDigitalObject obj1, IDigitalObject obj2, int descriptor){

		//Recebe a matriz do descritor
		double[][] matrix = arrayMatrix.get(descriptor);

		int target = imageIndex.get(obj1.getId());
		int idObject = imageIndex.get(obj2.getId());

		return matrix[target][idObject];
	}

	/**
	 * Obtém a média das distâncias de uma imagem para outra utilizando todos os descritores
	 *  e seus respectivos pesos no Algoritmo Genético
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public double getGADistance(IDigitalObject obj1, IDigitalObject obj2, XReal individual){

		double[] valuesDistance = this.getDistance(obj1, obj2);
		int size = valuesDistance.length;
		double weightedDistance = 0;
		double weightSum = 0;
		try {
			for(int i = 0; i < size; i++){
				weightedDistance += valuesDistance[i] * individual.getValue(i);
				weightSum += individual.getValue(i);
			}
		} catch (JMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return weightedDistance/weightSum;
	}
}