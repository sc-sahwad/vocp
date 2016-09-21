package com.scryAnalytics.RegFeaturesExtractionController.DAO;

import java.util.List;

public class NLPEntitiesDAO {

	private List<GateAnnotation> Token;
	private List<GateAnnotation> SpaceToken;
	private List<GateAnnotation> Sentence;
	private List<GateAnnotation> VG;
	private List<GateAnnotation> NounChunk;
	private List<GateAnnotation> Split;

	private List<GateAnnotation> drugs;
	private List<GateAnnotation> SideEffects;
	private List<GateAnnotation> ALT_THERAPY;
	private List<GateAnnotation> ALT_DRUG;
	private List<GateAnnotation> Regimen;

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

	public List<GateAnnotation> getDrugs() {
		return drugs;
	}

	public void setDrugs(List<GateAnnotation> drugs) {
		this.drugs = drugs;
	}

	public List<GateAnnotation> getSideEffects() {
		return SideEffects;
	}

	public void setSideEffects(List<GateAnnotation> sideEffects) {
		SideEffects = sideEffects;
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

	public List<GateAnnotation> getRegimen() {
		return Regimen;
	}

	public void setRegimen(List<GateAnnotation> regimen) {
		Regimen = regimen;
	}

}
