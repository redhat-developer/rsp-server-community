/*-----------------------------------------------------------------------------------------------
 *  Copyright (c) Red Hat, Inc. All rights reserved.
 *  Licensed under the EPL v2.0 License. See LICENSE file in the project root for license information.
 *-----------------------------------------------------------------------------------------------*/

import * as path from 'path';
import { Uri } from "vscode";
import { FelixRspLauncherOptions } from "./impl/server";

/**
 * RSP Provider ID
 */
const RSP_PROVIDER_ID = 'redhat.vscode-community-server-connector';

/**
 * RSP Provider Name - it will be displayed in the tree node
 */
const RSP_PROVIDER_NAME = 'Community Server Connector';

/**
 * The provider id to be used in the .rsp folder
 */
const RSP_ID = 'redhat-community-server-connector';

/**
 * The minimum port for this rsp instance to avoid clobbering
 */
 const RSP_MIN_PORT = 9000;

/**
 * The maximum port for this rsp instance to avoid clobbering
 */
const RSP_MAX_PORT = 9499;

/**
 * How long to wait before trying to connect
 */
const RSP_CONNECTION_DELAY = 1500;
/**
 * How frequently to attempt to connect after launch
 */
const RSP_CONNECTION_POLL_INTERVAL = 500;

export const getImageFilenameForServerType = (serverType: string): string => {
    if(serverType.toLowerCase().indexOf('karaf') != -1) {
        return 'karaf.png';
    }
    if(serverType.toLowerCase().indexOf('tomcat') != -1) {
        return 'tomcat.svg';
    }
    if(serverType.toLowerCase().indexOf('felix') != -1) {
        return 'felix.png';
    }
    if(serverType.toLowerCase().indexOf('jetty') != -1) {
        return 'jetty.png';
    }
    if(serverType.toLowerCase().indexOf('glassfish') != -1) {
        return 'glassfish.png';
    }
    if(serverType.toLowerCase().indexOf('payara') != -1) {
        return 'payara.png';
    }
    if(serverType.toLowerCase().indexOf('liberty') != -1) {
        return 'websphere.png';
    }

    return 'community.png';
}

export const OPTIONS: FelixRspLauncherOptions = {
    providerId: RSP_PROVIDER_ID,
    providerName: RSP_PROVIDER_NAME,
    rspId: RSP_ID,
    minPort: RSP_MIN_PORT,
    maxPort: RSP_MAX_PORT,
    connectionDelay: RSP_CONNECTION_DELAY,
    connectionPollFrequency: RSP_CONNECTION_POLL_INTERVAL,
    minimumSupportedJava: 11,
    getImagePathForServerType: function (serverType: string): Uri {
        const tmpPath: string = getImageFilenameForServerType(serverType);
        if( tmpPath )
            return Uri.file(path.join(__dirname, '..', '..', 'images', tmpPath));
        return null;
    }
}
