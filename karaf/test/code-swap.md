<!--

    Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
### info

this explains how to use code-swap in karaf service


### init

* make sure you use oracle java-7, say form
[webupd8](http://www.webupd8.org/2012/01/install-oracle-java-jdk-7-in-ubuntu-via.html)

* both maven, eclipse, karaf must run on java-7

* eclipse -> preferences -> java -> installed jre -> 

		must have java 7 as default
		must map java-se-6 -> java-7
		must map java-se-7 -> java-7

### idea

this setup is following `shadow bundle` approach; namely, 
the module in development is installed twice: 

* first, as normal jar bundle; one time on karaf start up, 
via `features.xml` `install="auto"` dependency 

* second, as jardir bundle auto-made from `target/classes`; 
updated on every eclipse edit

`jardir:...` is a custom url handler from [felix file install]
(http://felix.apache.org/site/apache-felix-file-install.html)

### steps

* add testing module as dependency to karaf service app
		
		/barchart-karaf-test-app/pom.xml
		
		<dependency>
			<groupId>com.barchart.test</groupId>
			<artifactId>barchart-karaf-test-module1</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>


* make sure to do `eclipse -> maven -> update -> force`
on all related snapshot dependency projects 
to download latest snapshots into your local maven repo;
else you suffer from version confusion :-)

* run `parent maven install` once; this will produce few required things;
		
		/barchart-karaf-test/build/maven-install.ant

* `features.xml` with appropriate dependendencies
		
		/barchart-karaf-test-app/target/@etc/barchart-karaf-test-app-features.xml
		...
		<bundle>mvn:com.barchart.test/barchart-karaf-test-module1/1.0.0-SNAPSHOT</bundle>
		...

* as well as actual default jars in `${user.home}/.m2/repository`, 
including jar for module under development; this testing module jar, 
along with its dependencies will be loaded on karaf service startup
		
		barchart-karaf-test-module1-1.0.0-SNAPSHOT.jar

* also, take a look on module jardir file-install configuration:
it basically makes an effort to discover module jardir bundle as late as possible
to permit "normal" module bundle jar be installed first, resolve dependencies, etc.
		
		in source:
		/barchart-karaf-test-app/app/etc/org.apache.felix.fileinstall-module1.cfg
		or target:
		/barchart-karaf-test-app/target/@etc/org.apache.felix.fileinstall-module1.cfg

* start karaf service app
		
		/barchart-karaf-test-app/build/service-start.ant

* work by doing edit and save java class in eclipse module project source :-)

		/barchart-karaf-test-module1/src/main/java/com/barchart/karaf/module1/BasicExample.java

* verify file-install module update/install events,
as well as SCR components life cycle events (from examples below) 
in the karaf log:

		/barchart-karaf-test-app/target/@log/karaf.log
		
		sample result:
		/barchart-karaf-test-app/result/karaf.log
		
		you are looking for "Updating/Installed/Started" triad:
		2012-08-25 16:53:01,524 | WARN  | t-module1/target | fileinstall                      | 24 - org.apache.felix.fileinstall - 3.2.4 | A bundle with the same symbolic name (com.barchart.test.barchart-karaf-test-module1) and version (1.0.0.SNAPSHOT) is already installed.  Updating this bundle instead.
		2012-08-25 16:53:01,528 | INFO  | t-module1/target | fileinstall                      | 24 - org.apache.felix.fileinstall - 3.2.4 | Installed /work/git/barchart-karaf/barchart-karaf-test/barchart-karaf-test-module1/target/classes
		2012-08-25 16:53:02,700 | INFO  | t-module1/target | fileinstall                      | 24 - org.apache.felix.fileinstall - 3.2.4 | Started bundle: mvn:com.barchart.test/barchart-karaf-test-module1/1.0.0-SNAPSHOT
		

* create brand new `barchart-karaf-test-module2` 
and reproduce module setup for yourself

### be ware

there are few ways to break code-swap; 
for example these scenarios might require `service + module` re-install:  

* change in module dependencies 

* change in module packages (rename, add, delete) 

* change in module class names 

* obsolete component.xml (say, deleted component class )

you will know if you have one, when all was working fine and then you see bizarre 
class load or missing import or etc. exception

still, some of these could be fixed by `eclipse -> project -> clean` of the module project,
while service is running;

if not - stop; re-install; start; - karaf service again

### be nice

please do not play with code swap all day long :-)
 
jenkins does not care for code swap, unit tests are more important!

### replicate

feel free to replicate `barchart-karaf-test-app` project 
inside other target repos where you need code swap oriented development

### examples

* basic SCR component lifecycle

		/barchart-karaf-test-module1/src/main/java/com/barchart/karaf/module1/BasicExample.java
		
* how to receive OSGI events

		/barchart-karaf-test-module1/src/main/java/com/barchart/karaf/module1/EventExample.java
		
* how to process configuration changes

		/barchart-karaf-test-module1/src/main/java/com/barchart/karaf/module1/ConfigExample.java
