/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
import static org.junit.Assert.*
import groovy.json.JsonOutput

import org.junit.Test

class Project {
	def properties = new HashMap()
}

class TemplateParameterTest {

	@Test
	public void test1() {

		def project = new Project()

		def properties = project.properties;

		def nameList = [
			"news-1.aws.barchart.com." ,
			"news-12.aws.barchart.com." ,
			"news-123.aws.barchart.com." ,
			"news-1.aws-dev.barchart.com." ,
			"news-12.aws-dev.barchart.com." ,
			"news-123.aws-dev.barchart.com." ,
			"news-a.aws-dev.barchart.com." ,
			"news-1a.aws-dev.barchart.com." ,
			"news-a1.aws-dev.barchart.com." ,
			"news-1.stack.aws-dev.barchart.com." ,
			"news-12.stack.aws-dev.barchart.com." ,
			"news-123.stack.aws-dev.barchart.com." ,
			"news-0123.stack.aws-dev.barchart.com." ,
			"news-00123.stack.aws-dev.barchart.com." ,
			"news-000123.stack.aws-dev.barchart.com." ,
			"news-124a.stack.aws-dev.barchart.com." ,
			"news-a125.stack.aws-dev.barchart.com." ,
			"news-a126a.stack.aws-dev.barchart.com." ,
			""
		]

		def ParamNameList = nameList.join(";")

		properties["templateParamFile"] = "./target/template-testing-1.properties"

		properties["ParamInfix"] = "stack"
		properties["ParamIdentity"] = "news.aws-dev.barchart.com" // XXX no dot
		properties["ParamHostName"] = "abracadabra.aws-dev.barchart.com."
		properties["ParamZoneName"] = "aws-dev.barchart.com."
		properties["ParamNameList"] = ParamNameList

		println JsonOutput.prettyPrint(JsonOutput.toJson( project.properties))

		Binding binding = new Binding();
		binding.setVariable("project", project)

		new TemplateParameter(binding).run();

		assertEquals(properties["ParamNickName"].toString(), "news-127")
		assertEquals(properties["ParamHostName"].toString(), "news-127.stack.aws-dev.barchart.com.")

	}

	@Test
	public void test2() {

		def project = new Project()

		def properties = project.properties;

		def nameList = [""]

		def ParamNameList = nameList.join(";")

		properties["templateParamFile"] = "./target/template-testing-2.properties"

		properties["ParamInfix"] = "hack"
		properties["ParamIdentity"] = "news.media_7.aws-dev.barchart.com" // XXX no dot
		properties["ParamHostName"] = "abracadabra.aws-dev.barchart.com."
		properties["ParamZoneName"] = "aws-dev.barchart.com."
		properties["ParamNameList"] = ParamNameList

		println JsonOutput.prettyPrint(JsonOutput.toJson( project.properties))

		Binding binding = new Binding();
		binding.setVariable("project", project)

		new TemplateParameter(binding).run();

		assertEquals(properties["ParamNickName"].toString(), "news-media-7-1")
		assertEquals(properties["ParamHostName"].toString(), "news-media-7-1.hack.aws-dev.barchart.com.")

	}

	@Test
	public void test3() {

		def project = new Project()

		def properties = project.properties;

		// current DNS names
		def nameList = [""]

		def ParamNameList = nameList.join(";")

		properties["templateParamFile"] = "./target/template-testing-3.properties"

		properties["ParamInfix"] = "hack"
		properties["ParamIdentity"] = "cluster.news.aws.barchart.com" // XXX no dot
		properties["ParamHostName"] = "invalid"
		properties["ParamZoneName"] = "aws.barchart.com."
		properties["ParamNameList"] = ParamNameList

		println JsonOutput.prettyPrint(JsonOutput.toJson( project.properties))

		Binding binding = new Binding();
		binding.setVariable("project", project)

		new TemplateParameter(binding).run();

		assertEquals(properties["ParamNickName"].toString(), "cluster-news-1")
		assertEquals(properties["ParamHostName"].toString(), "cluster-news-1.hack.aws.barchart.com.")

	}
}
