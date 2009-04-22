/*
 * Created on Oct 26, 2005
 */
package tod.impl.bci.asm;

import java.net.URI;

import tod.impl.local.LocalCollector;

/**
 * A session that uses a {@link tod.impl.local.LocalCollector}
 * and {@link tod.impl.bci.asm.ASMInstrumenter}.
 * @author gpothier
 */
//public class ASMLocalSession extends ASMSession
//{
//	public ASMLocalSession(
//			URI aUri, 
//			String aGlobalWorkingSet,
//			String aIdentificationWorkingSet,
//			String aTraceWorkingSet)
//	{
//		this(
//				aUri, 
//				aGlobalWorkingSet, 
//				aIdentificationWorkingSet,
//				aTraceWorkingSet,
//				new LocalCollector(null));
//	}
//	
//	private ASMLocalSession(
//			URI aUri, 
//			String aGlobalWorkingSet,
//			String aIdentificationWorkingSet,
//			String aTraceWorkingSet,
//			LocalCollector aCollector)
//	{
//		super(
//				aUri,
//				aGlobalWorkingSet,
//				aIdentificationWorkingSet,
//				aTraceWorkingSet,
//				aCollector,
//				aCollector,
//				aCollector);
//	}
//}
