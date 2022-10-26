import { By, VSBrowser } from "vscode-extension-tester";

/**
 * @author Oleksii Korniienko <olkornii@redhat.com>
 */

export async function getServerConnector(connectorName,section) {
    await VSBrowser.instance.driver.wait(async () => {
        try {
            const serverItem = await section.findItem(connectorName);
            return (serverItem !== undefined);
        } catch (error) {
            throw error;
        }
    }, 10000);
    
    return await section.findItem(connectorName);
}
    
export async function waitForServerState(serverItem, serverStatesList) {
    await VSBrowser.instance.driver.wait(async () => {
        try {
            const element = await serverItem.findElement(By.className('label-description'));
            const innerHTML = await element.getAttribute('innerHTML');
            const actualState = innerHTML.replace('(', '').replace(')', '');
            return (serverStatesList.includes(actualState));
        } catch (error) {
            throw error;
        }
    }, 10000);
}