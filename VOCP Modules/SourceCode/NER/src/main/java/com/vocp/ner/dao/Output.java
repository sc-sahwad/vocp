package com.vocp.ner.dao;

import java.util.List;

public class Output {

	private List<GateAnnotation> DRUG;
	private List<GateAnnotation> SE;
	private List<GateAnnotation> ALT_THERAPY;
	private List<GateAnnotation> ALT_DRUG;
	private List<GateAnnotation> REG;

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

	@Override
	public String toString() {
		return "Output [DRUG=" + DRUG + ", SE=" + SE + ", ALT_THERAPY="
				+ ALT_THERAPY + ", ALT_DRUG=" + ALT_DRUG + ", REG=" + REG + "]";
	}

}
