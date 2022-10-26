import { extensionInstalledUITest } from './extensionInstalledUITest';
import { serverConnectorStartStopTest } from './serverConnectorStartStopTest';

/**
 * @author Oleksii Korniienko <olkornii@redhat.com>
 */

describe('VSCode rsp-server-community - UI tests', () => {
  extensionInstalledUITest();
  serverConnectorStartStopTest();
});