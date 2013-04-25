<!--

    Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
### info

barchart tanuki service wrapper

### maven release steps

* say you want to upgrade to the latest ```X.Y.Z``` version of 
[tanuki distro](http://wrapper.tanukisoftware.com/download/)

* update service template
``` 
[barchart-tanuki-service-template/pom.xml]/<version>3.5.15-build002-SNAPSHOT</version>
```
to reflect next version of tanuki distro, as first term, such as 
``` 
[barchart-tanuki-service-template/pom.xml]/<version>X.Y.Z-build002-SNAPSHOT</version>
```

* release [barchart-tanuki-service-template]; it will download and incorporate
the new ```X.Y.Z``` version of original tauki distribution files as part of the template; 

* update service -> template reference
```
[barchart-tanuki-service/pom.xml]/<templateVersion>3.5.15-build001</templateVersion>
```
to reflect your newly released template, such as 
```
[barchart-tanuki-service/pom.xml]/<templateVersion>X.Y.Z-build002</templateVersion>
```

* release [barchart-tanuki-service]; it will now depend on the newly built template

### integration test notes

[barchart-tanuki-service] uses integration tests in [barchart-tanuki-service/src/int]

these tests verify complete service wrapper life cycle:
* build a module  [barchart-tanuki-service/src/int/basic-module]
* build an app  [barchart-tanuki-service/src/int/basic-app]
* run verifier [barchart-tanuki-service/src/int/basic-tester]

the verifier is starting / stopping the service app, and
is looking for "magic file" that the app module should publish on
successful app startup by the service wrapper;

you will not be able to release [barchart-tanuki-service] when these tests fail
