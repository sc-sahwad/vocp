package com.scryAnalytics.GateMachineLearning;

import java.io.IOException;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.scryAnalytics.GateMachineLearning.DAO.MLInput;

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

	public static MLInput jsonToMLInput(String json) {
		MLInput input = new MLInput();

		ObjectMapper mapper = new ObjectMapper();
		try {
			input = mapper.readValue(json, MLInput.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}

}
