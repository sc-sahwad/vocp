package com.scryAnalytics.GateTrainer;

import java.util.Arrays;
import java.util.List;

import gate.Corpus;
import gate.Document;
import gate.FeatureMap;
import gate.Gate;
import gate.Factory;
import gate.LanguageAnalyser;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.POSTagger;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;

import java.io.File;

import org.apache.log4j.Logger;

import com.scryAnalytics.GateTrainer.DAO.GateAnnotation;
import com.scryAnalytics.GateTrainer.DAO.InputAnnotations;

public class GateFeatureGeneration {

	static Logger logger = Logger.getLogger(GateFeatureGeneration.class);
	private SerialAnalyserController applicationPipeline;
	private Corpus corpus;
	private String entity;

	public GateFeatureGeneration(String pluginHome, String entityName,
			String segmentJapes, String instanceJapes) {
		entity = entityName;

		try {

			if (!Gate.isInitialised()) {
				Gate.runInSandbox(true);
				Gate.init();
				Gate.setPluginsHome(new File(pluginHome));
			}

			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), "ANNIE").toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), "Tools").toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), "Tagger_NP_Chunking")
							.toURI().toURL());

			// Pipeline Instantation
			applicationPipeline = (SerialAnalyserController) Factory
					.createResource("gate.creole.SerialAnalyserController");

			// NLP Generic Related PLugins
			applicationPipeline.add((gate.LanguageAnalyser) Factory
					.createResource("gate.creole.tokeniser.DefaultTokeniser"));
			applicationPipeline.add((gate.LanguageAnalyser) Factory
					.createResource("gate.creole.splitter.SentenceSplitter"));

			applicationPipeline
					.add((gate.LanguageAnalyser) createPOSTaggerPR());

			applicationPipeline.add((gate.LanguageAnalyser) Factory
					.createResource("gate.creole.VPChunker"));

			applicationPipeline.add((gate.LanguageAnalyser) Factory
					.createResource("mark.chunking.GATEWrapper"));

			// Feature Generation Plugins
			prepareApplicationPipleine(entityName, segmentJapes, instanceJapes);

			corpus = Factory.newCorpus("VOCP Corpus");
			applicationPipeline.setCorpus(corpus);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			corpus.cleanup();
			applicationPipeline.cleanup();
			close();
		}

	}

	private Resource createPOSTaggerPR() throws ResourceInstantiationException {
		FeatureMap posTaggerParams = Factory.newFeatureMap();
		posTaggerParams.put(
				POSTagger.BASE_SENTENCE_ANNOTATION_TYPE_PARAMETER_NAME,
				"Sentence");
		posTaggerParams.put(
				POSTagger.BASE_TOKEN_ANNOTATION_TYPE_PARAMETER_NAME, "Token");
		posTaggerParams.put(POSTagger.OUTPUT_ANNOTATION_TYPE_PARAMETER_NAME,
				"Token");

		Resource posTagger = Factory.createResource("gate.creole.POSTagger",
				posTaggerParams);
		return posTagger;

	}

	private void prepareApplicationPipleine(String entity, String segmentJapes,
			String instanceJapes) throws Exception {

		String japeFolderPath = Gate.getPluginsHome().getPath()
				+ "/TrainingResources/FeatureExtraction/";

		//applicationPipeline
		//		.add((gate.LanguageAnalyser) getJapeTransducer(japeFolderPath
		//				+ "JapeFiles/WorkingSetCreation.jape"));

		// Pipeline Creation
		List<String> segmentCreationJapePaths = Arrays.asList(segmentJapes
				.split(","));

		for (String japeFilepath : segmentCreationJapePaths) {
			applicationPipeline
					.add((gate.LanguageAnalyser) getJapeTransducer(japeFolderPath
							+ japeFilepath));
		}

		applicationPipeline
				.add((gate.LanguageAnalyser) getJapeTransducer(japeFolderPath
						+ instanceJapes));

	}

	private Resource getJapeTransducer(String path) throws Exception {
		logger.info(path);
		LanguageAnalyser jape = (LanguageAnalyser) gate.Factory.createResource(
				"gate.creole.Transducer", gate.Utils.featureMap("grammarURL",
						new File(path).toURI().toURL(), "encoding", "UTF-8"));
		return jape;

	}

	public void addDocInCorpus(String message, InputAnnotations inputAnnots,
			String entity1Type, String entity2Type) {

		Document doc = null;

		try {
			doc = Factory.newDocument(message);

			List<GateAnnotation> entity1 = inputAnnots.getEntity1();
			List<GateAnnotation> entity2 = inputAnnots.getEntity2();
			List<GateAnnotation> relations = inputAnnots.getRelation();

			int drug_Id = -1;
			int sideEffect_Id = -1;
			for (GateAnnotation relation : relations) {
				if (relation.getFeatures().get("entity1-type")
						.equals(entity1Type)) {
					drug_Id = Integer.parseInt((String) relation.getFeatures()
							.get("entity1-id"));
				}
				if (relation.getFeatures().get("entity2-type")
						.equals(entity2Type)) {
					sideEffect_Id = Integer.parseInt((String) relation
							.getFeatures().get("entity2-id"));
				}

				Long e1StartOffset = null, e2StartOffset = null, e1EndOffset = null, e2EndOffset = null;
				Long rStartOffset = null, rEndOffset = null;

				for (GateAnnotation entity : entity1) {
					if (drug_Id == entity.getId()) {
						e1StartOffset = entity.getStartNode().getOffset();
						e1EndOffset = entity.getEndNode().getOffset();
						break;
					}

				}

				for (GateAnnotation entity : entity2) {
					if (sideEffect_Id == entity.getId()) {
						e2StartOffset = entity.getStartNode().getOffset();
						e2EndOffset = entity.getEndNode().getOffset();
						break;
					}

				}

				if (e1StartOffset != null && e2StartOffset != null) {

					if (e1StartOffset < e2StartOffset)
						rStartOffset = e1StartOffset;
					else
						rStartOffset = e2StartOffset;

					if (e1EndOffset < e2EndOffset)
						rEndOffset = e2EndOffset;
					else
						rEndOffset = e1EndOffset;

					doc.getAnnotations().add(rStartOffset, rEndOffset,
							relation.getType(), relation.getFeatures());
				}

			}

			doc.getAnnotations().addAll(entity1);
			doc.getAnnotations().addAll(entity2);

			corpus.add(doc);

		} catch (ResourceInstantiationException e) {

			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidOffsetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Corpus getCorpus() {
		return corpus;
	}

	public Corpus generateFeatures() {

		try {
			applicationPipeline.execute();
			logger.info(entity);
		} catch (ExecutionException e) {

			e.printStackTrace();
		}

		return corpus;
	}

	public void close() {
		Factory.deleteResource(corpus);
		Factory.deleteResource(applicationPipeline);
	}

}
