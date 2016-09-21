package com.scryAnalytics.GateTrainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

public class HbaseDriver {

	static Logger log = Logger.getLogger(HbaseDriver.class);
	 private static final String UTF8_ENCODING = "UTF-8";
	private static final Charset UTF8_CHARSET = Charset.forName(UTF8_ENCODING);
	Connection connection;
	Table annotTable;

	public HbaseDriver(Configuration conf, String annotTableName) {

		try {
			connection = ConnectionFactory.createConnection(conf);
			annotTable = connection.getTable(TableName.valueOf(Bytes
					.toBytes(annotTableName)));

		} catch (IOException e) {
			log.error("Hbase Connection Error !!!!");
		}

	}

	public ResultScanner getAnnotTableScanner(String annotflag) {
		Scan scan = new Scan();
		List<Filter> filters = new ArrayList<Filter>();
		SingleColumnValueFilter processedFilter = new SingleColumnValueFilter(
				Bytes.toBytes("marker"), Bytes.toBytes(annotflag),
				CompareFilter.CompareOp.NOT_EQUAL, new BinaryComparator(
						Bytes.toBytes("test")));
		processedFilter.setFilterIfMissing(false);
		filters.add(processedFilter);

		FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL,
				filters);
		scan.setFilter(fl);

		ResultScanner ss = null;
		try {
			ss = annotTable.getScanner(scan);
		} catch (IOException e) {
			log.error("Error Scanning Message table");
		}

		return ss;

	}

	public void updateAnnotTable(String rowkey, String flagColumn,
			String flagValue) {

		try {
			Put put = new Put(Bytes.toBytes(rowkey));
			put.addColumn(Bytes.toBytes("relations"),
					Bytes.toBytes(flagColumn), Bytes.toBytes(flagValue));
			annotTable.put(put);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public void close() {
		try {
			annotTable.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
