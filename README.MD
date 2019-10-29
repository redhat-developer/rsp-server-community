# The Runtime Server Protocol - Extras

## Summary

This repository is a home for an RSP server which can start, stop, and otherwise control runtimes other than those included with Red Hat's rsp-server. 

The protocol is based on LSP4J. In short, the base protocol is the same as LSP, but the specification of the messages is different. 

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification). 
The RSP Extensions to the base protocol can be found [here](https://github.com/redhat-developer/rsp-server/blob/master/schema/src/main/resources/schemaMD/specification.md)


## Building this extension
Since a lot of our dependencies aren't on an update site, we need to get them installed in the system in two different ways:  The first is in your local maven repository (for use when building), and the second is fetching them directly for use in a target platform (for use in eclipse during development).  

Run the following code:
    mkdir rsp.example
    cd rsp.example

    # Get and build the wildfly / cdk distribution of 
    # rsp-server to ensure all deps get into m2 repo
    git clone https://github.com/redhat-developer/rsp-server/
    cd rsp-server
    mvn clean install
    cd ../

    # Get this extension's code
    git clone https://github.com/robstryker/example-rsp
    example-rsp
    cd rsp/targetplatform/mavendeps/
    chmod +x fetch_distribution.sh
    ./fetch_distribution.sh
    cd ../../

    #Build this extension's code
    mvn clean install



