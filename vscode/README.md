# Runtime Server Protocol - Community Extension

[![License](https://img.shields.io/badge/license-EPLv2.0-brightgreen.svg)](https://github.com/redhat-developer/rsp-server-community/blob/master/README.md)
[![Visual Studio Marketplace](https://vsmarketplacebadge.apphb.com/version/redhat.vscode-community-server-connector.svg)](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector)
[![Gitter](https://badges.gitter.im/redhat-developer/server-connector.svg)](https://gitter.im/redhat-developer/rsp-server-connector?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)


## Summary

This VSCode Extension provides a Remote Server Protocol based server connector, which can start, stop, publish to, and otherwise control Community runtimes and servers like Apache Felix, Karaf, and Tomcat.


## Commands and features

[![ screencast ](https://img.youtube.com/vi/8JIcEzoPhlE/hqdefault.jpg)](https://youtu.be/8JIcEzoPhlE)

This extension depends on VSCode RSP UI Extension which is going to be installed automatically along with VSCode Community Server Connector Extension. RSP UI in conjuction with Community Server Connector Extension supports a number of commands for interacting with supported server adapters; these are accessible via the command menu (`Cmd+Shift+P` on macOS or `Ctrl+Shift+P` on Windows and Linux) and may be bound to keys in the normal way.


### Available Commands
   This extension provides no additional commands other than those available in [rsp-ui](https://github.com/redhat-developer/vscode-rsp-ui#available-commands)

## Extension Settings
   This extension provides no additional settings other than those available in [rsp-ui](https://github.com/redhat-developer/vscode-rsp-ui#extension-settings)

### Supported Servers
   * Apache Tomcat [ 5.5 | 6.0 | 7.0 | 8.0 | 8.5 | 9.0 ]
   * Apache Karaf [ 4.8 ] 
   * Apache Felix [ 3.2 | 4.6 | 5.6 | 6.0 ]
   * Jetty [ 9.x ]

