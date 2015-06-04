package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;

public class ChangeActorPropertyCommand
	extends Command
{

public ChangeActorPropertyCommand() {
	super("changeName");
}


public void execute() {
	// Dummy Command  
}

@Override
public boolean canUndo() {
	return false;
}

}
