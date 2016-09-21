package com.scryAnalytics.GateTrainer.DAO;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import gate.FeatureMap;
import gate.Node;
import gate.annotation.AnnotationImpl;

public class GateAnnotation extends AnnotationImpl {

	public GateAnnotation(Integer id, Node start, Node end, String type,
			FeatureMap features) {
		super(id, start, end, type, features);
		// TODO Auto-generated constructor stub
	}

	public GateAnnotation() {
		super(null, null, null, null, null);

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty
	private Integer id;
	@JsonProperty
	private String type;
	@JsonProperty
	@JsonDeserialize(using = FeatureMapDeserializer.class)
	FeatureMap features;
	@JsonProperty
	@JsonDeserialize(using = NodeDeserializer.class)
	Node startNode;
	@JsonProperty
	@JsonDeserialize(using = NodeDeserializer.class)
	Node endNode;
	@JsonProperty
	private String annotatedText;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public FeatureMap getFeatures() {
		return features;
	}

	public void setFeatures(FeatureMap features) {
		this.features = features;
	}

	public Node getStartNode() {
		return startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	public Integer getId() {

		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAnnotatedText() {
		return annotatedText;
	}

	public void setAnnotatedText(String annotatedText) {
		this.annotatedText = annotatedText;
	}

}
