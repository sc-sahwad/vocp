package com.scryAnalytics.FeatureExtraction.DAO;

import gate.FeatureMap;
import gate.Node;
import gate.annotation.AnnotationImpl;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class GateAnnotation extends AnnotationImpl {

	public GateAnnotation(Integer id, Node start, Node end, String type,
			FeatureMap features) {
		super(id, start, end, type, features);
		this.features = features;
		this.endNode = end;
		this.startNode = start;
	}

	public GateAnnotation() {
		super(null, null, null, null, null);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty
	@JsonDeserialize(using = FeatureMapDeserializer.class)
	private FeatureMap features;
	@JsonProperty
	@JsonDeserialize(using = NodeDeserializer.class)
	private Node startNode;
	@JsonProperty
	@JsonDeserialize(using = NodeDeserializer.class)
	private Node endNode;
	@JsonProperty
	private String annotatedText;

	@Override
	public Node getStartNode() {
		return this.startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	@Override
	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	@Override
	public FeatureMap getFeatures() {
		return features;
	}

	public void setFeatures(FeatureMap features) {
		this.features = features;
	}

	public String getAnnotatedText() {
		return annotatedText;
	}

	public void setAnnotatedText(String annotatedText) {
		this.annotatedText = annotatedText;
	}

}
