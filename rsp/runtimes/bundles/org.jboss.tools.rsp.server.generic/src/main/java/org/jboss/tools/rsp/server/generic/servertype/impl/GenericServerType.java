package org.jboss.tools.rsp.server.generic.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.IServerDelegateProvider;
import org.jboss.tools.rsp.server.spi.servertype.AbstractServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericServerType extends AbstractServerType {
	
	protected Attributes required = null;
	protected Attributes optional = null;
	private String runModes;
	private JSONMemento requiredAttributes;
	private JSONMemento optionalAttributes;
	private IServerDelegateProvider delegateProvider;
	
	public GenericServerType(String id, String name, String desc,
			String runModes, JSONMemento requiredAttributes, JSONMemento optionalAttributes,
			IServerDelegateProvider delegateProvider) {
		super(id, name, desc);
		this.runModes = runModes;
		this.requiredAttributes = requiredAttributes;
		this.optionalAttributes = optionalAttributes;
		this.delegateProvider = delegateProvider;
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		if( delegateProvider != null )
			return delegateProvider.createServerDelegate(getId(), server);
		return null; // TODO
	}

	@Override
	public Attributes getRequiredAttributes() {
		if(required == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			if( requiredAttributes != null ) {
				fillAttributeUtility(attrs, requiredAttributes);
			}
			required = attrs.toPojo();
		}
		return required;
	}

	@Override
	public Attributes getOptionalAttributes() {
		if (optional == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			if( optionalAttributes != null ) {
				fillAttributeUtility(attrs, optionalAttributes);
			}
			this.optional = attrs.toPojo();
		}
		return optional;
	}
	
	private void fillAttributeUtility(CreateServerAttributesUtility util, JSONMemento memento) {
		JSONMemento[] attrKeys = memento.getChildren();
		for( int i = 0; i < attrKeys.length; i++ ) {
			String id = attrKeys[i].getNodeName();
			String type = attrKeys[i].getString("type");
			String desc = attrKeys[i].getString("description");
			String dVal = attrKeys[i].getString("defaultValue");
			Object dValObj = convertDefaultValue(dVal, type);
			String secret = attrKeys[i].getString("secret");
			boolean secretVal = (secret == null ? false : Boolean.valueOf(secret));
			util.addAttribute(id, type, desc, dValObj, secretVal);
		}
	}
	
	@Override
	public Attributes getRequiredLaunchAttributes() {
		CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
		return attrs.toPojo();
	}

	@Override
	public Attributes getOptionalLaunchAttributes() {
		CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
		return attrs.toPojo();
	}

	@Override
	public ServerLaunchMode[] getLaunchModes() {
		if( runModes == null || runModes.trim().isEmpty())
			return new ServerLaunchMode[] {};
		
		String[] modeArr = runModes.split(",");
		ServerLaunchMode[] arr = new ServerLaunchMode[modeArr.length];
		for( int i = 0; i < modeArr.length; i++ ) {
			String modeId = modeArr[i];
			String modeDesc = findRunModeDescription(modeId);
			arr[i] = new ServerLaunchMode(modeId, modeDesc);
		}
		return arr;
	}

	private String findRunModeDescription(String modeId) {
		if( modeId == null || modeId.isEmpty())
			return null;
		if(ILaunchModes.RUN.equals(modeId))
				return ILaunchModes.RUN_DESC;
		if(ILaunchModes.DEBUG.equals(modeId))
				return ILaunchModes.DEBUG_DESC;
		return modeId;
	}

	private Object convertDefaultValue(String val, String type) {
		if( ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) 
			return val;
		if( ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type))
			return Integer.parseInt(val);
		if( ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type))
			return Boolean.parseBoolean(val);
		// TODO list and map?? 
		return val; 
	}
}
