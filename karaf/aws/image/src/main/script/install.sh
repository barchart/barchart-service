#!/bin/bash
#
# Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


#
# java + karaf install script for ubuntu
#

# super group in ubuntu image
ADMIN_GROUP="root"
# super user in ubuntu image
ADMIN_USER="ubuntu"

# karaf install home
KARAF_HOME="/var/karaf"
# karaf group name
KARAF_GROUP="karaf"
# karaf user name
KARAF_USER="karaf"

# karaf distro file name
KARAF_ARCHIVE="${karafFinalName}"
# karaf distro application folder prefix
KARAF_APP_DIR="app"

# this script location
WORK=$(dirname $0 )

#
#
#

echo "##################################################"
echo "### project : ${project.artifactId}"
echo "###"

echo "##################################################"
echo "### verify current location"
echo "###"

pwd

echo "##################################################"
echo "### verify upload contents"
echo "###"

ls -las $WORK

echo "##################################################"
echo "### configure kernel"
echo "###"

cp --verbose --force $WORK/sysctl-karaf.conf /etc/sysctl.d/
cp --verbose --force $WORK/limits-karaf.conf /etc/security/limits.d/

echo "##################################################"
echo "### disable installer dialogs"
echo "###"

export DEBIAN_FRONTEND=noninteractive
echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true    | debconf-set-selections
	
echo "##################################################"
echo "### install packages"
echo "###"
add-apt-repository --yes ppa:webupd8team/java
apt-get --yes update
apt-get --yes install oracle-jdk7-installer
apt-get --yes install xorg
apt-get --yes install git
apt-get --yes install mc tar wget zip unzip secure-delete
apt-get --yes upgrade
apt-get --yes dist-upgrade

echo "##################################################"
echo "### verify java"
echo "###"
																																																
java -version 2>&1

echo "##################################################"
echo "### setup karaf user home"
echo "###"

addgroup --system $KARAF_GROUP
adduser  --system --shell=/bin/bash --ingroup $KARAF_GROUP --home $KARAF_HOME $KARAF_USER

adduser  $ADMIN_USER $KARAF_GROUP

echo "##################################################"
echo "### copy karaf home resources"
echo "###"

cp --verbose --force --recursive $WORK/. $KARAF_HOME

echo "##################################################"
echo "### unpack karaf application"
echo "###"

unzip -o $KARAF_HOME/$KARAF_ARCHIVE -d $KARAF_HOME

echo "##################################################"
echo "### remove application folder time stamp suffix"
echo "###"

mv --verbose "$KARAF_HOME/$KARAF_APP_DIR"* "$KARAF_HOME/$KARAF_APP_DIR" 

echo "##################################################"
echo "### install tanuki service"
echo "###"

$KARAF_HOME/$KARAF_APP_DIR/nix-install.sh

echo "##################################################"
echo "### setup karaf home access permissions"
echo "###"

chown --recursive $KARAF_USER:$KARAF_GROUP $KARAF_HOME
chmod --recursive o-rwx,ug+rw,ugo-s $KARAF_HOME
find $KARAF_HOME -type d | xargs chmod g+xs

echo "##################################################"
echo "### tanuki start"
echo "###"

$KARAF_HOME/$KARAF_APP_DIR/nix-start.sh

echo "##################################################"
echo "### karaf fetching config repo and maven artifacts"
echo "###"

WAIT_SIZE=90

for step in $(seq 1 $WAIT_SIZE) ; do
	echo "wait : $step of $WAIT_SIZE"
	sleep 1s
done

echo "##################################################"
echo "### tanuki stop"
echo "###"

$KARAF_HOME/$KARAF_APP_DIR/nix-stop.sh

echo "##################################################"
echo "### cleanup work folder"
echo "###"

srm -r -l -v $WORK

sleep 3s

exit 0
