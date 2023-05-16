# Runtime Server Protocol - Community Extension


[![Visual Studio Marketplace](https://img.shields.io/visual-studio-marketplace/v/redhat.vscode-community-server-connector?style=for-the-badge&label=VS%20Marketplace&logo=visual-studio-code&color=blue)](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector)
[![Downloads](https://img.shields.io/visual-studio-marketplace/d/redhat.vscode-community-server-connector?style=for-the-badge&color=purple)](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector)
[![Gitter](https://img.shields.io/gitter/room/redhat-developer/server-connector?style=for-the-badge&logo=gitter)](https://gitter.im/redhat-developer/server-connector)
[![Build Status](https://img.shields.io/github/actions/workflow/status/redhat-developer/rsp-server-community/actions.yml?style=for-the-badge&logo=github)](https://github.com/redhat-developer/rsp-server-community/actions)
[![License](https://img.shields.io/badge/license-EPLv2.0-brightgreen.png?style=for-the-badge)](https://github.com/redhat-developer/rsp-server-community/blob/master/vscode/LICENSE)

## Summary

This VSCode Extension provides a Runtime Server Protocol based server connector, which can start, stop, publish to, and otherwise control Community runtimes and servers like Apache Felix, Karaf, and Tomcat.


## Commands and features

[![ screencast ](https://img.youtube.com/vi/8JIcEzoPhlE/hqdefault.jpg)](https://youtu.be/8JIcEzoPhlE)

This extension depends on VSCode RSP UI Extension which is going to be installed automatically along with VSCode Community Server Connector Extension. RSP UI in conjuction with Community Server Connector Extension supports a number of commands for interacting with supported server adapters; these are accessible via the command menu (`Cmd+Shift+P` on macOS or `Ctrl+Shift+P` on Windows and Linux) and may be bound to keys in the normal way.


### Available Commands
   This extension provides no additional commands other than those available in [rsp-ui](https://github.com/redhat-developer/vscode-rsp-ui#available-commands)

## Extension Settings
   This extension provides no additional settings other than those available in [rsp-ui](https://github.com/redhat-developer/vscode-rsp-ui#extension-settings)

## Server Parameters
   To change Server Parameters, right-click on the server you want to edit and select `Edit Server`

### Additional Server Parameters
   This extension supports the server parameters available in [rsp-ui](https://github.com/redhat-developer/vscode-rsp-ui#server-parameters). This extension also support the following server parameters. 

   * `"server.classpath.additions"` - a list of semicolon-separated paths to add to the launch classpath (Java-based servers only)

### Supported Servers
   * Apache Tomcat [ 5.5 | 6.0 | 7.0 | 8.0 | 8.5 | 9.0 ]
   * Apache Karaf [ 4.8 ] 
   * Apache Felix [ 3.2 | 4.6 | 5.6 | 6.0 ]
   * Jetty [ 9.x ]
   * Glassfish [ 5.x ]
   * Websphere Liberty [ 21.x ]

