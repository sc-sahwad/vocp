package com.scryAnalytics.GateTrainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.scryAnalytics.GateTrainer.DAO.GateAnnotation;

public class Utility {

	public static String objectToJson(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		String outputString = "";
		try {
			outputString = mapper.writeValueAsString(obj);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outputString;
	}

	public static List<GateAnnotation> jsonToAnnotations(String json) {
		List<GateAnnotation> gateAnnotations = new ArrayList<GateAnnotation>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			gateAnnotations = mapper.readValue(json,
					new TypeReference<List<GateAnnotation>>() {
					});

		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gateAnnotations;
	}

}
