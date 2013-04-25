#!/bin/bash
#
# Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


#
# karaf user setup
#

echo "##################################################"

ADMIN_GROUP="root"
ADMIN_USER="root"

KARAF_USER="karaf"
KARAF_GROUP="$KARAF_USER"
KARAF_HOME="/var/$KARAF_USER"

deluser $KARAF_USER
delgroup $KARAF_GROUP

addgroup --system $KARAF_GROUP
adduser  --system --shell=/bin/bash --ingroup $KARAF_GROUP --home $KARAF_HOME $KARAF_USER

chown --changes --recursive $KARAF_USER:$KARAF_GROUP $KARAF_HOME
chmod --changes --recursive o-rwx,g+rw,ugo-s $KARAF_HOME
find $KARAF_HOME -type d -exec chmod --changes ug+s {} \;

#setfacl --recursive --modify g:$KARAF_GROUP:rwX,d:g:$KARAF_GROUP:rwX $KARAF_HOME

ls -las $KARAF_HOME

echo "##################################################"
