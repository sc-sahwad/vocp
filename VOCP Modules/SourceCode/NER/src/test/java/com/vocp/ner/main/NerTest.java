package com.vocp.ner.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NerTest {

	public static void main(String[] args) {
		String message = "ACT causes bone pain";
		String input = "{\"vg\":[],\"token\":[{\"features\":{\"string\":\"ACT\",\"length\":\"3\",\"orth\":\"allCaps\",\"kind\":\"word\",\"category\":\"NNP\"},\"id\":0,\"type\":\"Token\",\"startNode\":{\"id\":0,\"offset\":0},\"endNode\":{\"id\":1,\"offset\":3},\"annotatedText\":\"ACT\"},{\"features\":{\"string\":\"causes\",\"length\":\"6\",\"orth\":\"lowercase\",\"kind\":\"word\",\"category\":\"NNS\"},\"id\":2,\"type\":\"Token\",\"startNode\":{\"id\":2,\"offset\":4},\"endNode\":{\"id\":3,\"offset\":10},\"annotatedText\":\"causes\"},{\"features\":{\"string\":\"bone\",\"length\":\"4\",\"orth\":\"lowercase\",\"kind\":\"word\",\"category\":\"NN\"},\"id\":4,\"type\":\"Token\",\"startNode\":{\"id\":4,\"offset\":11},\"endNode\":{\"id\":5,\"offset\":15},\"annotatedText\":\"bone\"},{\"features\":{\"string\":\"pain\",\"length\":\"4\",\"orth\":\"lowercase\",\"kind\":\"word\",\"category\":\"NN\"},\"id\":6,\"type\":\"Token\",\"startNode\":{\"id\":6,\"offset\":16},\"endNode\":{\"id\":7,\"offset\":20},\"annotatedText\":\"pain\"}],\"spaceToken\":[{\"features\":{\"string\":\" \",\"length\":\"1\",\"kind\":\"space\"},\"id\":1,\"type\":\"SpaceToken\",\"startNode\":{\"id\":1,\"offset\":3},\"endNode\":{\"id\":2,\"offset\":4},\"annotatedText\":\" \"},{\"features\":{\"string\":\" \",\"length\":\"1\",\"kind\":\"space\"},\"id\":3,\"type\":\"SpaceToken\",\"startNode\":{\"id\":3,\"offset\":10},\"endNode\":{\"id\":4,\"offset\":11},\"annotatedText\":\" \"},{\"features\":{\"string\":\" \",\"length\":\"1\",\"kind\":\"space\"},\"id\":5,\"type\":\"SpaceToken\",\"startNode\":{\"id\":5,\"offset\":15},\"endNode\":{\"id\":6,\"offset\":16},\"annotatedText\":\" \"}],\"sentence\":[{\"features\":{},\"id\":7,\"type\":\"Sentence\",\"startNode\":{\"id\":0,\"offset\":0},\"endNode\":{\"id\":7,\"offset\":20},\"annotatedText\":\"ACT causes bone pain\"}],\"nounChunk\":[{\"features\":{},\"id\":8,\"type\":\"NounChunk\",\"startNode\":{\"id\":0,\"offset\":0},\"endNode\":{\"id\":7,\"offset\":20},\"annotatedText\":\"ACT causes bone pain\"}],\"split\":[]}\r\n";

		File vocpGateDir = new File("/Users/ankit-scry/Desktop/plugins");

		List<String> requiredNEREntities = new ArrayList<String>();
		requiredNEREntities.add("DRUG");
		requiredNEREntities.add("SE");
		requiredNEREntities.add("REG");
		requiredNEREntities.add("ALT_THERAPY");
		requiredNEREntities.add("ALT_DRUG");
		String nerEntititiesJson = "";

		GateNERImpl gateNER;
		gateNER = new GateNERImpl(vocpGateDir.getAbsolutePath(),
				requiredNEREntities);
		try {
			nerEntititiesJson = gateNER.generateNER(message, input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(nerEntititiesJson);

	}
}
