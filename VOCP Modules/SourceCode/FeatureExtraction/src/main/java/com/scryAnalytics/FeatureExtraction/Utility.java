package com.scryAnalytics.FeatureExtraction;

import java.io.IOException;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.scryAnalytics.FeatureExtraction.DAO.Input;

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

	public static Input jsonToInput(String json) {

		Input input = new Input();

		ObjectMapper mapper = new ObjectMapper();
		try {
			input = mapper.readValue(json, Input.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

	/*
	 * public static Output jsonToOutput(String json) { Output output = new
	 * Output();
	 * 
	 * ObjectMapper mapper = new ObjectMapper(); try { output =
	 * mapper.readValue(json, Output.class); } catch (JsonMappingException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
	 * return output; }
	 */
}
