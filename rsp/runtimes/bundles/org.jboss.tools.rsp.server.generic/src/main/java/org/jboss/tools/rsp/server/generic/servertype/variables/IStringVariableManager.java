package org.jboss.tools.rsp.server.generic.servertype.variables;

public interface IStringVariableManager {

	IValueVariable getValueVariable(String name);

	IDynamicVariable getDynamicVariable(String name);

}
