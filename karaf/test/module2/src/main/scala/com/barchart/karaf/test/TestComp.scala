/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.karaf.test

import org.osgi.service.component.annotations._
import com.weiglewilczek.slf4s.Logging

@Component(immediate = true)
class TestComp extends Logging {

  @Activate
  protected def activate() {

    logger.info("### activate 12")

  }

  @Deactivate
  protected def deactivate() {

    logger.info("### deactivate 12")

  }

}
