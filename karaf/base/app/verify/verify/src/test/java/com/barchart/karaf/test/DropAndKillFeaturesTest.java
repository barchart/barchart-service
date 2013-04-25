package com.barchart.karaf.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * FIXME
 */
@Ignore
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class DropAndKillFeaturesTest extends TestAny {

	/**
	 * Test bundle name.
	 */
	final String BUNDLE = "com.barchart.test.barchart-karaf-base-test01";

	/**
	 * Test feature name.
	 */
	final String FEATURE = "barchart-karaf-base-test01-feature";

	@Configuration
	public Option[] config0() {
		return super.config();
	}

	@Configuration
	public Option[] config1() {
		return new Option[] {};
	}

	public void featureDropAndKill() throws Exception {

		final Path home = Paths.get(System.getProperty("karaf.home"));
		final Path test = home.resolve("../../../src/test/resources");

		log.info("\n\t home : {}\n\t test : {}", home, test);

		final Path source = test.resolve("case-01/feature.xml");

		final Path target = home.resolve("etc/feature.xml");

		logFeatures("init");
		assertFeatureNotInstalled(FEATURE);

		log.info("\n\t drop");
		Files.copy(source, target);

		for (int k = 0; k < 100; k++) {
			if (isFeatureInstalled(FEATURE)) {
				break;
			}
			Thread.sleep(100);
		}

		logFeatures("step 1");
		assertFeatureInstalled(FEATURE);
		assertBundleInstalled(BUNDLE);

		log.info("\n\t kill");
		Files.delete(target);

		for (int k = 0; k < 100; k++) {
			if (!isFeatureInstalled(FEATURE)) {
				break;
			}
			Thread.sleep(100);
		}

		logFeatures("done");
		assertFeatureNotInstalled(FEATURE);
		assertBundleNotInstalled(BUNDLE);

	}

	@Test
	public void featureDropAndKillMany() throws Exception {
		for (int k = 0; k < 3; k++) {
			featureDropAndKill();
		}
	}

}
