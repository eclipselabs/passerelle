package com.isencia.passerelle.actor.test;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class TestRunner implements CommandProvider {

	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\n---Passerelle Actor test---\n");
		buffer.append("\trunActorTest\n");
		return buffer.toString();
	}

	public void _runActorTest(CommandInterpreter ci) {
		junit.textui.TestRunner.run(ActorTest.class);
	}
}
