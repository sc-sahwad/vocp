package com.scryAnalytics.GateTrainer;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.json.JSONArray;
import org.xhtmlrenderer.layout.TextUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import com.opencsv.*;
import gate.*;
import gate.annotation.NodeImpl;

import org.json.JSONArray;
import com.google.inject.spi.Message;
import com.scryAnalytics.GateTrainer.Configuration.VOCPConstants;
import com.scryAnalytics.GateTrainer.Configuration.VocpConfiguration;
import com.scryAnalytics.GateTrainer.DAO.GateAnnotation;
import com.scryAnalytics.GateTrainer.DAO.InputAnnotations;
import com.sun.tools.doclets.formats.html.resources.standard;
import com.sun.xml.txw2.Document;

public class CorpusTraining {

	static Logger log = Logger.getLogger(CorpusTraining.class);

	private static Configuration conf;
	private static HashSet<Integer> entity1 = new HashSet<Integer>();
	private static HashSet<Integer> entity2 = new HashSet<Integer>();
	/**
	 * Initialization
	 */
	static {
		conf = VocpConfiguration.create();
	}

	public String preprocess(String message){
		String temp = "";
		for(int i = 0; i < message.length(); i++){
			if(message.charAt(i) != ',' && message.charAt(i) != '"' && message.charAt(i) != '?' && !Character.isDigit(message.charAt(i))
					&& message.charAt(i) != '*'){
				temp += message.charAt(i);
			}
		}
		message = temp;
		message = message.replaceAll("\\s+", " ");
		message = message.replaceAll("!", ".");
		message = message.replaceAll("\\.{2,}", ".");
		try{
			Scanner file = new Scanner(new File("/home/sahil/Voice-of-Cancer-Patients/"
					+ "VOCP Modules/SourceCode/GateTraining/GateTrainer/stopwords.txt"));
			Set<String> stopWordsSet = new HashSet<String>();
			String[] lines = message.split("\\.");
			while(file.hasNext()){
				stopWordsSet.add(file.next());
			}
			message = "";
			for(String line: lines){
				temp = "";
				String[] words = line.split(" ");
				for(String word: words){
					if(!stopWordsSet.contains(word.toLowerCase())){
						temp += (word + " ");
					}
				}
				if(temp.length() >= 2){
					temp = temp.substring(0, temp.length() - 1);
					message += temp + ".";
				}
			}
			if(message.startsWith(" "))
				message = message.substring(1, message.length());
		}
		catch (Exception e){}

		return message;
	}

	public String getOffset(String entityJson, String message, int flag){
		List<GateAnnotation> ann = Utility.jsonToAnnotations(entityJson);
		List<GateAnnotation> bnn  = new ArrayList<GateAnnotation>();
		HashMap<String, Integer> hm = new HashMap<>();
		for(int j = 0; j < ann.size(); j++){
			GateAnnotation brr = ann.get(j);
			String standardName = brr.getFeatures().get("standardName").toString();
			standardName = preprocess(standardName);
			if(standardName.length() == 0){
				if(flag == 0) entity1.add(Integer.parseInt(brr.getFeatures().get("id").toString()));
				else entity2.add(Integer.parseInt(brr.getFeatures().get("id").toString()));
				continue;
			}
			if(standardName.length() > 0 && standardName.charAt(standardName.length() - 1) == '.'){
				standardName = standardName.substring(0, standardName.length() - 1);
				brr.getFeatures().put("standardName", standardName);
			}
			int idx = -1;
			if(!hm.containsKey(standardName)){
				idx = message.indexOf(standardName);
			}
			else{
				idx = message.indexOf(standardName, hm.get(standardName));
			}
			if(idx != -1){
				int id = brr.getId();
				NodeImpl newNode = new NodeImpl(id, new Long(idx));
				brr.setStartNode(newNode);
				newNode = new NodeImpl(id, new Long(idx + standardName.length()));
				brr.setEndNode(newNode);
				hm.put(standardName, idx + 1);
			}
			ann.set(j, brr);
			bnn.add(brr);
		}
		return Utility.objectToJson(bnn);
	}

	public String getRelation(String relationJson){
		List<GateAnnotation> ann = Utility.jsonToAnnotations(relationJson);
		List<GateAnnotation> bnn = new ArrayList<GateAnnotation>();
		for(int i = 0; i < ann.size(); i++){
			GateAnnotation brr = ann.get(i);
			int entity1_id = Integer.parseInt(brr.getFeatures().get("entity1-id").toString());
			int entity2_id = Integer.parseInt(brr.getFeatures().get("entity2-id").toString());
			if( (!entity1.isEmpty() && entity1.contains(entity1)) || (!entity2.isEmpty() && entity2.contains(entity2))){
				continue;
			}
			bnn.add(brr);
		}
		return Utility.objectToJson(bnn);
	}
	public int trainModel() {

		int res = 1;

		String pluginHome = conf.get(VOCPConstants.GATE_PLUGIN_DIR);
		String mlconfigFilePath = conf.get(VOCPConstants.ML_CONFIG_FILE_PATH);
		String annotTableName = conf.get(VOCPConstants.ANNOTTABLE);

		try {
			String entity1 = conf.get(VOCPConstants.ENTITY1);
			String entity2 = conf.get(VOCPConstants.ENTITY2);
			String relation = conf.get(VOCPConstants.RELATION);
			String annotFlag = conf.get(VOCPConstants.FLAGCOLUMN);
			HbaseDriver hbaseDriver = new HbaseDriver(conf, annotTableName);
			ResultScanner annotTableScanner = hbaseDriver
					.getAnnotTableScanner(annotFlag);

			String segmentJapeFiles = conf.get(VOCPConstants.SEGMENTJAPE);
			String instanceJapeFiles = conf.get(VOCPConstants.INSTANCEJAPE);

			GateFeatureGeneration featureGeneration = new GateFeatureGeneration(
					pluginHome, entity1, segmentJapeFiles, instanceJapeFiles);
			int count = 0;
			List<String> str= new ArrayList<String>();
			List<String> row = new ArrayList<String>();
		    for (Result messageresult : annotTableScanner) {
				 String rowkey = Bytes.toString(messageresult.getRow());
				String message = Bytes.toString(messageresult.getValue(
						Bytes.toBytes("annot"), Bytes.toBytes("message")));
				String entity1Json = Bytes.toString(messageresult.getValue(
						Bytes.toBytes("annot"), Bytes.toBytes(entity1)));
				String entity2Json = Bytes.toString(messageresult.getValue(
						Bytes.toBytes("annot"), Bytes.toBytes(entity2)));
				String relationJson = Bytes.toString(messageresult.getValue(
						Bytes.toBytes("relations"), Bytes.toBytes(relation)));
//				JSONArray annotArr = new JSONArray(entity1Json);
//				for(int k = 0; k < annotArr.length(); k++){
//					String val = annotArr.getJSONObject(k).getString("features");
//					JSONObject obj = new JSONObject(val);
//					String name = obj.getString("standardName");
//					if(name.charAt(name.length() - 1) == '.'){
//						name = name.substring(0, name.length() - 1);
//						obj.put("standardName", name);
//					}
//					name = obj.getString("standardName");
//					System.out.println(obj);
//					annotArr.getJSONObject(k).put("features", obj) ;
//					System.out.println(name);
//					val = annotArr.getJSONObject(k).getString("startNode");
//					obj = new JSONObject(val);
//					Long offset = obj.getLong("offset");
//				}


				if (entity1Json != null && entity2Json != null
						&& relationJson != null) {
//					if(count>100) break;
					message = preprocess(message);

					entity1Json = getOffset(entity1Json, message, 0);

					entity2Json = getOffset(entity2Json, message, 1);

				    relationJson = getRelation(relationJson);
				    InputAnnotations inputAnnots = new InputAnnotations();
					inputAnnots.setEntity1(Utility
							.jsonToAnnotations(entity1Json));
					inputAnnots.setEntity2(Utility
							.jsonToAnnotations(entity2Json));
					inputAnnots.setRelation(Utility
							.jsonToAnnotations(relationJson));
					//System.out.println(message);
					//System.out.println("Entity1 :  " + entity1Json);
					//System.out.println("Entity2 : " + entity2Json);
					//System.out.println("Relation  :" + relationJson);
					str.add(message);
					row.add(rowkey);
					featureGeneration.addDocInCorpus(message, inputAnnots,
							entity1, entity2);
					System.out.println(count++);
				}
			}
//		     CSVWriter writer = new CSVWriter(new FileWriter("/home/sahil/Desktop/DrugSE_Results/relation_2.csv"));
//		     List<String> data = new ArrayList<String>();
		     Corpus corpus = featureGeneration.generateFeatures();
//			System.out.println("Corpus:" +corpus.get(0));
			GateMLService mlService = new GateMLService(pluginHome,
					mlconfigFilePath);
			mlService.evaluateModel(corpus);
			Double val = 0.0, tot = 0.0;
			int sentences = 0;
			int pairOrderSE = 0, pairOrderDrug = 0;
			for(int i = 0; i < corpus.size(); i++){
				gate.Document dc = corpus.get(i);
//				System.out.println(dc.getAnnotations());
//				System.out.println(str.get(i));
				for(Annotation ann: dc.getAnnotations().get("RelationClass")){
//					data.clear();
//					data.add(row.get(i));
//					data.add(str.get(i));
					 if(ann.getFeatures().containsKey("rel-type")){
						 String predicted = ann.getFeatures().get("rel-type").toString();
//						 if(ann.getFeatures().get("pairOrder").toString().equals("drug-SE")) pairOrderDrug++;
//						 else pairOrderSE++;
//						 if(Integer.parseInt(ann.getFeatures().get("sentencesInBetween").toString()) > 1){
//							 sentences++;
//						 }
						 String target = "";
						 int startOffset = Integer.parseInt(ann.getStartNode().getOffset().toString());
						 int endOffset = Integer.parseInt(ann.getEndNode().getOffset().toString());
						 String segment = str.get(i).substring(startOffset, endOffset);
//						 data.add(segment);
						 int drug_id = Integer.parseInt(ann.getFeatures().get("drug-Id").toString());
						 int sideEffect_id = Integer.parseInt(ann.getFeatures().get("sideEffect-Id").toString());
//						 for(Annotation relAnn : dc.getAnnotations().get("OrigClass")){
//							 if(Integer.parseInt(relAnn.getFeatures().get("drug-Id").toString()) == drug_id &&
//									 Integer.parseInt(relAnn.getFeatures().get("sideEffect-Id").toString()) == sideEffect_id){
//								 target = relAnn.getFeatures().get("value").toString();
//								 break;
//							 }
//						 }
//						 System.out.println(predicted + "--" + target);

						 for(Annotation drugAnn : dc.getAnnotations().get("Drug")){
							 if(Integer.parseInt(drugAnn.getFeatures().get("id").toString()) == drug_id ){
//								 System.out.println(drugAnn.getFeatures().get("standardName").toString());
//								 data.add(drugAnn.getFeatures().get("standardName").toString());
								 break;
							 }
						 }
						 for(Annotation SEAnn : dc.getAnnotations().get("Side_Effect")){
							 if(Integer.parseInt(SEAnn.getFeatures().get("id").toString()) == sideEffect_id){
//								 System.out.println(SEAnn.getFeatures().get("standardName").toString());
//								 data.add(SEAnn.getFeatures().get("standardName").toString());
								 break;
							 }
						 }
//						 data.add(predicted);
//						 data.add(target);
//						 System.out.println(data);
//						 writer.writeNext(data.toArray(new String[0]));
//					     PrintWriter pwriter = new PrintWriter("/home/sahil/Desktop/DrugSE_Results/temp/"+row.get(i)+".xml", "UTF-8");
//					     pwriter.println(dc.toXml());
//					     pwriter.close();
						 if(predicted.equals(target)) val++;
						 tot++;
					 }
				}
			}
//			writer.close();
//			System.out.println(sentences);
//			System.out.println(pairOrderDrug + " " + pairOrderSE);
//			System.out.println("Overall accuracy= " + val/tot);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;

	}

	public static void main(String[] args) throws Exception {
		String arg[] = new String[2];
		arg[0] = "-annotTable";
		arg[1] = "bratAnnotationsNtemp";
		args = arg;
		String usage = "Usage: ModelTrainer" + " -annotTable tableName";

		if (args.length == 0) {
			System.err.println(usage);
			System.exit(1);

		}

		for (int i = 0; i < args.length; i++) {

			if ("-annotTable".equals(args[i])) {
				conf.set(VOCPConstants.ANNOTTABLE, args[++i]);

			} else {
				throw new IllegalArgumentException("arg " + args[i]
						+ " not recognized");
			}

		}

		CorpusTraining mlTrainer = new CorpusTraining();
		int res = mlTrainer.trainModel();
		System.exit(res);
	}

}
