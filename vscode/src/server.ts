/*-----------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat, Inc. All rights reserved.
 *  Licensed under the EPL v2.0 License. See LICENSE file in the project root for license information.
 *-----------------------------------------------------------------------------------------------*/

'use strict';

import * as cp from 'child_process';
import { ExtensionAPI } from './extensionApi';
import * as path from 'path';
import * as portfinder from 'portfinder';
import * as requirements from './requirements';
import * as vscode from 'vscode';
import { ServerInfo, ServerState } from 'vscode-server-connector-api';
import * as waitOn from 'wait-on';
import * as tcpPort from 'tcp-port-used';
import * as fs from 'fs-extra';
import { homedir } from 'os';
import { RSP_CONNECTION_DELAY, RSP_CONNECTION_POLL_INTERVAL, RSP_ID, RSP_MAX_PORT, RSP_MIN_PORT } from './constants';

let cpProcess: cp.ChildProcess;
let javaHome: string;
let port: number;
let spawned: boolean;

export function start(stdoutCallback: (data: string) => void,
    stderrCallback: (data: string) => void,
    api: ExtensionAPI): Promise<ServerInfo> {
    return requirements.resolveRequirements()
        .catch(error => {
            const msg = error && error.msg ? error.msg : "Unknown Error";
            const buttonArray = error && error.btns ? error.btns : [];
            const buttonLabels = buttonArray.map(btn => btn.label);
            // show error
            vscode.window.showErrorMessage(msg, buttonLabels)
                .then(selection => {
                    const btnSelected = buttonArray.find(btn => btn.label === selection);
                    if (btnSelected) {
                        if (btnSelected.openUrl) {
                            vscode.commands.executeCommand('vscode.open', btnSelected.openUrl);
                        } else {
                            vscode.window.showInformationMessage(
                                `To configure Java for Server Connector Extension add "rsp-ui.rsp.java.home" property to your settings file
                        (ex. "rsp-ui.rsp.java.home": "/usr/local/java/jdk-11.0.13").`);
                            vscode.commands.executeCommand(
                                'workbench.action.openSettingsJson'
                            );
                        }
                    }
                });
            // rethrow to disrupt the chain.
            throw error;
        })
        .then(requirements => {
            javaHome = requirements.java_home;
            const options: portfinder.PortFinderOptions = {
                port: RSP_MIN_PORT,
                stopPort: RSP_MAX_PORT
            };
            return portfinder.getPortPromise(options);
        })
        .then(async serverPort => {
            const lockFile = await getLockFile();
            const lockFileExist = await lockFileExists(lockFile);
            const portInUse = await lockFilePortInUse(lockFile);

            if(lockFileExist && portInUse) {
                const p = await getLockFilePort(lockFile);
                if (p) {
                    port = +p;
                }
                spawned = false;
            } else {
                if(lockFileExist && !portInUse) {
                    await fs.unlink(lockFile);
                }
                port = serverPort;
                const serverLocation = getServerLocation(process);
                startServer(serverLocation, serverPort, javaHome, stdoutCallback, stderrCallback, api);
                spawned = true;
            }
            const opts = {
                resources: [`tcp:localhost:${port}`],
                delay: RSP_CONNECTION_DELAY, // initial delay in ms, default 0
                interval: RSP_CONNECTION_POLL_INTERVAL, // poll interval in ms, default 250ms
                simultaneous: 1 // limit connection attempts to one per resource at a time
            };
            return waitOn(opts);
        })
        .then(() => {
            if (!port) {
                return Promise.reject('Could not allocate a port for the rsp server to listen on.');
            } else {
                return Promise.resolve({
                    port: port,
                    host: 'localhost',
                    spawned: spawned
                });
            }
        })
        .catch(error => {
            console.log(error);
            return Promise.reject(error);
        });
}

async function getLockFile() {
    const lockFile = path.resolve(homedir(), '.rsp', RSP_ID, '.lock');
    return lockFile;
}

async function lockFileExists(lockFile: string) {
    if (fs.existsSync(lockFile)) {
        return true;
    }
    return false;
}

async function getLockFilePort(lockFile: string): Promise<string | null> {
    if (fs.existsSync(lockFile)) {
        const port = await fs.readFile(lockFile, 'utf8');
        return port;
    }
    return null;
}


async function lockFilePortInUse(lockFile: string) {
    if (fs.existsSync(lockFile)) {
        const port = await fs.readFile(lockFile, 'utf8');
        const isBusy = await tcpPort.check(+port);
        return isBusy;
    }
    return false;
}

function getServerLocation(process: NodeJS.Process): string {
    return process.env.RSP_SERVER_LOCATION ?
        process.env.RSP_SERVER_LOCATION : path.resolve(__dirname, '..', '..', 'server');
}

function startServer(
    location: string, port: number, javaHome: string,
    stdoutCallback: (data: string) => void, stderrCallback: (data: string) => void, api: ExtensionAPI): void {
    const felix = path.join(location, 'bin', 'felix.jar');
    const java = path.join(javaHome, 'bin', 'java');
    // Debuggable version
    // const process = cp.spawn(java, [`-Xdebug`, `-Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=y`, `-Drsp.server.port=${port}`, '-jar', felix], { cwd: location });
    // Production version
    cpProcess = cp.spawn(java, [`-Drsp.server.port=${port}`, `-Dorg.jboss.tools.rsp.id=${RSP_ID}`, '-Dlogback.configurationFile=./conf/logback.xml', '-jar', felix], 
        { cwd: location, env: process.env });
    if(cpProcess) {
        if (cpProcess.stdout)
            cpProcess.stdout.on('data', stdoutCallback);
        if (cpProcess.stderr)
            cpProcess.stderr.on('data', stderrCallback);
        cpProcess.on('close', () => {
            if (api != null) {
                api.updateRSPStateChanged(ServerState.STOPPED);
            }
        });
        cpProcess.on('exit', () => {
            if (api != null) {
                api.updateRSPStateChanged(ServerState.STOPPED);
            }
        });
    }
}

export async function terminate(): Promise<void> {
    try {
        if (cpProcess) {
            cpProcess.removeAllListeners();
            cpProcess.kill();
        }
    } catch (err) {
        return Promise.reject(err);
    }
}
