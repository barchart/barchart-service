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
import org.osgi.service.component.annotations.Property;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.conf.event.ConfigEvent;

/** how to receive OSGI events */
@Component(immediate = true)
public class EventExample implements EventHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** provide event subscription topic filter */
	@Property(name = EventConstants.EVENT_TOPIC)
	static final String TOPIC = ConfigEvent.ALL;

	@Activate
	protected void activate() {

		log.info("event-activate");

	}

	@Deactivate
	protected void deactivate() {

		log.info("event-deactivate");

	}

	@Override
	public void handleEvent(final Event event) {

		log.info("event-topic : {}", event.getTopic());

	}

}
