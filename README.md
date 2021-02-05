# Runtime Server Protocol - Community Extension

[![RSP+Community+CI](https://img.shields.io/github/workflow/status/redhat-developer/rsp-server-community/RSP%20Community%20CI)](https://github.com/redhat-developer/rsp-server-community/actions)
[![License](https://img.shields.io/badge/license-EPLv2.0-brightgreen.svg)](https://github.com/redhat-developer/rsp-server-community/blob/master/README.md)
[![Visual Studio Marketplace](https://vsmarketplacebadge.apphb.com/version/redhat.vscode-server-connector.svg)](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector)
[![Gitter](https://badges.gitter.im/redhat-developer/server-connector.svg)](https://gitter.im/redhat-developer/server-connector?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)


## Summary

This repository is a home for an RSP server, and the associated VSCode Extension, which can start, stop, and otherwise control Community runtimes and servers like Apache Felix, Karaf, Tomcat, Glassfish, and Jetty. 

The protocol is based on LSP4J. In short, the base protocol is the same as LSP, but the specification of the messages is different. 

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification). 
The RSP Extensions to the base protocol can be found [here](https://github.com/redhat-developer/rsp-server/blob/master/schema/src/main/resources/schemaMD/specification.md)


## Commands and features

[![ screencast ](https://img.youtube.com/vi/8JIcEzoPhlE/hqdefault.jpg)](https://youtu.be/8JIcEzoPhlE)

This extension depends on VSCode RSP UI Extension which is going to be installed automatically along with VSCode Community Server Connector Extension. RSP UI in conjuction with Community Server Connector Extension supports a number of commands for interacting with supported server adapters; these are accessible via the command menu (`Cmd+Shift+P` on macOS or `Ctrl+Shift+P` on Windows and Linux) and may be bound to keys in the normal way.




## Building this server and extension

Run the following code:

    # First, build the server
    git clone https://github.com/redhat-developer/rsp-server-community
    cd rsp-server-community/rsp
    mvn clean install
    cd ../

    # Now build the extension
    cd vscode/

    #Build this extension's code
    npm install
    npm run build
    npm run test
    vsce package


