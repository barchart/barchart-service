#!/usr/bin/env python

### Script provided by DataStax.

import shlex
import subprocess
import time
import os
import sys

def exe(command, shellEnabled=False):
    print '[EXEC] %s' % command
    
    if shellEnabled:
        process = subprocess.Popen(command, shell=True, stderr=subprocess.PIPE, stdout=subprocess.PIPE)
    else:
        process = subprocess.Popen(shlex.split(command), stderr=subprocess.PIPE, stdout=subprocess.PIPE)
    
    output = process.communicate()

    if output[0]:
        print 'stdout:'
        print output[0]
    if output[1]:
        print 'stderr:'
        print output[1]

    return output

def pipe(command1, command2):
    # Helper function to execute piping commands and print traces of the commands and output for debugging/logging purposes
    print '[PIPE] %s | %s' % (command1, command2)
    p1 = subprocess.Popen(shlex.split(command1), stdout=subprocess.PIPE)
    p2 = subprocess.Popen(shlex.split(command2), stdin=p1.stdout)
    p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
    output = p2.communicate()[0]
    return output

def is_error(output):
    if not output[1] and not 'err' in output[0].lower() and not 'failed' in output[0].lower():
        return True
    else:
        return False

def install_software():

    # Disable oracle prompts.
    exe('sudo echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections')
    exe('sudo echo debconf shared/accepted-oracle-license-v1-1 seen true   | debconf-set-selections')
    
    # Add java repository.
    exe('sudo add-apt-repository --yes ppa:webupd8team/java')

    # Installer update.
    exe('sudo apt-get -y update')
    
    # Install java. 
    exe('sudo export DEBIAN_FRONTEND=noninteractive; apt-get --yes install oracle-java6-installer')
    exe('sudo java -version')

    exe('sudo apt-get -y upgrade')

    # Install tools.
    exe('sudo apt-get -y install htop sysstat iftop binutils pssh pbzip2 xfsprogs zip unzip openssl curl ant liblzo2-dev ntp python-pip tree unzip')

    # Install cassandra required.
    exe('sudo apt-get -y install libjna-java')

    # Install RAID setup.
    exe('sudo apt-get -y install mdadm')
    
    return
    

install_software()

print "Image succesfully configured."
