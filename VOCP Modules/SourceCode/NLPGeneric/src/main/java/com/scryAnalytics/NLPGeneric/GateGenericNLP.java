package com.scryAnalytics.NLPGeneric;

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
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.POSTagger;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import org.apache.log4j.Logger;

import com.scryAnalytics.NLPGeneric.DAO.GateAnnotation;
import com.scryAnalytics.NLPGeneric.DAO.Output;

public class GateGenericNLP {

	static Logger logger = Logger.getLogger(GateGenericNLP.class.getName());
	private SerialAnalyserController applicationPipeline;
	private Corpus corpus;
	private List<NLPEntities> entitiesToGenerate;

	public GateGenericNLP(String pluginHome, List<NLPEntities> entities)
			throws GateException, MalformedURLException {

		entitiesToGenerate = entities;
		Gate.runInSandbox(true);

		Gate.init();
		Gate.setPluginsHome(new File(pluginHome));
		Gate.getCreoleRegister().registerDirectories(
				new File(Gate.getPluginsHome(), "ANNIE").toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(
				new File(Gate.getPluginsHome(), "Tools").toURI().toURL());
		Gate.getCreoleRegister().registerDirectories(
				new File(Gate.getPluginsHome(), "Tagger_NP_Chunking").toURI()
						.toURL());
		applicationPipeline = (SerialAnalyserController) Factory
				.createResource("gate.creole.SerialAnalyserController");

		applicationPipeline.add((gate.LanguageAnalyser) Factory
				.createResource("gate.creole.tokeniser.DefaultTokeniser"));
		applicationPipeline.add((gate.LanguageAnalyser) Factory
				.createResource("gate.creole.splitter.SentenceSplitter"));

		if (entitiesToGenerate.contains(NLPEntities.POS_TAGGER)) {
			applicationPipeline
					.add((gate.LanguageAnalyser) createPOSTaggerPR());

		}

		if (entitiesToGenerate.contains(NLPEntities.VP_CHUNKER)) {
			applicationPipeline.add((gate.LanguageAnalyser) Factory
					.createResource("gate.creole.VPChunker"));

		}

		if (entitiesToGenerate.contains(NLPEntities.NP_CHUNKER)) {
			applicationPipeline.add((gate.LanguageAnalyser) Factory
					.createResource("mark.chunking.GATEWrapper"));

		}

		corpus = Factory.newCorpus("VOCP Corpus");
		applicationPipeline.setCorpus(corpus);

	}

	public String generateNLPEntities(String message) {

		Document doc = null;
		String resultJson = "";

		try {
			doc = Factory.newDocument(message);
			corpus.add(doc);
			applicationPipeline.execute();

			resultJson = prepareResultJson();
			Factory.deleteResource(doc);

		} catch (ResourceInstantiationException e) {

			e.printStackTrace();
		} catch (ExecutionException e) {

			e.printStackTrace();
		} finally {
			logger.debug("clearing corpus.....");
			corpus.clear();
			Factory.deleteResource(doc);
		}

		return resultJson;

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

	private List<GateAnnotation> getTokens() {
		List<GateAnnotation> tokens = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet tokenSet = defaultAnnSet.get("Token");
		for (Annotation annotation : tokenSet) {

			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));

			tokens.add(annot);
		}
		return tokens;
	}

	private List<GateAnnotation> getSpaceTokens() {
		List<GateAnnotation> spaceTokens = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet spaceTokenSet = defaultAnnSet.get("SpaceToken");
		for (Annotation annotation : spaceTokenSet) {
			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));

			spaceTokens.add(annot);
		}
		return spaceTokens;
	}

	private List<GateAnnotation> getSentences() {
		List<GateAnnotation> sentences = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet sentencesSet = defaultAnnSet.get("Sentence");
		for (Annotation annotation : sentencesSet) {
			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));

			sentences.add(annot);
		}
		return sentences;
	}

	private List<GateAnnotation> getVPChunks() {
		List<GateAnnotation> vpChunks = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet VGSet = defaultAnnSet.get("VG");
		for (Annotation annotation : VGSet) {
			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));

			vpChunks.add(annot);
		}
		return vpChunks;
	}

	private List<GateAnnotation> getNounChunks() {

		List<GateAnnotation> nounChunks = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet nounChunksSet = defaultAnnSet.get("NounChunk");
		for (Annotation annotation : nounChunksSet) {
			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));

			nounChunks.add(annot);
		}
		return nounChunks;
	}

	private List<GateAnnotation> getSplits() {

		List<GateAnnotation> splits = new ArrayList<GateAnnotation>();
		Document doc = corpus.get(0);
		AnnotationSet defaultAnnSet = doc.getAnnotations();
		AnnotationSet splitSet = defaultAnnSet.get("Split");
		for (Annotation annotation : splitSet) {
			GateAnnotation annot = new GateAnnotation(annotation.getId(),
					annotation.getStartNode(), annotation.getEndNode(),
					annotation.getType(), annotation.getFeatures());
			annot.setAnnotatedText(gate.Utils.stringFor(doc, annotation));

			splits.add(annot);
		}
		return splits;
	}

	private String prepareResultJson() {
		Output result = new Output();
		result.setToken(getTokens());
		result.setSpaceToken(getSpaceTokens());
		result.setSentence(getSentences());
		result.setSplit(getSplits());

		if (entitiesToGenerate.contains(NLPEntities.VP_CHUNKER)) {
			result.setVG(getVPChunks());
		}

		if (entitiesToGenerate.contains(NLPEntities.NP_CHUNKER)) {
			result.setNounChunk(getNounChunks());
		}

		String resultJson = Utility.objectToJson(result);
		return resultJson;
	}

	public void close() {
		Factory.deleteResource(corpus);
		Factory.deleteResource(applicationPipeline);
	}

}
