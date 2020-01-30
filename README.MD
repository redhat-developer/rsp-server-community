# The Runtime Server Protocol - Apache

## Summary

This repository is a home for an RSP server which can start, stop, and otherwise control Apache runtimes and servers like Felix, Karaf, and Tomcat.

The protocol is based on LSP4J. In short, the base protocol is the same as LSP, but the specification of the messages is different. 

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification). 
The RSP Extensions to the base protocol can be found [here](https://github.com/redhat-developer/rsp-server/blob/master/schema/src/main/resources/schemaMD/specification.md)


## Building this extension

Run the following code:
    # First, build the server
    git clone https://github.com/robstryker/rsp-server-apache.git
    cd rsp-server-apache/rsp
    mvn clean install
    cd ../

    # Now build the extension
    cd vscode/

    #Build this extension's code
    npm install
    npm run build
    npm run test
    vsce package


