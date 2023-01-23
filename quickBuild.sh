#!/bin/sh
cd rsp
mvn clean install
cd ../vscode
npm install
npm run build
vsce package
