package com.scryAnalytics.GateTrainer.DAO;

import java.util.List;

public class InputAnnotations {

	private List<GateAnnotation> entity1;
	private List<GateAnnotation> entity2;
	private List<GateAnnotation> relation;

	public List<GateAnnotation> getEntity1() {
		return entity1;
	}

	public void setEntity1(List<GateAnnotation> entity1) {
		this.entity1 = entity1;
	}

	public List<GateAnnotation> getEntity2() {
		return entity2;
	}

	public void setEntity2(List<GateAnnotation> entity2) {
		this.entity2 = entity2;
	}

	public List<GateAnnotation> getRelation() {
		return relation;
	}

	public void setRelation(List<GateAnnotation> relation) {
		this.relation = relation;
	}

}
