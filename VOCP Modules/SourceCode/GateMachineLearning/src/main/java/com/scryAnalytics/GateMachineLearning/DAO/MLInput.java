package com.scryAnalytics.GateMachineLearning.DAO;

import java.util.List;

public class MLInput {

	// GenericNLP Annotations
	private List<GateAnnotation> Token;
	private List<GateAnnotation> SpaceToken;
	private List<GateAnnotation> Sentence;
	private List<GateAnnotation> VG;
	private List<GateAnnotation> NounChunk;
	private List<GateAnnotation> Split;

	// NER Annotations
	private List<GateAnnotation> Drug;
	private List<GateAnnotation> SideEffect;
	private List<GateAnnotation> AltTherapy;
	private List<GateAnnotation> AltDrug;
	private List<GateAnnotation> Regimen;

	// Features Annotations
	private List<GateAnnotation> SegmentInstance;
	private List<GateAnnotation> SegmentClass;

	public List<GateAnnotation> getToken() {
		return Token;
	}

	public void setToken(List<GateAnnotation> token) {
		Token = token;
	}

	public List<GateAnnotation> getSpaceToken() {
		return SpaceToken;
	}

	public void setSpaceToken(List<GateAnnotation> spaceToken) {
		SpaceToken = spaceToken;
	}

	public List<GateAnnotation> getSentence() {
		return Sentence;
	}

	public void setSentence(List<GateAnnotation> sentence) {
		Sentence = sentence;
	}

	public List<GateAnnotation> getVG() {
		return VG;
	}

	public void setVG(List<GateAnnotation> vG) {
		VG = vG;
	}

	public List<GateAnnotation> getNounChunk() {
		return NounChunk;
	}

	public void setNounChunk(List<GateAnnotation> nounChunk) {
		NounChunk = nounChunk;
	}

	public List<GateAnnotation> getSplit() {
		return Split;
	}

	public void setSplit(List<GateAnnotation> split) {
		Split = split;
	}

	public List<GateAnnotation> getDrug() {
		return Drug;
	}

	public void setDrug(List<GateAnnotation> drug) {
		Drug = drug;
	}

	public List<GateAnnotation> getSideEffect() {
		return SideEffect;
	}

	public void setSideEffect(List<GateAnnotation> sideEffect) {
		SideEffect = sideEffect;
	}

	public List<GateAnnotation> getAltTherapy() {
		return AltTherapy;
	}

	public void setAltTherapy(List<GateAnnotation> altTherapy) {
		AltTherapy = altTherapy;
	}

	public List<GateAnnotation> getAltDrug() {
		return AltDrug;
	}

	public void setAltDrug(List<GateAnnotation> altDrug) {
		AltDrug = altDrug;
	}

	public List<GateAnnotation> getRegimen() {
		return Regimen;
	}

	public void setRegimen(List<GateAnnotation> regimen) {
		Regimen = regimen;
	}

	public List<GateAnnotation> getSegmentInstance() {
		return SegmentInstance;
	}

	public void setSegmentInstance(List<GateAnnotation> segmentInstance) {
		SegmentInstance = segmentInstance;
	}

	public List<GateAnnotation> getSegmentClass() {
		return SegmentClass;
	}

	public void setSegmentClass(List<GateAnnotation> segmentClass) {
		SegmentClass = segmentClass;
	}

}
