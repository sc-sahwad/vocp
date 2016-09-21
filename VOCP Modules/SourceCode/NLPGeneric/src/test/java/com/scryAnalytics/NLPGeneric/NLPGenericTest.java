package com.scryAnalytics.NLPGeneric;

import gate.util.GateException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.scryAnalytics.NLPGeneric.DAO.Output;

public class NLPGenericTest {

	public static void main(String[] args) {

		String pluginHome = "/Applications/GATE_Developer_8.1/plugins";
		List<NLPEntities> requiredNLPEntities = new ArrayList<NLPEntities>();
		requiredNLPEntities.add(NLPEntities.POS_TAGGER);
		requiredNLPEntities.add(NLPEntities.VP_CHUNKER);
		requiredNLPEntities.add(NLPEntities.NP_CHUNKER);

		try {
			GateGenericNLP nlpModule = new GateGenericNLP(pluginHome,
					requiredNLPEntities);
			String message = "Adriyamycin, Cytoxan and taxol causes bone pain . Yoga helps in headache while claritin reduces pain";

			String resultJson = nlpModule.generateNLPEntities(message);
			System.out.println(resultJson);
			Output output = Utility.jsonToOutput(resultJson);

			System.out.println(Utility.objectToJson(output));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
