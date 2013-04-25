package com.barchart.karaf.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class BootFeaturesTest extends TestAny {

	/**
	 * Ensure base-app features are present.
	 */
	@Test
	public void bootFeaturesInstalled() throws Exception {
		assertFeaturesInstalled( //
				"standard", //
				"config", //
				"eventadmin", //
				"scr", //
				// "management", //
				// "obr", //
				"ssh" //
		);
	}

}
