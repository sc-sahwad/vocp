package com.scryAnalytics.RegFeaturesExtractionController;

import gate.FeatureMap;

import java.io.IOException;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import com.scryAnalytics.RegFeaturesExtractionController.Configuration.VOCPConstants;
import com.scryAnalytics.RegFeaturesExtractionController.DAO.DocumentEntitiesDAO;
import com.scryAnalytics.RegFeaturesExtractionController.DAO.GateAnnotation;

public class RegFeaturesExtractionReducer extends
		TableReducer<ImmutableBytesWritable, Text, ImmutableBytesWritable> {

	static Logger log = Logger.getLogger(RegFeaturesExtractionReducer.class);
	private ImmutableBytesWritable messageTable;
	private ImmutableBytesWritable segmentsTable;
	private Configuration conf;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		conf = context.getConfiguration();

		messageTable = new ImmutableBytesWritable(Bytes.toBytes(conf
				.get(VOCPConstants.INPUTTABLE)));
		segmentsTable = new ImmutableBytesWritable(Bytes.toBytes(conf
				.get(VOCPConstants.OUTPUTTABLE)));

	}

	public void reduce(ImmutableBytesWritable key, Iterable<Text> values,
			Context context) throws IOException, InterruptedException {

		for (Text jsonText : values) {
			String documentEntitiesJson = jsonText.toString();
			DocumentEntitiesDAO documentEntities = Utility
					.jsonToDocumentEntities(documentEntitiesJson);

			List<GateAnnotation> segments = documentEntities.getSegment();
			int count = 0;
			if (segments != null) {
				for (GateAnnotation segment : segments) {

					String keyString = documentEntities.getSystemId() + "#"
							+ Integer.toString(count++);

					ImmutableBytesWritable outputKey = new ImmutableBytesWritable(
							keyString.getBytes());

					Put put = new Put(outputKey.get());

					FeatureMap features = segment.getFeatures();

					put.addColumn(Bytes.toBytes("seg"),
							Bytes.toBytes("system_id"),
							Bytes.toBytes(documentEntities.getSystemId()));
					put.addColumn(Bytes.toBytes("seg"),
							Bytes.toBytes("segmentText"),
							Bytes.toBytes(segment.getAnnotatedText()));

					put.addColumn(Bytes.toBytes("seg"),
							Bytes.toBytes("regimen"),
							Bytes.toBytes((String) features.get("regimen")));
					put.addColumn(Bytes.toBytes("seg"),
							Bytes.toBytes("sideEffect"),
							Bytes.toBytes((String) features.get("sideEffect")));

					Double confidenceScore = 0.0;
					if (features.get("SegmentType").toString()
							.equals("SentenceBased")) {
						confidenceScore = 3.0;
					} else if (features.get("SegmentType").toString()
							.equals("WindowBased")) {
						if (features.get("WindowsType").toString()
								.equals("ConsiderationSet")) {
							confidenceScore = 2.0;

						} else {
							confidenceScore = 1.0;
						}
					}

					put.addColumn(Bytes.toBytes("seg"),
							Bytes.toBytes("confidence_score"),
							Bytes.toBytes(confidenceScore));
					put.addColumn(Bytes.toBytes("seg"),
							Bytes.toBytes("relationType"),
							Bytes.toBytes("Valid"));

					context.write(segmentsTable, put);

				}

				Put messagePut = new Put(key.get());
				messagePut
						.addColumn(Bytes.toBytes(conf
								.get(VOCPConstants.FLAGCOLUMNFAMILY)), Bytes
								.toBytes(conf.get(VOCPConstants.FLAGCOLUMN)),
								Bytes.toBytes("2"));

				context.write(messageTable, messagePut);

			}
		}

	}
}
