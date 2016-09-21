package com.scryAnalytics.RegFeaturesExtractionController;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import com.scryAnalytics.FeatureExtraction.SegmentsFeatureExtraction;
import com.scryAnalytics.FeatureExtraction.DAO.VOCPEntities;
import com.scryAnalytics.RegFeaturesExtractionController.DAO.DocumentEntitiesDAO;
import com.scryAnalytics.RegFeaturesExtractionController.DAO.NLPEntitiesDAO;
import com.scryAnalytics.RegFeaturesExtractionController.DAO.SegmentFeaturesDAO;

public class RegFeaturesExtractionMapper extends
		TableMapper<ImmutableBytesWritable, Text> {

	static Logger log = Logger.getLogger(RegFeaturesExtractionMapper.class);
	static private SegmentsFeatureExtraction featureExtraction;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {

		if (null == featureExtraction) {
			log.info("In setup method");
			File vocpGateDir = new File("vocpGate");
			String[] children = vocpGateDir.list();
			for (String string : children) {
				System.out.println(string);
			}

			try {

				featureExtraction = new SegmentsFeatureExtraction(
						vocpGateDir.getAbsolutePath(), VOCPEntities.REGIMEN);

				// log.info(featureExtraction.toString());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
	}

	public void map(ImmutableBytesWritable row, Result result, Context context)
			throws InterruptedException, IOException {
		log.info("In Map method");
		ImmutableBytesWritable rowkey = new ImmutableBytesWritable(row.get());

		String featuresJson = "";

		String message = Bytes.toString(result.getValue(Bytes.toBytes("p"),
				Bytes.toBytes("message")));
		String systemId = Bytes.toString(result.getValue(Bytes.toBytes("p"),
				Bytes.toBytes("system_id")));

		NLPEntitiesDAO nlpEntities = new NLPEntitiesDAO();
		nlpEntities.setToken(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("gen"), Bytes.toBytes("token")))));
		nlpEntities.setSpaceToken(Utility.jsonToAnnotations(Bytes
				.toString(result.getValue(Bytes.toBytes("gen"),
						Bytes.toBytes("spaceToken")))));
		nlpEntities.setSentence(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("gen"), Bytes.toBytes("sentence")))));
		nlpEntities.setVG(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("gen"), Bytes.toBytes("verbGroup")))));
		nlpEntities.setSplit(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("gen"), Bytes.toBytes("split")))));
		nlpEntities.setNounChunk(Utility.jsonToAnnotations(Bytes
				.toString(result.getValue(Bytes.toBytes("gen"),
						Bytes.toBytes("nounChunk")))));

		nlpEntities.setDrugs(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("ner"), Bytes.toBytes("drug")))));
		nlpEntities.setRegimen(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("ner"), Bytes.toBytes("regimen")))));
		nlpEntities.setSideEffects(Utility.jsonToAnnotations(Bytes
				.toString(result.getValue(Bytes.toBytes("ner"),
						Bytes.toBytes("sideEffect")))));
		nlpEntities.setALT_DRUG(Utility.jsonToAnnotations(Bytes.toString(result
				.getValue(Bytes.toBytes("ner"), Bytes.toBytes("altDrug")))));
		nlpEntities.setALT_THERAPY(Utility.jsonToAnnotations(Bytes
				.toString(result.getValue(Bytes.toBytes("ner"),
						Bytes.toBytes("altTherapy")))));

		String inputAnnotations = Utility.objectToJson(nlpEntities);

		try {
			featuresJson = featureExtraction.generateFeatures(message,
					inputAnnotations);
			SegmentFeaturesDAO segmentFeatures = Utility
					.jsonToSegmentFeatures(featuresJson);

			DocumentEntitiesDAO documentEntities = new DocumentEntitiesDAO();
			documentEntities.setSystemId(systemId);
			documentEntities.setToken(nlpEntities.getToken());
			documentEntities.setSpaceToken(nlpEntities.getSpaceToken());
			documentEntities.setSentence(nlpEntities.getSentence());
			documentEntities.setVG(nlpEntities.getVG());
			documentEntities.setNounChunk(nlpEntities.getNounChunk());
			documentEntities.setSplit(nlpEntities.getSplit());
			documentEntities.setDRUG(nlpEntities.getDrugs());
			documentEntities.setSE(nlpEntities.getSideEffects());
			documentEntities.setREG(nlpEntities.getRegimen());
			documentEntities.setALT_DRUG(nlpEntities.getALT_DRUG());
			documentEntities.setALT_THERAPY(nlpEntities.getALT_THERAPY());

			documentEntities.setSegment(segmentFeatures.getSegment());
			documentEntities.setSegmentClass(segmentFeatures.getSegmentClass());
			documentEntities.setSegmentInstance(segmentFeatures
					.getSegmentInstance());

			String documentEntitiesJson = Utility
					.objectToJson(documentEntities);

			context.write(rowkey, new Text(documentEntitiesJson));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	protected void cleanup(Context context) throws java.io.IOException,
			InterruptedException {
		featureExtraction.close();

	}

}
