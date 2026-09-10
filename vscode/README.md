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
   To change Server Parameters, right-click on the server you want to edit and select `Edit Server`.

   This extension supports all global and provisional server parameters documented in [vscode-rsp-ui](https://github.com/redhat-developer/vscode-rsp-ui#server-parameters), including `mapProperty.launch.env` for setting environment variables. The parameters below are specific to the community server types.

### Community Server Parameters

   * `"server.base.dir"` - a filesystem path pointing to a server configuration directory. For Tomcat, this is used as `CATALINA_BASE` (defaults to `server.home.dir` if not set)
   * `"server.http.port"` - the HTTP port for the server (default `8080`)
   * `"server.http.host"` - the HTTP host for the server (default `localhost`)
   * `"server.deploy.dir"` - the deployment directory (default `${server.base.dir}/webapps/` for Tomcat)
   * `"server.classpath.additions"` - semicolon-separated paths to add to the launch classpath (Java-based servers only)

## FAQ

### 1. How can I override VM and program arguments?

Set `"args.override.boolean"` to `true` via `Edit Server`. On the next server start, two properties will be generated: `"args.vm.override.string"` and `"args.program.override.string"`. You can then edit these to customize the launch arguments. If you set `"args.override.boolean"` back to `false`, the server will auto-generate the arguments as normal.

These argument strings support Eclipse-style `${env_var:NAME}` references, which are resolved against the RSP server's process environment before launching. For example: `-Dapp.name=${env_var:MY_APP}`.

See the [vscode-rsp-ui documentation](https://github.com/redhat-developer/vscode-rsp-ui#provisional-global-server-parameters) for more details.

### 2. My server fails to start with `UnsatisfiedLinkError` for a native library — how do I set `LD_LIBRARY_PATH`?

If your application uses native libraries (e.g., Oracle Instant Client), you may need to set `LD_LIBRARY_PATH` (Linux) or `DYLD_LIBRARY_PATH` (macOS) in the launched server process. Java's `-Djava.library.path` only tells the JVM where to find `.so`/`.dylib` files directly, but the OS dynamic linker resolves *transitive* native dependencies using `LD_LIBRARY_PATH` instead.

Setting `LD_LIBRARY_PATH` in your terminal does not help if VSCode was launched from the desktop, because GUI applications do not inherit terminal environment variables.

To set environment variables for the server process, use `Edit Server` and add:

```json
"mapProperty.launch.env": {
    "LD_LIBRARY_PATH": "/path/to/native/libs"
}
```

### 3. My server uses logback/SLF4J instead of the default logging — how do I add logging JARs?

If your server has been configured to use logback or SLF4J instead of the default logging framework, you may see `ClassNotFoundException` for classes like `org.slf4j.bridge.SLF4JBridgeHandler`. Use `"server.classpath.additions"` to add the path to your logging JARs:

```json
"server.classpath.additions": "/path/to/jul-to-slf4j.jar;/path/to/slf4j-api.jar"
```

### 4. Is there a video that explains how these extensions and the Runtime Server Protocol work?
Yes. You can watch [this video](https://www.youtube.com/watch?v=8JIcEzoPhlE) to learn more.

### Supported Servers
   * Apache Tomcat [ 5.5 | 6.0 | 7.0 | 8.0 | 8.5 | 9.0 | 10.x | 11.x ]
   * Apache Karaf [ 4.8 ]
   * Apache Felix [ 3.2 | 4.6 | 5.6 | 6.0 ]
   * Jetty [ 9.x ]
   * Glassfish [ 5.x ]
   * Websphere Liberty [ 21.x ]

