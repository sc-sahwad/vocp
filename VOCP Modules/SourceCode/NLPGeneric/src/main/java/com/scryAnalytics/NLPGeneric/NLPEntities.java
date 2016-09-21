package com.scryAnalytics.NLPGeneric;

public enum NLPEntities {

	POS_TAGGER("pos_tagger"), NP_CHUNKER("np_chunker"), VP_CHUNKER("vp_chunker");

	private String nlpEntity;

	private NLPEntities(String nlpEntity) {
		this.nlpEntity = nlpEntity;
	}

	public String getNlpEntity() {
		return nlpEntity;
	}

}
