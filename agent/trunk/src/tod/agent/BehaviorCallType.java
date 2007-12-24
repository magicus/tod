/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.agent;

import tod.agent.EventInterpreter.ThreadData;

/**
 * Defines the various type of possible behavior call
 * @author gpothier
 */
public enum BehaviorCallType 
{
	METHOD_CALL()
	{
		public <T extends ThreadData> void call(
				HighLevelCollector<T> aCollector,
				T aThread, 
				long aParentTimestamp,
				short aDepth,
				long aTimestamp, 
				long aOperationLocation,
				boolean aDirectParent,
				int aCalledBehavior,
				int aExecutedBehavior,
				Object aTarget,
				Object[] aArguments)
		{
			aCollector.methodCall(
					aThread,
					aParentTimestamp,
					aDepth, 
					aTimestamp,
					aOperationLocation,
					aDirectParent, 
					aCalledBehavior, 
					aExecutedBehavior, 
					aTarget,
					aArguments);
		}
	}, 
	SUPER_CALL()
	{
		public <T extends ThreadData> void call(
				HighLevelCollector<T> aCollector,
				T aThread, 
				long aParentTimestamp,
				short aDepth,
				long aTimestamp, 
				long aOperationLocation,
				boolean aDirectParent,
				int aCalledBehavior,
				int aExecutedBehavior,
				Object aTarget,
				Object[] aArguments)
		{
			aCollector.superCall(
					aThread,
					aParentTimestamp,
					aDepth, 
					aTimestamp,
					aOperationLocation,
					aDirectParent, 
					aCalledBehavior, 
					aExecutedBehavior, 
					aTarget,
					aArguments);
		}
	}, 
	INSTANTIATION()
	{
		public <T extends ThreadData> void call(
				HighLevelCollector<T> aCollector,
				T aThread, 
				long aParentTimestamp,
				short aDepth,
				long aTimestamp, 
				long aOperationLocation,
				boolean aDirectParent,
				int aCalledBehavior,
				int aExecutedBehavior,
				Object aTarget,
				Object[] aArguments)
		{
			aCollector.instantiation(
					aThread,
					aParentTimestamp,
					aDepth, 
					aTimestamp,
					aOperationLocation,
					aDirectParent, 
					aCalledBehavior, 
					aExecutedBehavior, 
					aTarget,
					aArguments);
		}
	};
	
	/**
	 * Performs the appropriate call on the specified collector.
	 */
	public abstract <T extends ThreadData> void call(
			HighLevelCollector<T> aCollector,
			T aThread,
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			long aOperationLocation,
			boolean aDirectParent,
			int aCalledBehavior,
			int aExecutedBehavior,
			Object aTarget,
			Object[] aArguments);
	
}
