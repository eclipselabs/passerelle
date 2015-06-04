package com.isencia.passerelle.workbench.model.editor.ui.properties;

import java.util.Comparator;

import ptolemy.kernel.util.NamedObj;

public class NamedObjComparator implements Comparator<NamedObj> {

	/**
	 * Sorts parameters by name alphabetically.
	 */

	public int compare(NamedObj o1, NamedObj o2) {
		return o1.getDisplayName().compareTo(o2.getDisplayName());
	}

}
