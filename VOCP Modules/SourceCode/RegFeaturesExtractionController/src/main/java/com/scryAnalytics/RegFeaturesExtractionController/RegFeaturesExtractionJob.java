package com.scryAnalytics.RegFeaturesExtractionController;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.MultiTableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.scryAnalytics.RegFeaturesExtractionController.Configuration.VOCPConstants;
import com.scryAnalytics.RegFeaturesExtractionController.Configuration.VocpConfiguration;

public class RegFeaturesExtractionJob extends Configured implements Tool {

	static Logger log = Logger.getLogger(RegFeaturesExtractionJob.class);

	public RegFeaturesExtractionJob() {
	}

	public int segmentsFeatureGeneration(String batchIdInput) throws Exception {

		Long batchId = null;
		if (!batchIdInput.equalsIgnoreCase("-all")) {
			batchId = Long.parseLong(batchIdInput);
		}

		// Gate Related Stuff
		URI[] appURI = new URI[1];
		Path localGateApp = new Path(getConf().get(
				VOCPConstants.GATE_PLUGIN_ARCHIVE));
		Path hdfsGateApp = new Path(getConf().get("fs.defaultFS")
				+ "/tmp/gate-" + getConf().get(VOCPConstants.UUID_KEY)
				+ "-GatePlugins.zip");
		log.info(hdfsGateApp.toUri());
		appURI[0] = hdfsGateApp.toUri();

		FileSystem fs = FileSystem.get(getConf());
		log.info(fs.getHomeDirectory().toString());
		fs.copyFromLocalFile(localGateApp, hdfsGateApp);

		Job job = Job.getInstance(getConf(), "RegimenFeaturesExtraction");
		String cacheURI = hdfsGateApp.toString() + "#vocpGate";
		job.addCacheArchive(new URI(cacheURI));
		job.setJarByClass(RegFeaturesExtractionJob.class);

		List<String> inputCfs = Arrays.asList(getConf().get(
				VOCPConstants.INPUTCOLUMNFAMILIES).split(","));

		Scan scan = new Scan();
		for (String cf : inputCfs) {
			scan.addFamily(Bytes.toBytes(cf));
		}

		List<Filter> filters = new ArrayList<Filter>();

		SingleColumnValueFilter flagFilter = new SingleColumnValueFilter(
				Bytes.toBytes(getConf().get(VOCPConstants.FLAGCOLUMNFAMILY)),
				Bytes.toBytes(getConf().get(VOCPConstants.FLAGCOLUMN)),
				CompareFilter.CompareOp.EQUAL, new BinaryComparator(
						Bytes.toBytes("0")));
		flagFilter.setFilterIfMissing(false);
		filters.add(flagFilter);

		SingleColumnValueFilter processedFilter = new SingleColumnValueFilter(
				Bytes.toBytes(getConf().get(VOCPConstants.FLAGCOLUMNFAMILY)),
				Bytes.toBytes("is_processed"), CompareFilter.CompareOp.EQUAL,
				new BinaryComparator(Bytes.toBytes("1")));
		processedFilter.setFilterIfMissing(true);
		filters.add(processedFilter);

		if (batchId != null) {
			SingleColumnValueFilter batchFilter = new SingleColumnValueFilter(
					Bytes.toBytes("f"), Bytes.toBytes("batchId"),
					CompareFilter.CompareOp.EQUAL, new BinaryComparator(
							Bytes.toBytes(batchId)));
			batchFilter.setFilterIfMissing(false);
			filters.add(batchFilter);
		}

		FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL,
				filters);
		scan.setFilter(fl);
		scan.setCaching(100000);

		TableMapReduceUtil.initTableMapperJob(
				Bytes.toBytes(getConf().get(VOCPConstants.INPUTTABLE)), scan,
				RegFeaturesExtractionMapper.class,
				ImmutableBytesWritable.class, Text.class, job);

		job.setReducerClass(RegFeaturesExtractionReducer.class);
		job.setOutputFormatClass(MultiTableOutputFormat.class);
		TableMapReduceUtil.addDependencyJars(job);
		TableMapReduceUtil.addDependencyJars(job.getConfiguration());

		// TableMapReduceUtil.initTableReducerJob(
		// getConf().get(VOCPConstants.OUTPUTTABLE),
		// FeatureExtractionReducer.class, job);

		boolean success = job.waitForCompletion(true);

		FileSystem.get(job.getConfiguration()).deleteOnExit(hdfsGateApp);

		return success ? 0 : -1;

	}

	public int run(String[] args) throws Exception {
		String batchString = "";

		String usage = "Usage: RegFeatureExtractionController"
				+ " -inputTable tableName -outputTable tableName"
				+ " -batchId batchId / -all";

		if (args.length == 0) {
			System.err.println(usage);
			return -1;
		}

		for (int i = 0; i < args.length; i++) {

			if ("-inputTable".equals(args[i])) {
				getConf().set(VOCPConstants.INPUTTABLE, args[++i]);
			} else if ("-outputTable".equals(args[i])) {
				getConf().set(VOCPConstants.OUTPUTTABLE, args[++i]);
			} else if ("-batchId".equals(args[i])) {
				batchString = args[++i];
			} else if ("-all".equals(args[i])) {
				batchString = "-all";
			} else {
				throw new IllegalArgumentException("arg " + args[i]
						+ " not recognized");
			}

		}

		int result = segmentsFeatureGeneration(batchString);

		return result;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(VocpConfiguration.create(),
				new RegFeaturesExtractionJob(), args);
		System.exit(res);
	}

}
