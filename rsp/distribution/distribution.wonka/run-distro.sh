#!/bin/sh

unzip -o target/org.jboss.tools.rsp.distribution-0.10.0-SNAPSHOT.zip -d target && \
cd target/rsp-distribution/ && \
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -jar bin/felix.jar
