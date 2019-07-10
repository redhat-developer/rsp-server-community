# The Runtime Server Protocol - Example Extension

## Summary

This protocol will allow an IDE or other client to control various types of application servers. The IDE will communicate with any number of servers whose job is to discover, start, stop, and eventually publish to these application servers. 

The protocol is based on LSP4J, though the messages that can be sent or received are completely unrelated to the language server protocol itself. In short, the base protocol is the same as LSP, but the specification of the messages is different. 

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification). 
The RSP Extensions to the base protocol can be found [here](https://github.com/redhat-developer/rsp-server/blob/master/schema/src/main/resources/schemaMD/specification.md)


## Extending our existing implementation

This example uses an apache-felix based RSP. Most of the jars are provided by small units of functionality in use by the wildfly, eap, cdk and minishift RSP implementations. The goal of this repository is to show how easy it is to make a custom rsp distribution when building on top of the existing libraries and utilities. 


## Running this example
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



