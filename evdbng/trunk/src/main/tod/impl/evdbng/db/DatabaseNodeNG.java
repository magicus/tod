package tod.impl.evdbng.db;

import java.io.File;

import tod.core.ILogCollector;
import tod.core.database.structure.IHostInfo;
import tod.impl.dbgrid.db.DatabaseNode;
import tod.impl.dbgrid.db.EventDatabase;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.evdbng.GridEventCollectorNG;
import tod.impl.evdbng.db.file.PagedFile;

public class DatabaseNodeNG extends DatabaseNode
{

	@Override
	protected EventDatabase createEventDatabase(File aDirectory)
	{
		File theIndexFile = new File(aDirectory, "indexes.bin");
		theIndexFile.delete();
		
		File theEventsFile = new File(aDirectory, "events.bin");
		theEventsFile.delete();
		
		return new EventDatabaseNG(
				getStructureDatabase(), 
				getNodeId(), 
				new PagedFile(theIndexFile),
				new PagedFile(theEventsFile));
	}

	@Override
	protected ObjectsDatabase createObjectsDatabase(File aDirectory, String aName)
	{
		File theFile = new File(aDirectory, "objects-"+aName+".bin");
		theFile.delete();
		PagedFile thePagedFile = new PagedFile(theFile);
		return new ObjectsDatabaseNG(thePagedFile, thePagedFile);
	}

	@Override
	public ILogCollector createLogCollector(IHostInfo aHostInfo)
	{
		return new GridEventCollectorNG(aHostInfo, getStructureDatabase(), this);
	}

}
