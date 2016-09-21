package com.scryAnalytics.GateTrainer;

import java.io.File;
import java.net.MalformedURLException;

import gate.Corpus;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.learning.EvaluationBasedOnDocs;
import gate.util.GateException;

import org.apache.log4j.Logger;

public class GateMLService {

	static Logger log = Logger.getLogger(GateMLService.class);

	private File mlConfigFile;
	gate.learning.LearningAPIMain learner;
	SerialAnalyserController controller;

	public GateMLService(String pluginHome, String mlconfigFilePath)
			throws GateException, MalformedURLException {

		if (!Gate.isInitialised()) {
			Gate.runInSandbox(true);
			Gate.init();
			Gate.setPluginsHome(new File(pluginHome));
		}

		Gate.getCreoleRegister().registerDirectories(
				new File(Gate.getPluginsHome(), "Learning").toURI().toURL());

		mlConfigFile = new File(mlconfigFilePath);

	}

	public void trainModel(Corpus corpus) {

		try {

			FeatureMap fm = Factory.newFeatureMap();
			fm.put("configFileURL", mlConfigFile.toURI().toURL());

			fm.put("learningMode", "TRAINING");

			learner = (gate.learning.LearningAPIMain) gate.Factory
					.createResource("gate.learning.LearningAPIMain", fm);
			controller = (gate.creole.SerialAnalyserController) Factory
					.createResource("gate.creole.SerialAnalyserController");
			controller.setCorpus(corpus);

			controller.add(learner);

			controller.execute();

		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		}

	}

	public void evaluateModel(Corpus corpus) {

		try {

			FeatureMap fm = Factory.newFeatureMap();
			fm.put("configFileURL", mlConfigFile.toURI().toURL());

			fm.put("learningMode", "EVALUATION");

			learner = (gate.learning.LearningAPIMain) gate.Factory
					.createResource("gate.learning.LearningAPIMain", fm);
			controller = (gate.creole.SerialAnalyserController) Factory
					.createResource("gate.creole.SerialAnalyserController");
			controller.setCorpus(corpus);

			controller.add(learner);

			controller.execute();

			EvaluationBasedOnDocs ev = learner.getEvaluation();
			System.out.println("Precision :       "
					+ ev.macroMeasuresOfResults.precision + "\n"
					+ "Recall:           " + ev.macroMeasuresOfResults.recall
					+ "\n" + "f1:               "
					+ ev.macroMeasuresOfResults.f1 + "\n"
					+ "PrecisionLenient: "
					+ ev.macroMeasuresOfResults.precisionLenient + "\n"
					+ "RecallLenient:    "
					+ ev.macroMeasuresOfResults.recallLenient + "\n"
					+ "F1Lenient:        "
					+ ev.macroMeasuresOfResults.f1Lenient + "\n");

			System.out.println("Full Results-----------" + "\n"
							+ ev.macroMeasuresOfResults.printResults());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
