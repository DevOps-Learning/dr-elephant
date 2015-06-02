package com.linkedin.drelephant.spark.heuristics;

import com.linkedin.drelephant.analysis.Severity;
import com.linkedin.drelephant.spark.MockSparkApplicationData;
import com.linkedin.drelephant.spark.SparkApplicationData;
import java.util.Properties;
import junit.framework.TestCase;

import static com.linkedin.drelephant.spark.heuristics.BestPropertiesConventionHeuristic.SPARK_DRIVER_MEMORY;
import static com.linkedin.drelephant.spark.heuristics.BestPropertiesConventionHeuristic.SPARK_EXECUTOR_CORES;
import static com.linkedin.drelephant.spark.heuristics.BestPropertiesConventionHeuristic.SPARK_SERIALIZER;
import static com.linkedin.drelephant.spark.heuristics.BestPropertiesConventionHeuristic.SPARK_SHUFFLE_MANAGER;


/**
 * This class test the BestPropertiesConventionHeuristic
 *
 */
public class BestPropertiesConventionHeuristicTest extends TestCase {
  public void testPropertiesCheck() {
    assertEquals(analyzeJob(getDefaultGoodProperteis()), Severity.NONE);

    assertEquals(Severity.MODERATE, analyzeJob(getPropertiesAndOverideOne(SPARK_DRIVER_MEMORY, "7G")));
    assertEquals(Severity.CRITICAL, analyzeJob(getPropertiesAndOverideOne(SPARK_DRIVER_MEMORY, "8G")));
    assertEquals(Severity.CRITICAL, analyzeJob(getPropertiesAndOverideOne(SPARK_DRIVER_MEMORY, "9G")));

    assertEquals(Severity.NONE, analyzeJob(getPropertiesAndOverideOne(SPARK_EXECUTOR_CORES, "1")));
    assertEquals(Severity.NONE, analyzeJob(getPropertiesAndOverideOne(SPARK_EXECUTOR_CORES, "2")));
    assertEquals(Severity.CRITICAL, analyzeJob(getPropertiesAndOverideOne(SPARK_EXECUTOR_CORES, "4")));


    assertEquals(Severity.MODERATE, analyzeJob(getPropertiesAndOverideOne(SPARK_SERIALIZER, "foo")));
    assertEquals(Severity.MODERATE, analyzeJob(getPropertiesAndOverideOne(SPARK_SHUFFLE_MANAGER, "hash")));
  }

  public void testNullSettings() {
    assertEquals(Severity.MODERATE, analyzeJob(getPropertiesAndRemove(SPARK_SERIALIZER)));
    assertEquals(Severity.NONE, analyzeJob(getPropertiesAndRemove(SPARK_SHUFFLE_MANAGER)));
    assertEquals(Severity.NONE, analyzeJob(getPropertiesAndRemove(SPARK_EXECUTOR_CORES)));
  }

  private static Properties getDefaultGoodProperteis() {
    Properties properties = new Properties();
    properties.put(SPARK_DRIVER_MEMORY, "1G");
    properties.put(SPARK_EXECUTOR_CORES, "1");
    properties.put(SPARK_SERIALIZER, "org.apache.spark.serializer.KryoSerializer");
    properties.put(SPARK_SHUFFLE_MANAGER, "sort");

    return properties;
  }

  private static Properties getPropertiesAndOverideOne(String key, String value) {
    Properties properties = getDefaultGoodProperteis();
    properties.put(key, value);
    return properties;
  }

  private static Properties getPropertiesAndRemove(String key) {
    Properties properties = getDefaultGoodProperteis();
    properties.remove(key);
    return properties;
  }

  private Severity analyzeJob(Properties sparkProperties) {
    SparkApplicationData data = new MockSparkApplicationData();
    for (String key : sparkProperties.stringPropertyNames()) {
      data.getEnvironmentData().addSparkProperty(key, sparkProperties.getProperty(key));
    }
    return new BestPropertiesConventionHeuristic().apply(data).getSeverity();
  }
}