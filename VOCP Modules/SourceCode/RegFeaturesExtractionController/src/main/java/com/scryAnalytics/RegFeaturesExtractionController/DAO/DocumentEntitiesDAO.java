package com.scryAnalytics.RegFeaturesExtractionController.DAO;

import java.util.List;

public class DocumentEntitiesDAO {

	private String systemId;

	private List<GateAnnotation> Token;
	private List<GateAnnotation> SpaceToken;
	private List<GateAnnotation> Sentence;
	private List<GateAnnotation> VG;
	private List<GateAnnotation> NounChunk;
	private List<GateAnnotation> Split;

	private List<GateAnnotation> DRUG;
	private List<GateAnnotation> SE;
	private List<GateAnnotation> ALT_THERAPY;
	private List<GateAnnotation> ALT_DRUG;
	private List<GateAnnotation> REG;

	private List<GateAnnotation> SegmentInstance;
	private List<GateAnnotation> SegmentClass;
	private List<GateAnnotation> Segment;

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

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

	public List<GateAnnotation> getDRUG() {
		return DRUG;
	}

	public void setDRUG(List<GateAnnotation> dRUG) {
		DRUG = dRUG;
	}

	public List<GateAnnotation> getSE() {
		return SE;
	}

	public void setSE(List<GateAnnotation> sE) {
		SE = sE;
	}

	public List<GateAnnotation> getALT_THERAPY() {
		return ALT_THERAPY;
	}

	public void setALT_THERAPY(List<GateAnnotation> aLT_THERAPY) {
		ALT_THERAPY = aLT_THERAPY;
	}

	public List<GateAnnotation> getALT_DRUG() {
		return ALT_DRUG;
	}

	public void setALT_DRUG(List<GateAnnotation> aLT_DRUG) {
		ALT_DRUG = aLT_DRUG;
	}

	public List<GateAnnotation> getREG() {
		return REG;
	}

	public void setREG(List<GateAnnotation> rEG) {
		REG = rEG;
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

	public List<GateAnnotation> getSegment() {
		return Segment;
	}

	public void setSegment(List<GateAnnotation> segment) {
		Segment = segment;
	}

}
