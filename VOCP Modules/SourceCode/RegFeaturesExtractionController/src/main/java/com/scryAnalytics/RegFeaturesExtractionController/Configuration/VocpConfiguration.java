package com.scryAnalytics.RegFeaturesExtractionController.Configuration;

import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;

public class VocpConfiguration {

	private VocpConfiguration() {
	} // singleton

	/*
	 * Configuration.hashCode() doesn't return values that correspond to a
	 * unique set of parameters. This is a workaround so that we can track
	 * instances of Configuration created by VOCP.
	 */
	private static void setUUID(Configuration conf) {
		UUID uuid = UUID.randomUUID();
		conf.set(VOCPConstants.UUID_KEY, uuid.toString());
	}

	/**
	 * Retrieve a VOCP UUID of this configuration object, or null if the
	 * configuration was created elsewhere.
	 * 
	 * @param conf
	 *            configuration instance
	 * @return uuid or null
	 */
	public static String getUUID(Configuration conf) {
		return conf.get(VOCPConstants.UUID_KEY);
	}

	/**
	 * Create a {@link Configuration} for VOCP. This will load the standard VOCP
	 * resources, <code>vocp-props.xml</code> overrides.
	 */
	public static Configuration create() {
		Configuration conf = new Configuration();
		setUUID(conf);
		addVocpResources(conf);

		String hadoopConfigPath = conf.get(VOCPConstants.HADOOPCONFIG);
		conf.addResource(new Path(hadoopConfigPath + "core-site.xml"));
		conf.addResource(new Path(hadoopConfigPath + "hdfs-site.xml"));
		conf.addResource(new Path(hadoopConfigPath + "mapred-site.xml"));
		conf.addResource(new Path(hadoopConfigPath + "yarn-site.xml"));

		Configuration finalConfig = new Configuration();
		finalConfig = HBaseConfiguration.create(conf);

		// finalConfig.set("mapred.framework.name", "local");
		// System.out.println(finalConfig.get("mapreduce.framework.name"));
		// finalConfig.set("mapreduce.textoutputformat.separator", ",");

		return finalConfig;
	}

	/**
	 * Create a {@link Configuration} from supplied properties.
	 * 
	 * @param addVocpResources
	 *            if true, then first <code>vocp-props.xml</code> will be loaded
	 *            prior to applying the properties. Otherwise these resources
	 *            won't be used.
	 * @param vocpProperties
	 *            a set of properties to define (or override)
	 */
	public static Configuration create(boolean addVocpResources,
			Properties vocpProperties) {
		Configuration conf = new Configuration();
		setUUID(conf);
		if (addVocpResources) {
			addVocpResources(conf);
		}
		for (Entry<Object, Object> e : vocpProperties.entrySet()) {
			conf.set(e.getKey().toString(), e.getValue().toString());
		}
		return conf;
	}

	/**
	 * Add the standard Vocp resources to {@link Configuration}.
	 * 
	 * @param conf
	 *            Configuration object to which configuration is to be added.
	 */
	private static Configuration addVocpResources(Configuration conf) {
		conf.addResource("vocp-props.xml");
		conf.reloadConfiguration();
		return conf;
	}

}
