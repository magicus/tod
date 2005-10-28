/*
 * Created on Sep 29, 2004
 */
package reflex.lib.logging.test;

import reflex.Run;
import reflex.core.LogLevel;
import reflex.lib.logging.core.api.config.StaticConfig;
import reflex.tools.selectors.WorkingSetClassSelector;
import tod.core.PrintLogCollector;
import tod.core.transport.SocketCollector;


public class Main
{
	public static void main(String[] args) throws Throwable  
	{
		new Main().test();
	}
	
    public void test() throws Throwable
    {
        LogLevel.set(LogLevel.VERBOSE);
        StaticConfig.getInstance().setLoggingClassSelector(new WorkingSetClassSelector("[+reflex.lib.logging.test.**]"));
//        StaticConfig.getInstance().getIdentifiedClasses().addPackage("reflex.lib.logging.test", true);
        
        StaticConfig.getInstance().setLogFieldWrite(true);
        StaticConfig.getInstance().setLogLocalVariableWrite(true);
        StaticConfig.getInstance().setLogInstantiations(true);
        StaticConfig.getInstance().setLogParameters(true);
        StaticConfig.getInstance().setLogMethods(true);
        
//        SocketCollector theCollector = new SocketCollector("localhost", 4012);
        PrintLogCollector theCollector = new PrintLogCollector();
		StaticConfig.getInstance().setLogCollector(theCollector);
        Run.main(
                new String[]{
                	"--working-set", "[+reflex.lib.logging.test.** +java.util.**]",
                    "-lp", "class:reflex.lib.logging.core.impl.mop.Config",
                    "reflex.lib.logging.test.LoggingTest"});
    }
}
