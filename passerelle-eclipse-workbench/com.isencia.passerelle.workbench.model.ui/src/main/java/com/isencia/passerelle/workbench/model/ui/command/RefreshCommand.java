package com.isencia.passerelle.workbench.model.ui.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * The <code>SetConstraintCommand</code> allows Nodes to be moved and resized
 * 
 * @author Dirk Jacobs
 * 
 */
public class RefreshCommand extends org.eclipse.gef.commands.Command {
	private static final Logger logger = LoggerFactory
			.getLogger(RefreshCommand.class);	

	private NamedObj model;
	
	public Logger getLogger() {
		return logger;
	}

	public void execute() {
		doExecute();
	}
	
	@Override
	public boolean canExecute() {
		return true;
	}


	public void doExecute() {
		if( model==null)
			return;
		
		// Perform Change in a ChangeRequest so that all Listeners are notified
		model.requestChange(new ModelChangeRequest(this.getClass(), model, "setLocation"){
			@Override
			protected void _execute() throws Exception {
				if( getLogger().isDebugEnabled() )
					getLogger().debug("Refresh model");
			}
		});
	}



	public void redo() {
	}

	public void undo() {
	}

	@Override
	public boolean canUndo() {
		return false;
	}


	public void setModel(NamedObj model) {
		this.model = model;
	}

}
