package tod.impl.dbgrid.db;

import java.io.File;

import tod.core.ILogCollector;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.dbgrid.GridEventCollector1;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.db.file.HardPagedFile;

public class DatabaseNode1 extends DatabaseNode 
{
	@Override
	protected synchronized void initDatabase() 
	{
		HardPagedFile.clearCache(); //TODO: only clear pages of current database
		super.initDatabase();
	}
	
	@Override
	protected EventDatabase createEventDatabase(File aDirectory)
	{
		File theFile = new File(aDirectory, "events.bin");
		theFile.delete();

		return new EventDatabase1(getStructureDatabase(), getNodeId(), theFile);
	}

	@Override
	protected ObjectsDatabase createObjectsDatabase(File aDirectory, String aName)
	{
		File theFile = new File(aDirectory, "objects-"+aName+".bin");
		theFile.delete();
		
		return new ObjectsDatabase1(theFile);
	}
	
	@Override
	public ILogCollector createLogCollector(IHostInfo aHostInfo)
	{
		return new MyCollector(getMaster(), aHostInfo, getStructureDatabase(), this);
	}

	private static class MyCollector extends GridEventCollector1
	{
		private GridMaster itsMaster;
		
		public MyCollector(
				RIGridMaster aMaster, 
				IHostInfo aHost, 
				IMutableStructureDatabase aStructureDatabase,
				DatabaseNode aNode)
		{
			super(aHost, aStructureDatabase, aNode);

			// Only for local master (see #thread). 
			if (aMaster instanceof GridMaster)
			{
				itsMaster = (GridMaster) aMaster;
			}
		}

		@Override
		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
			if (itsMaster != null)
			{
				ThreadInfo theThread = createThreadInfo(getHost(), aThreadId, aJVMThreadId, aName);
				itsMaster.registerThread(theThread);
			}
			else throw new UnsupportedOperationException("Should have been filtered by master");		
		}
	}

}
