#!/bin/sh
ls runtimes/bundles/*/jbang/* | awk '{ print "cd " $0 "; cd ../../../../;"}' | sed 's/jbang\//jbang; jbang /g' | sh
