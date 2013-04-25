package com.barchart.karaf.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * Deploy feature-1.xml as feature.xml, then deploy feature-2.xml feature.xml in
 * the same place. Ensure both feature and bundle version change.
 * <p>
 * This test fails and is disabled.
 * <p>
 * See <a href="https://issues.apache.org/jira/browse/KARAF-2180">KARAF-2180</a?
 */
@Ignore
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class ChangeVersionTest1 extends TestAny {

	/**
	 * Test bundle name.
	 */
	final String BUNDLE = "com.barchart.test.barchart-karaf-base-test01";

	/**
	 * Test feature name.
	 */
	final String FEATURE = "barchart-karaf-base-test01-feature";

	/**
	 * Original version.
	 */
	final String VERSION_1 = "1.0.0.SNAPSHOT";

	/**
	 * Replacement version.
	 */
	final String VERSION_2 = "1.0.1.SNAPSHOT";

	@Configuration
	public Option[] config0() {
		return super.config();
	}

	@Configuration
	public Option[] config1() {
		return new Option[] {};
	}

	@Test
	public void changeVersion() throws Exception {

		final Path home = Paths.get(System.getProperty("karaf.home"));
		final Path test = home.resolve("../../../src/test/resources");

		log.info("\n\t home : {}\n\t test : {}", home, test);

		/** Original source. */
		final Path source1 = test.resolve("case-02/feature-1.xml");
		/** Replacement source. */
		final Path source2 = test.resolve("case-02/feature-2.xml");

		/** shared target */
		final Path target = home.resolve("etc/feature.xml");

		assertFeatureNotInstalled(FEATURE);
		assertBundleNotInstalled(BUNDLE);

		log.info("\n\t original");
		Files.copy(source1, target);

		log.info("\n\t wait for original install");
		for (int k = 0; k < 100; k++) {
			if (null != feature(FEATURE, VERSION_1)) {
				break;
			}
			Thread.sleep(100);
		}

		assertFeatureVersion(FEATURE, VERSION_1);
		assertBundleVersion(BUNDLE, VERSION_1);

		log.info("\n\t replacement");
		Files.copy(source2, target, StandardCopyOption.REPLACE_EXISTING);

		log.info("\n\t wait for original removal");
		for (int k = 0; k < 100; k++) {
			if (!isFeatureInstalled(FEATURE, VERSION_1)) {
				break;
			}
			Thread.sleep(100);
		}

		log.info("\n\t wait for replacement install");
		for (int k = 0; k < 100; k++) {
			if (isFeatureInstalled(FEATURE, VERSION_2)) {
				break;
			}
			Thread.sleep(100);
		}

		assertFeatureVersion(FEATURE, VERSION_2);
		assertBundleVersion(BUNDLE, VERSION_2);

		log.info("\n\t cleanup");
		Files.delete(target);

		log.info("\n\t wait for replacement removal");
		for (int k = 0; k < 100; k++) {
			if (!isFeatureInstalled(FEATURE)) {
				break;
			}
			Thread.sleep(100);
		}

		logFeatures("done");

		assertBundleNotInstalled(BUNDLE);
		assertFeatureNotInstalled(FEATURE);

	}

	// @Test
	public void changeVersionMany() throws Exception {
		for (int k = 0; k < 3; k++) {
			changeVersion();
		}
	}

}
