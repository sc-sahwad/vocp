package com.vocp.ner.dao;

import java.util.List;

public class Input {

	private List<GateAnnotation> Token;
	private List<GateAnnotation> SpaceToken;
	private List<GateAnnotation> Sentence;
	private List<GateAnnotation> VG;
	private List<GateAnnotation> NounChunk;
	private List<GateAnnotation> Split;

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

	@Override
	public String toString() {
		return "Input [Token=" + Token + ", SpaceToken=" + SpaceToken
				+ ", Sentence=" + Sentence + ", VG=" + VG + ", NounChunk="
				+ NounChunk + ", Split=" + Split + "]";
	}

}
