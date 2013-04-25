/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.karaf.module1;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** basic SCR component lifecycle */
@Component(immediate = true)
public class BasicExample {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Activate
	protected void activate() {

		log.info("basic-activate   9");

	}

	@Deactivate
	protected void deactivate() {

		log.info("basic-deactivate 9");

	}

}
