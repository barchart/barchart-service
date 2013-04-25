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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.conf.event.ConfigEvent;
import com.barchart.conf.sync.api.ConfigManager;
import com.barchart.conf.util.ConfigAny;
import com.typesafe.config.Config;

/** how to process configuration changes */
@Component(immediate = true)
public class ConfigExample implements EventHandler {

	//

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** provide event subscription topic filter */
	@Property(name = EventConstants.EVENT_TOPIC)
	static final String TOPIC = ConfigEvent.CONFIG_CHANGE;

	@Activate
	protected void activate() {

		log.info("config-activate");

		if (manager.isConfigValid()) {
			processChange("CONFIG VALID");
		}

	}

	@Deactivate
	protected void deactivate() {

		log.info("config-deactivate");

	}

	private ConfigManager manager;

	@Reference
	protected void bind(final ConfigManager s) {
		manager = s;
	}

	protected void unbind(final ConfigManager s) {
		manager = null;
	}

	@Override
	public void handleEvent(final Event event) {

		log.info("event-topic : {}", event.getTopic());

		if (manager.isConfigValid()) {
			processChange("CONFIG CHANGE");
		}

	}

	/** here you apply changes to your components */
	private void processChange(final String message) {

		log.info("@@@@@@@@@@@@@@@@@@@@@@");

		log.info(message);

		final Config config = manager.getConfig();

		log.info("config : \n{}", ConfigAny.toString(config));

		log.info("@@@@@@@@@@@@@@@@@@@@@@");

	}

}
