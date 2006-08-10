/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import tod.impl.dbgrid.dbnode.CFlowMap;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.PagedFile;

public class TestCFlowMap
{
	@Test public void test() throws FileNotFoundException
	{
		DatabaseNode theNode = new DatabaseNode(0);
		PagedFile theIndexFile = new PagedFile(new File("cflow-index.bin"), DB_INDEX_PAGE_SIZE);
		PagedFile theDataFile = new PagedFile(new File("cflow-data.bin"), DB_CFLOW_PAGE_SIZE);
		
		CFlowMap theMap = new CFlowMap(theNode, theIndexFile, theDataFile);
		
		

	}
}
