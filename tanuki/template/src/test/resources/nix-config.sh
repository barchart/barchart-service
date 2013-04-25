#! /bin/bash
#
# Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


# application
APP_NAME="${serviceName}"
APP_LONG_NAME="${serviceName} ( ${description} )"

# location of this script
CMD_PATH="$(dirname "$(readlink -f ${BASH_SOURCE[0]})")"

# location of wrapper binaries
BIN_PATH="$CMD_PATH\bin"

# full path to the wrapper service script
APP_WRAPPER_SH="$CMD_PATH/app-wrapper.sh"

# debian, ubuntu
APP_WRAPPER_LINK_SH="/etc/init.d/$APP_NAME.sh"

case "$1" in
	
    'install')
		sudo ln -s $APP_WRAPPER_SH $APP_WRAPPER_LINK_SH
		echo "added APP_WRAPPER_LINK_SH: $APP_WRAPPER_LINK_SH" 
        ;;

    'uninstall')
    	sudo rm -f $APP_WRAPPER_LINK_SH
		echo "removed APP_WRAPPER_LINK_SH: $APP_WRAPPER_LINK_SH" 
        ;;

    *)
        echo "Usage: $0 { install | uninstall }"
        exit 1
        ;;
esac

exit 0
