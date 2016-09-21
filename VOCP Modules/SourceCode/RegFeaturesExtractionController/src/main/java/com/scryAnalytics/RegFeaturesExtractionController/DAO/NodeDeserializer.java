package com.scryAnalytics.RegFeaturesExtractionController.DAO;

import java.io.IOException;

import gate.Node;
import gate.annotation.NodeImpl;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class NodeDeserializer extends JsonDeserializer<Node> {

	@Override
	public Node deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		ObjectCodec oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);

		Node gateNode = new NodeImpl(node.get("id").asInt(), node.get("offset")
				.asLong());

		return gateNode;
	}

}
