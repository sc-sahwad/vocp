package com.vocp.ner.main;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.vocp.ner.dao.GateAnnotation;
import com.vocp.ner.dao.Input;
import com.vocp.ner.dao.Output;
import com.vocp.ner.util.ApplicationConstants;
import com.vocp.ner.util.Utility;

public class GateNERImpl {

	static Logger logger = Logger.getLogger(GateNERImpl.class.getName());
	private SerialAnalyserController applicationPipeline;
	private Corpus corpus;
	private String entityName;
	private List<String> entityNames;
	private boolean drugGazInitialized = false;

	public static Properties properties = new Properties();

	static {
		try {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			InputStream input = classLoader
					.getResourceAsStream("conf/ner_config.properties");
			properties.load(input);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GateNERImpl(String pluginHome, String entity) {

		entityName = entity;

		if (pluginHome == null || pluginHome.trim().isEmpty()) {
			pluginHome = properties.getProperty("GATE_PLUGIN_HOME");
		}

		try {
			if (!Gate.isInitialised()) {
				Gate.runInSandbox(true);
				Gate.init();
				Gate.setPluginsHome(new File(pluginHome));
			}

			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), properties
							.getProperty("ANNIE_PLUGIN_HOME")).toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), properties
							.getProperty("TOOLS_PLUGIN_HOME")).toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), properties
							.getProperty("STRING_ANNOTATION_PLUGIN_HOME"))
							.toURI().toURL());

			applicationPipeline = (SerialAnalyserController) Factory
					.createResource("gate.creole.SerialAnalyserController");

			if (!entityName.equals(ApplicationConstants.REGIMEN_ENTITY)) {

				if (entityName.equals(ApplicationConstants.SIDE_EFFECT_ENTITY)) {
					applicationPipeline
							.add((gate.LanguageAnalyser) getGazetteerByName("CONTEXT"));
				}

				applicationPipeline
						.add((gate.LanguageAnalyser) getGazetteerByName(entityName));

				applicationPipeline
						.add((gate.LanguageAnalyser) getJapeTransducer(entityName));

			} else {

				applicationPipeline
						.add((gate.LanguageAnalyser) getGazetteerByName("REG_ABBV"));

				applicationPipeline
						.add((gate.LanguageAnalyser) getGazetteerByName(ApplicationConstants.DRUG_ENTITY));

				applicationPipeline
						.add((gate.LanguageAnalyser) getJapeTransducer(ApplicationConstants.DRUG_ENTITY));

				applicationPipeline
						.add((gate.LanguageAnalyser) getJapeTransducer("REG_ABBV"));

				applicationPipeline
						.add((gate.LanguageAnalyser) getJapeTransducer("REG_SPECIAL_FINDER"));

				applicationPipeline
						.add((gate.LanguageAnalyser) getJapeTransducer("REG_FINDER"));

				applicationPipeline
						.add((gate.LanguageAnalyser) getGazetteerByName("REG_FEATURE"));

			}

			corpus = Factory.newCorpus("VOCP Corpus");
			applicationPipeline.setCorpus(corpus);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public GateNERImpl(String pluginHome, List<String> entities) {

		entityNames = entities;

		if (pluginHome == null || pluginHome.trim().isEmpty()) {
			pluginHome = properties.getProperty("GATE_PLUGIN_HOME");
		}

		try {
			if (!Gate.isInitialised()) {
				Gate.runInSandbox(true);
				Gate.init();
				Gate.setPluginsHome(new File(pluginHome));
			}

			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), properties
							.getProperty("ANNIE_PLUGIN_HOME")).toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), properties
							.getProperty("TOOLS_PLUGIN_HOME")).toURI().toURL());
			Gate.getCreoleRegister().registerDirectories(
					new File(Gate.getPluginsHome(), properties
							.getProperty("STRING_ANNOTATION_PLUGIN_HOME"))
							.toURI().toURL());

			applicationPipeline = (SerialAnalyserController) Factory
					.createResource("gate.creole.SerialAnalyserController");

			for (String entityName : entityNames) {
				if (!entityName.equals(ApplicationConstants.REGIMEN_ENTITY)) {

					if (entityName
							.equals(ApplicationConstants.SIDE_EFFECT_ENTITY)) {
						applicationPipeline
								.add((gate.LanguageAnalyser) getGazetteerByName("CONTEXT"));
					}

					applicationPipeline
							.add((gate.LanguageAnalyser) getGazetteerByName(entityName));

					applicationPipeline
							.add((gate.LanguageAnalyser) getJapeTransducer(entityName));

					if (entityName.equals(ApplicationConstants.DRUG_ENTITY)) {
						drugGazInitialized = true;
					}

				} else {

					applicationPipeline
							.add((gate.LanguageAnalyser) getGazetteerByName("REG_ABBV"));

					if (!drugGazInitialized) {
						applicationPipeline
								.add((gate.LanguageAnalyser) getGazetteerByName(ApplicationConstants.DRUG_ENTITY));

						applicationPipeline
								.add((gate.LanguageAnalyser) getJapeTransducer(ApplicationConstants.DRUG_ENTITY));

					}

					applicationPipeline
							.add((gate.LanguageAnalyser) getJapeTransducer("REG_ABBV"));

					applicationPipeline
							.add((gate.LanguageAnalyser) getJapeTransducer("REG_SPECIAL_FINDER"));

					applicationPipeline
							.add((gate.LanguageAnalyser) getJapeTransducer("REG_FINDER"));

					applicationPipeline
							.add((gate.LanguageAnalyser) getGazetteerByName("REG_FEATURE"));

				}
			}

			corpus = Factory.newCorpus("VOCP Corpus");
			applicationPipeline.setCorpus(corpus);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String generateNER(String message, String input) {

		Input inputObject = Utility.jsonToInput(input);
		Document doc = null;
		String resultJson = "";

		try {
			doc = Factory.newDocument(message);
			doc.getAnnotations().addAll(inputObject.getToken());
			doc.getAnnotations().addAll(inputObject.getSentence());
			corpus.add(doc);
			applicationPipeline.execute();

			if (entityNames != null && !entityNames.isEmpty()) {
				resultJson = prepareResultJson(entityNames);
			} else {
				resultJson = prepareResultJson(entityName);
			}

		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.debug("clearing corpus.....");
			corpus.clear();
			Factory.deleteResource(doc);
		}

		return resultJson;

	}

	private Resource getGazetteerByName(String entityName) throws Exception {

		String path = properties.getProperty(entityName + "_GAZ_LIST_FILE");
		String gazType = properties.getProperty(entityName + "_GAZ_TYPE");
		String[] settings = properties
				.getProperty(entityName + "_GAZ_SETTINGS").split(",");

		if (settings.length % 2 != 0) {
			throw new RuntimeException("Not enough settings of gazetteer for "
					+ entityName);
		}

		String resourcePath = Gate.getPluginsHome().getPath()
				+ "/Resources/NamedEntityExtraction/" + path.split(",")[1];

		FeatureMap fm = Factory.newFeatureMap();
		fm.put(path.split(",")[0], new File(resourcePath).toURI().toURL());
		// fm.put("encoding", "UTF-8");

		for (int i = 0; i < settings.length; i += 2) {
			fm.put(settings[i], settings[i + 1]);
		}

		LanguageAnalyser gaz = (LanguageAnalyser) Factory.createResource(
				gazType, fm);

		return gaz;

	}

	/*
	 * private LanguageAnalyser getDefaultGaz(String resourcePath, String
	 * gazType) throws Exception { LanguageAnalyser gaz = (LanguageAnalyser)
	 * Factory.createResource(gazType, gate.Utils .featureMap("listsURL", new
	 * File(resourcePath).toURI() .toURL(), "encoding", "UTF-8",
	 * "caseSensitive", "false", "longestMatchOnly", "false", "wholeWordsOnly",
	 * "true")); return gaz; }
	 * 
	 * private LanguageAnalyser getExtendedGaz(String resourcePath, String
	 * gazType) throws Exception { LanguageAnalyser gaz = (LanguageAnalyser)
	 * Factory.createResource(gazType, gate.Utils .featureMap("configFileURL",
	 * new File(resourcePath).toURI() .toURL(), "caseConversionLanguage", "en",
	 * "caseSensitive", "false", "gazetteerFeatureSeparator", "\\t",
	 * "longestMatchOnly", "true", "matchAtWordEndOnly", "false",
	 * "matchAtWordStartOnly", "true", "outputAnnotationType",
	 * "VocpAnnotations")); return gaz;
	 * 
	 * }
	 */

	private Resource getJapeTransducer(String entityName) throws Exception {
		String path = properties.getProperty(entityName + "_JAPE");
		String resourcePath = Gate.getPluginsHome().getPath()
				+ "/Resources/NamedEntityExtraction/" + path;

		LanguageAnalyser jape = (LanguageAnalyser) gate.Factory.createResource(
				"gate.creole.Transducer", gate.Utils.featureMap("grammarURL",
						new File(resourcePath).toURI().toURL(), "encoding",
						"UTF-8"));
		return jape;

	}

	private List<GateAnnotation> getEntity(String entityName) {
		List<GateAnnotation> entities = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet entitySet = defaultAnnSet.get(properties
				.getProperty(entityName + "_ANNOTATION_NAME"));
		for (Annotation annotation : entitySet) {
			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));
			entities.add(annot);
		}
		return entities;
	}

	private String prepareResultJson(String entityName) throws Exception {
		Output result = new Output();
		Method methodName = Output.class.getDeclaredMethod("set" + entityName,
				List.class);
		methodName.invoke(result, getEntity(entityName));
		String resultJson = Utility.objectToJson(result);
		return resultJson;
	}

	private String prepareResultJson(List<String> entityNames) throws Exception {
		Output result = new Output();
		for (String entityName : entityNames) {
			Method methodName = Output.class.getDeclaredMethod("set"
					+ entityName, List.class);
			methodName.invoke(result, getEntity(entityName));
		}
		String resultJson = Utility.objectToJson(result);
		return resultJson;
	}

	public void close() {
		Factory.deleteResource(corpus);
		Factory.deleteResource(applicationPipeline);
	}

}
