package org.jboss.tools.rsp.server.generic;

import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.server.spi.publishing.IPublishController;

/**
 * This interface shouldn't really exist. The listDeploymentOptions 
 * method should rightly belong in IPublishController, but until 
 * such change is made, we need to use this enhanced interface.
 * 
 */
public interface IPublishControllerWithOptions extends IPublishController {
	public Attributes listDeploymentOptions();
}
