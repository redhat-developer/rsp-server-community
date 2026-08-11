import * as os from 'os';
import * as path from 'path';

import { runTests } from '@vscode/test-electron';

async function main() {
    try {
        // The folder containing the Extension Manifest package.json
        // Passed to `--extensionDevelopmentPath`
        const extensionDevelopmentPath = path.resolve(__dirname, '../..');

        // The path to the extension test runner script
        // Passed to --extensionTestsPath
        const extensionTestsPath = path.resolve(__dirname, './');

        const launchArgs: string[] = [];
        // macOS limits Unix domain socket paths to 104 bytes; CI workspace paths
        // can exceed this, causing VS Code's IPC socket to fail with EINVAL.
        if (os.platform() === 'darwin') {
            launchArgs.push('--user-data-dir=/tmp/vsctest');
        }

        // Download VS Code, unzip it and run the integration test
        console.log(extensionDevelopmentPath, extensionTestsPath);
        await runTests({ extensionDevelopmentPath, extensionTestsPath, launchArgs });
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
}

main();
