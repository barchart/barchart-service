/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
/**
 * Generate RegionImageMap cloud formation template snippet.
 */

///////////////////////////////////////////

/** AWS cloud formtation snippet example */
// "RegionImageMap": {
//	 "ap-northeast-1": { "AMI": "TODO" },
//	 "ap-southeast-1": { "AMI": "TODO" },
//	 "ap-southeast-2": { "AMI": "TODO" },
//	 "eu-west-1":      { "AMI": "ami-58111f2c" },
//	 "sa-east-1":      { "AMI": "ami-656cb478" },
//	 "us-east-1":      { "AMI": "ami-ad9b0ec4" },
//	 "us-west-1":      { "AMI": "ami-ac3a19e9" },
//	 "us-west-2":      { "AMI": "ami-20cd4710" }
// },

println "@@@ Region-Image Map"

// sorted by region
def imageMap = new TreeMap(project.properties["amazonImageMap"])

for ( entry in imageMap.entrySet() ) {
	region = entry.key
	imageList = entry.value
	println "@@@ region=$region"
	for( image in imageList ){
		println "@@@   image=$image"
	}
}

// sorted by Image.Name
def lastAMI( imageList ) {
	if( imageList.isEmpty() ) {
		"TODO"
	} else {
		imageList.sort { it.name }.last().imageId
	}
}

def snippet = [
	"RegionImageMap" : imageMap.collectEntries
	{   region, imageList ->
		[
			region,
			[ "AMI" : lastAMI(imageList) ]
		]
	}
]

def json = new groovy.json.JsonBuilder(snippet)
def result = json.toPrettyString()

println "@@@ result: \n" + result

def folder = new File( "$project.basedir/target/" )
folder.mkdirs()
def file = new File( folder, "RegionImageMap.template" )
file.write( result )

println "@@@ result location : " + file.getAbsolutePath()

///////////////////////////////////////////
