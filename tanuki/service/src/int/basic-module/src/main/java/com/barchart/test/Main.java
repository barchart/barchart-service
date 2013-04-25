/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.test;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private final static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(final String... args) throws Exception {

		log.debug("init");

		final File appDir = new File(System.getProperty("user.dir"));

		final File magicFile = new File(appDir, "magic-file");

		for (int k = 0; k < 100; k++) {

			magicFile.createNewFile();

			log.debug("ready {}", k);

			Thread.sleep(1 * 1000);

		}

		log.debug("done");

	}

}
