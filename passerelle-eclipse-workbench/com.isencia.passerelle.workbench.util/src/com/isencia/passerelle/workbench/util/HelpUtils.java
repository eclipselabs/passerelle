

package com.isencia.passerelle.workbench.util;

import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;



public class HelpUtils {
	public static final String HELP_BUNDLE_ID = "com.isencia.passerelle.actor.conf";

	public static String getContextId(Object element) {
		if (element instanceof String) {
			return HELP_BUNDLE_ID + ".name";
		} else if (element instanceof Variable) {
			return  getContextIdOfVariable((Variable) element);
		}
		return null;
	}
	public static String getContextIdOfVariable(Variable param) {

		Attribute attr = (Attribute) param;
		if (param.getContainer() != null) {
			String helpBundle = HELP_BUNDLE_ID;
			String actorName = param.getContainer().getClass()
					.getName().replace(".", "_");
			return helpBundle + "." + actorName + "_"
					+ attr.getName();
		}
		return "";
	}
}
