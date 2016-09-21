package com.scryAnalytics.GateMachineLearning;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import org.apache.log4j.Logger;

import com.scryAnalytics.GateMachineLearning.DAO.GateAnnotation;
import com.scryAnalytics.GateMachineLearning.DAO.MLInput;

public class GateMLService {

	static Logger log = Logger.getLogger(GateMLService.class);

	private File mlConfigFile;
	ProcessingResource classifier;
	SerialAnalyserController controller;
	private Corpus corpus;

	public GateMLService(String pluginHome, String mlconfigFilePath)
			throws GateException, MalformedURLException {

		Gate.runInSandbox(true);
		Gate.init();
		Gate.setPluginsHome(new File(pluginHome));
		Gate.getCreoleRegister().registerDirectories(
				new File(Gate.getPluginsHome(), "Learning").toURI().toURL());

		corpus = Factory.newCorpus("VOCP Corpus");
		mlConfigFile = new File(mlconfigFilePath);

		FeatureMap fm = Factory.newFeatureMap();
		fm.put("configFileURL", mlConfigFile.toURI().toURL());

		fm.put("learningMode", "APPLICATION");
		fm.put("outputASName", "Output");

		classifier = (ProcessingResource) gate.Factory.createResource(
				"gate.learning.LearningAPIMain", fm);

		controller = (gate.creole.SerialAnalyserController) Factory
				.createResource("gate.creole.SerialAnalyserController");
		controller.setCorpus(corpus);
		controller.add(classifier);

	}

	public String extractRelation(String message, String inputAnnotationsJson) {

		Document doc = null;
		String mlOutputJson = "";

		try {
			doc = prepareDocument(message, inputAnnotationsJson);
			corpus.add(doc);
			log.debug("Running Machine Learning Model ----->");

			controller.execute();

			AnnotationSet outputAnnSet = doc.getAnnotations("Output");
			AnnotationSet results = outputAnnSet.get("SegmentClass");

			List<GateAnnotation> mlOutput = new ArrayList<GateAnnotation>();

			for (Annotation annotation : results) {
				GateAnnotation gateAnnotation = new GateAnnotation(
						annotation.getId(), annotation.getStartNode(),
						annotation.getEndNode(), annotation.getType(),
						annotation.getFeatures());
				mlOutput.add(gateAnnotation);

			}

			mlOutputJson = Utility.objectToJson(mlOutput);
			Factory.deleteResource(doc);
		} catch (ResourceInstantiationException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return mlOutputJson;

	}

	private Document prepareDocument(String message, String inputAnnotationsJson)
			throws ResourceInstantiationException {
		MLInput input = Utility.jsonToMLInput(inputAnnotationsJson);

		Document doc = Factory.newDocument(message);
		doc.getAnnotations().addAll(input.getToken());
		doc.getAnnotations().addAll(input.getSentence());
		doc.getAnnotations().addAll(input.getSpaceToken());
		doc.getAnnotations().addAll(input.getVG());
		doc.getAnnotations().addAll(input.getNounChunk());
		doc.getAnnotations().addAll(input.getSplit());
		doc.getAnnotations().addAll(input.getDrug());
		doc.getAnnotations().addAll(input.getSideEffect());
		doc.getAnnotations().addAll(input.getRegimen());

		doc.getAnnotations().addAll(input.getAltDrug());
		doc.getAnnotations().addAll(input.getAltTherapy());
		doc.getAnnotations().addAll(input.getSegmentInstance());
		doc.getAnnotations().addAll(input.getSegmentClass());

		return doc;

	}

	public void close() {
		Factory.deleteResource(corpus);
		Factory.deleteResource(classifier);
		Factory.deleteResource(controller);
	}

}
