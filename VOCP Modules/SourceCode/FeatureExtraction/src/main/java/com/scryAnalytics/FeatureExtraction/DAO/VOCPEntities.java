package com.scryAnalytics.FeatureExtraction.DAO;

public enum VOCPEntities {

	DRUG("DRUG"), REGIMEN("REG"), ALTDRUG("ALT_DRUG"), ALTTHERAPY("ALT_THERAPY");

	private String vocpEntity;

	private VOCPEntities(String vocpEntity) {
		this.vocpEntity = vocpEntity;
	}

	public String getVocpEntity() {
		return vocpEntity;
	}

}
