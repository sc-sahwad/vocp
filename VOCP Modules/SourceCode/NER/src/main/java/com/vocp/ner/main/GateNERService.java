package com.vocp.ner.main;

import java.util.List;

public class GateNERService {

	public String getEntity(String message, String entityName, String input)
			throws Exception {

		GateNERImpl gateGenericNLP = new GateNERImpl(null, entityName);
		return gateGenericNLP.generateNER(message, input);

	}

	public String getEntities(String message, List<String> entityNames,
			String input) throws Exception {

		GateNERImpl gateGenericNLP = new GateNERImpl(null, entityNames);
		return gateGenericNLP.generateNER(message, input);

	}

}
