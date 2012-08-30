#!/bin/bash
#
# Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

#
#	${mavenStamp}
#

# real location of this script, un-linked if needed
REAL_PATH="$(dirname "$(readlink -f -n $0)")"

"$REAL_PATH/nix-wrapper-manager.sh" stop
"$REAL_PATH/nix-wrapper-manager.sh" disable
"$REAL_PATH/nix-wrapper-manager.sh" uninstall
