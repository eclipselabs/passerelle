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
public class SetConstraintCommand extends org.eclipse.gef.commands.Command {
	private static final String Command_Label_Location = "change location command";
	private static final Logger logger = LoggerFactory
			.getLogger(SetConstraintCommand.class);

	private double[] newPos;

	private double[] oldPos;

	private NamedObj model;

	public Logger getLogger() {
		return logger;
	}

	public void execute() {
		doExecute();
	}

	@Override
	public boolean canExecute() {
		oldPos = ModelUtils.getLocation(model);
		return newPos != null && !newPos.equals(oldPos);
	}

	public void doExecute() {
		if (model == null)
			return;

		// Perform Change in a ChangeRequest so that all Listeners are notified
		model.requestChange(new ModelChangeRequest(this.getClass(), model,
				"setLocation") {
			@Override
			protected void _execute() throws Exception {
				oldPos = ModelUtils.getLocation(model).clone();
				ModelUtils.setLocation(model, newPos);
			}
		});
	}

	public String getLabel() {
		return Command_Label_Location;
	}

	public void redo() {
		if (canExecute())
			doExecute();
	}

	public void undo() {
		model.requestChange(new ModelChangeRequest(this.getClass(), model,
				"refresh") {
			@Override
			protected void _execute() throws Exception {
				ModelUtils.setLocation(model, oldPos);
			}
		});
	}

	@Override
	public boolean canUndo() {
		return oldPos != null;
	}

	public void setLocation(double[] location) {
		newPos = location;
	}

	public void setModel(NamedObj model) {
		this.model = model;
	}

}
