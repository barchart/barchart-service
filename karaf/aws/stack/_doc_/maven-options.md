<!--

    Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
clean package

--errors

--activate-profiles stack-params,stack-create

--define amazonRegion=${amazonRegion} 
--define stackName=${stackName}

--define ParamInfix=${ParamInfix}
--define ParamInstanceType=${ParamInstanceType}
--define ParamKeyName=${ParamKeyName}
--define ParamIdentity=${ParamIdentity}
--define ParamTopicName=${ParamTopicName}
--define ParamMinSize=${ParamMinSize}
--define ParamMaxSize=${ParamMaxSize}
--define ParamSecurityGroup=${ParamSecurityGroup}

