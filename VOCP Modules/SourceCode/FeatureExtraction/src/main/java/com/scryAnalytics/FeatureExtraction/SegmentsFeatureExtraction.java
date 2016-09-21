package com.scryAnalytics.FeatureExtraction;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.scryAnalytics.FeatureExtraction.DAO.GateAnnotation;
import com.scryAnalytics.FeatureExtraction.DAO.Input;
import com.scryAnalytics.FeatureExtraction.DAO.Output;
import com.scryAnalytics.FeatureExtraction.DAO.VOCPEntities;

public class SegmentsFeatureExtraction {

	static Logger logger = Logger.getLogger(SegmentsFeatureExtraction.class);
	private SerialAnalyserController applicationPipeline;
	private Corpus corpus;
	private String entityName;

	public static Properties properties = new Properties();

	static {
		try {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			InputStream input = classLoader
					.getResourceAsStream("conf/config.properties");

			properties.load(input);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SegmentsFeatureExtraction(String pluginHome, VOCPEntities entity) {
		entityName = entity.getVocpEntity();

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

			prepareApplicationPipleine(entityName);

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

	public String generateFeatures(String message, String inputAnnotations) {

		Document doc = null;
		String resultJson = "";

		try {
			doc = Factory.newDocument(message);

			Input inputObject = Utility.jsonToInput(inputAnnotations);

			Method[] methods = inputObject.getClass().getMethods();
			for (Method method : methods) {
				if (method.getName().startsWith("get")
						&& method.getGenericParameterTypes().length == 0
						&& !(method.getName().equals("getClass"))) {
					Object returnObject = method.invoke(inputObject);
					if (returnObject != null) {
						@SuppressWarnings("unchecked")
						List<GateAnnotation> ann = (List<GateAnnotation>) returnObject;
						if (ann != null && !ann.isEmpty())
							doc.getAnnotations().addAll(ann);
					}

				}
			}

			corpus.add(doc);
			applicationPipeline.execute();
			logger.info(entityName);	
			resultJson = prepareResultJson(entityName);
			Factory.deleteResource(doc);
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

	private Resource getJapeTransducer(String path) throws Exception {
		logger.info(path);
		LanguageAnalyser jape = (LanguageAnalyser) gate.Factory.createResource(
				"gate.creole.Transducer", gate.Utils.featureMap("grammarURL",
						new File(path).toURI().toURL(), "encoding", "UTF-8"));
		return jape;

	}

	private void prepareApplicationPipleine(String entity) throws Exception {

		applicationPipeline = (SerialAnalyserController) Factory
				.createResource("gate.creole.SerialAnalyserController");

		String japeFolderPath = Gate.getPluginsHome().getPath()
				+ "/Resources/FeatureExtraction/";

		applicationPipeline
				.add((gate.LanguageAnalyser) getJapeTransducer(japeFolderPath
						+ properties.getProperty("WORKING_SET_JAPE")));

		// Pipeline Creation
		List<String> segmentCreationJapePaths = Arrays.asList(properties
				.getProperty(entity + "_SEGMENT_JAPE").split(","));

		for (String japeFilepath : segmentCreationJapePaths) {
			applicationPipeline
					.add((gate.LanguageAnalyser) getJapeTransducer(japeFolderPath
							+ japeFilepath));
		}

		if (!properties.getProperty(entity + "_INSTANCE_JAPE").isEmpty()) {
			applicationPipeline
					.add((gate.LanguageAnalyser) getJapeTransducer(japeFolderPath
							+ properties.getProperty(entity + "_INSTANCE_JAPE")));
		}

	}

	private List<GateAnnotation> getannotationList(String annotationName) {
		List<GateAnnotation> entities = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet entitySet = defaultAnnSet.get(properties
				.getProperty(annotationName));
		for (Annotation annotation : entitySet) {
			if (annotation.getType().equals(properties.getProperty(entityName + "_INSTANCE_NAME"))) {

				Integer entityId = Integer.parseInt((String) annotation
						.getFeatures().get(
								properties.getProperty(entityName
										+ "_DISPLAY_NAME")
										+ "-Id"));
				Integer sideEffectId = Integer.parseInt((String) annotation
						.getFeatures().get("sideEffect-Id"));

				String entityValue = (String) defaultAnnSet.get(entityId)
						.getFeatures().get("standardName");
				String sideEffectName = (String) defaultAnnSet
						.get(sideEffectId).getFeatures().get("standardName");

				annotation.getFeatures().put(
						properties.getProperty(entityName + "_DISPLAY_NAME"),
						entityValue);
				annotation.getFeatures().put("sideEffect", sideEffectName);
			}

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

		result.setSegment(getannotationList(entityName + "_SEGMENT_NAME"));

		if (!properties.getProperty(entityName + "_INSTANCE_NAME").isEmpty()) {
			result.setSegmentInstance(getannotationList(entityName
					+ "_INSTANCE_NAME"));
		}

		if (!properties.getProperty(entityName + "_CLASS_NAME").isEmpty()) {
			result.setSegmentClass(getannotationList(entityName + "_CLASS_NAME"));
		}

		String resultJson = Utility.objectToJson(result);
		return resultJson;

	}

	public void close() {
		Factory.deleteResource(corpus);
		Factory.deleteResource(applicationPipeline);
	}

}
