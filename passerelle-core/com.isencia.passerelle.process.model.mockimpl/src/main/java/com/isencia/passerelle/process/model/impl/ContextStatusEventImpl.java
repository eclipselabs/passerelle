/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import com.isencia.passerelle.process.model.Context;

/**
 * @author delerw
 *
 */
public class ContextStatusEventImpl extends ContextEventImpl {
	private static final long serialVersionUID = 1L;

	protected ContextStatusEventImpl() {
	}

	/**
	 * Constructor to log the new status of a context.
	 * i.e. it takes the status of the context as topic.
	 * 
	 * @param context
	 */
	public ContextStatusEventImpl(Context context) {
		super(context,context.getStatus().name());
	}

	/**
	 * @param context
	 */
	public ContextStatusEventImpl(Context context, String message) {
		super(context,context.getStatus().name(),message);
	}

}
