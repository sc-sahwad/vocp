package com.vocp.ner.dao;

import gate.Factory;
import gate.FeatureMap;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class FeatureMapDeserializer extends JsonDeserializer<FeatureMap> {

	@SuppressWarnings("unchecked")
	@Override
	public FeatureMap deserialize(JsonParser jsonParser,
			DeserializationContext deserializationContext) throws IOException,
			JsonProcessingException {
		Map<String, String> features = jsonParser.readValueAs(Map.class);
		FeatureMap featureMap = Factory.newFeatureMap();
		featureMap.putAll(features);

		return featureMap;
	}

}
