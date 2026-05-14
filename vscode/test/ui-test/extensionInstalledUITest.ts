import { expect } from 'chai';
import { ActivityBar, ExtensionsViewItem, ExtensionsViewSection, SideBarView, ViewControl } from 'vscode-extension-tester';
import { AdaptersConstants } from './common/constants/adaptersConstants';

/**
 * @author Oleksii Korniienko <olkornii@redhat.com>
 */
export function extensionInstalledUITest(): void {
  describe("Verify extension is vissible in installed list", () => {
    let view: ViewControl;
    let sideBar: SideBarView;
    let section: ExtensionsViewSection;

    beforeEach(async function () {
      this.timeout(10000);
      view = await new ActivityBar().getViewControl('Extensions');
      sideBar = await view.openView();
      const content = sideBar.getContent();
      section = (await content.getSection('Installed')) as ExtensionsViewSection;
    });

    it('RSP server community extension is installed', async function () {
      this.timeout(20000);

      // Retry logic to handle StaleElementReferenceError
      const maxRetries = 3;
      let lastError: Error | undefined;

      for (let attempt = 0; attempt < maxRetries; attempt++) {
        try {
          const item = (await section.findItem(`@installed ` + AdaptersConstants.RSP_EXTENSTION_NAME)) as ExtensionsViewItem;
          expect(item).not.undefined;
          const title = await item.getTitle();
          expect(title).to.equal(AdaptersConstants.RSP_EXTENSTION_NAME);
          return; // Success, exit the test
        } catch (error: any) {
          lastError = error;
          if (error.name === 'StaleElementReferenceError' && attempt < maxRetries - 1) {
            // Wait a bit before retrying
            await new Promise(resolve => setTimeout(resolve, 500));
            continue;
          }
          // If it's not a stale element error, or we're out of retries, throw
          throw error;
        }
      }

      // If we get here, all retries failed
      throw lastError!;
    });

    afterEach(async function () {
      this.timeout(10000);
      section.clearSearch();
    });

    after(async () => {
      if (sideBar && (await sideBar.isDisplayed()) && view) {
        await view.closeView();
      }
    });
  });
}