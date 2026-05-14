#!/bin/sh
cd rsp
MAVEN_OPTS="-Djdk.xml.maxGeneralEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 -Djdk.xml.entityExpansionLimit=0" mvn clean install
cd ../vscode
npm install
npm run build
vsce package
