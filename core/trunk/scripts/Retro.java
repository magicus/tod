import net.sf.retrotranslator.transformer.*;
import java.lang.reflect.*;

public class Retro
{
	public static void main(String[] args) throws Exception
	{
		String theVersion = System.getProperty("java.version");
		System.out.println("JVM version: "+theVersion);
		if (! theVersion.startsWith("1.5"))
		{
			System.out.println("Using RetroTranslator");
			JITRetrotranslator.install();
		}
		else
		{
			System.out.println("JVM 1.5 detected");
		}
		
		if ("session".equals(args[0]))
		{
			launch("tod.impl.dbgrid.GridSession", new String[] {"no-node"});
		}
		else if ("node".equals(args[0]))
		{
			launch("tod.impl.dbgrid.dbnode.DatabaseNode", new String[] {});
		}
		else if ("store".equals(args[0]))
		{
			launch("tod.utils.StoreTODServer", new String[] {});
		}
		else if ("replay".equals(args[0]))
		{
			launch("tod.impl.dbgrid.bench.GridReplay", new String[] {args[1]});
		}
		else if ("nodestore".equals(args[0]))
		{
			launch("tod.impl.dbgrid.bench.BenchDatabaseNode", new String[] {});
		}
		else if ("dispatch".equals(args[0]))
		{
			launch("tod.impl.dbgrid.bench.GridDispatch", new String[] {args[1], args[2]});
		}
		else if ("netbench".equals(args[0]))
		{
			launch("tod.impl.dbgrid.bench.NetBench", new String[] {args[1], args[2]});
		}
		else throw new RuntimeException("Not handled: "+args[0]);
	}
	
	private static void launch(String aClassName, String[] aArgs) throws Exception
	{
		Class theClass = Class.forName(aClassName);
		Method theMethod = theClass.getMethod("main", new Class[] {String[].class});
		theMethod.invoke(null, new Object[] {aArgs});
	}
}