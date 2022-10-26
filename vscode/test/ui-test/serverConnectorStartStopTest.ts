import { expect } from 'chai';
import { SideBarView, ViewControl, ActivityBar, DefaultTreeSection } from 'vscode-extension-tester';
import { getServerConnector, waitForServerState } from './common/serverUtility';
import { AdaptersConstants } from './common/constants/adaptersConstants';

/**
 * @author Oleksii Korniienko <olkornii@redhat.com>
 */
export function serverConnectorStartStopTest(): void {
  describe("Verify Community Server connector can be started and stopped", () => {
    let view: ViewControl;
    let sideBar: SideBarView;
    let section: DefaultTreeSection;

    beforeEach(async function () {
      this.timeout(10000);
      view = await new ActivityBar().getViewControl('Explorer');
      sideBar = await view.openView();
      const content = sideBar.getContent();
      section = (await content.getSection('Servers')) as DefaultTreeSection;
    });

    it('Start/stop server connector test', async function () {
      this.timeout(45000);
      await section.expand();

      const serverItem = await getServerConnector(AdaptersConstants.RSP_SERVER_PROVIDER_NAME, section);
      expect(serverItem).not.undefined;
      await waitForServerState(serverItem, [AdaptersConstants.SERVER_STARTED, AdaptersConstants.SERVER_CONNECTED]);

      const serverFirstContextMenu = await serverItem.openContextMenu();
      expect(await serverFirstContextMenu.hasItem(AdaptersConstants.RSP_SERVER_PROVIDER_STOP)).to.be.true;
      await serverFirstContextMenu.select(AdaptersConstants.RSP_SERVER_PROVIDER_STOP);
      await waitForServerState(serverItem, [AdaptersConstants.SERVER_STOPPED]);

      const serverSecondContextMenu = await serverItem.openContextMenu();
      expect(await serverSecondContextMenu.hasItem(AdaptersConstants.RSP_SERVER_PROVIDER_START)).to.be.true;
      await serverSecondContextMenu.select(AdaptersConstants.RSP_SERVER_PROVIDER_START);
      await waitForServerState(serverItem, [AdaptersConstants.SERVER_STARTED]);

      const serverThirdContextMenu = await serverItem.openContextMenu();
      expect(await serverThirdContextMenu.hasItem(AdaptersConstants.RSP_SERVER_PROVIDER_STOP)).to.be.true;
      serverThirdContextMenu.close();
    });

    after(async () => {
      if (sideBar && (await sideBar.isDisplayed()) && view) {
        await view.closeView();
      }
    });
  });
}