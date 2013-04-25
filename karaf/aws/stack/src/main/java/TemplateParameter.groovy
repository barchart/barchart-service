/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
/**
 * barchart cloud formation stack naming convention:
 * 
 * pattern: [prefix]-[index].[infix].[suffix]
 * 
 * prefix = first part of identity dns name
 * index  = auto increment
 * infix  = stack middle name
 * suffix = last part of identity dns name
 * 
 * rule : find existing, if any, and increment stack index
 */

///////////////////////////////////////////

/**
 * such as "stack" as in "news-1.stack.aws-dev.barchart.com."
 */ 
def ParamInfix = project.properties["ParamInfix"]
println "@@@ ParamInfix=$ParamInfix"

/**
 * such as "news.aws-dev.barchart.com" from https://github.com/barchart/barchart-configuration
 */ 
def ParamIdentity = project.properties["ParamIdentity"]
println "@@@ ParamIdentity=$ParamIdentity"

/**
 * dns zone hosting identity record
 */ 
def ParamZoneName = project.properties["ParamZoneName"]
println "@@@ ParamZoneName=$ParamZoneName"

/**
 * list of all dns records from same dns zone as identity record
 */ 
def ParamNameList = project.properties["ParamNameList"]
println "@@@ ParamNameList=$ParamNameList"

///////////////////////////////////////////

/** amazon naming convention constraint */
def amazonFilter( name ) {
	name.replaceAll("[^A-Za-z0-9-]","-")
}

def hostPrefix = (ParamIdentity + ".").replaceAll("." + ParamZoneName, "")

def stackPrefix = amazonFilter(hostPrefix)
def stackSuffix = ParamInfix + "." + ParamZoneName

def nameList = ParamNameList.split(";")

def indexList = [ 0 ]

for ( name in nameList ) {
	if( name.startsWith(stackPrefix) && name.endsWith(stackSuffix) ){

		def index = "0" + name // start with zero for missing records
				.replaceFirst(stackPrefix,"") // remove prefix
				.replaceAll(stackSuffix,"") // remove suffix
				.replaceAll("\\D","") // remove non digits

		indexList.add( index.toLong() )

	}
}

def stackIndex = indexList.max() + 1

///////////////////////////////////////////

def stackName = "$stackPrefix-$stackIndex"
def stackHost = "$stackName.$stackSuffix"

project.properties["ParamNickName"] = stackName
project.properties["ParamHostName"] = stackHost

println "### ParamNickName : " + project.properties["ParamNickName"]
println "### ParamHostName : " + project.properties["ParamHostName"]

///////////////////////////////////////////
