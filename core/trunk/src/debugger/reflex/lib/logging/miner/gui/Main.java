/*
 * Created on Sep 29, 2004
 */
package reflex.lib.logging.miner.gui;

import reflex.Run;
import reflex.core.LogLevel;

/**
 * Main class to lauch miner UI
 * 
 * @author gpothier
 */
public class Main
{
	public static void main(String[] args) throws Throwable
	{
		LogLevel.set(LogLevel.VERBOSE);
		String theWorkingSet = "[+reflex.lib.logging.** " +
				// "-reflex.lib.logging.core.impl.transport.MessageType " +
				// "-reflex.lib.logging.core.api.collector.BehaviourType " +
				"-reflex.lib.logging.miner.impl.sql.EventType ]";

		String[] theReflexArgs = new String[]
		{ 
				"--working-set", theWorkingSet, 
				"-lp", "class:reflex.lib.pciobject.PCIObjectConfig",
				"reflex.lib.logging.miner.gui.MinerUI" 
		};
		
		Run.main(theReflexArgs, args);
	}
}
