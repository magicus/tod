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
package tod.impl.local.filter;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Filter for all events that have a specific target.
 * @author gpothier
 */
public class TargetFilter extends AbstractStatelessFilter
{
	private ObjectId itsTarget;
	
	public TargetFilter(LocalBrowser aBrowser, ObjectId aTarget)
	{
		super (aBrowser);
		itsTarget = aTarget;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		Object theTarget;
		
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			theTarget = theEvent.getTarget();
		}
		else
		{
			IBehaviorCallEvent theParent = aEvent.getParent();
			theTarget = theParent != null ? theParent.getTarget() : null;
		}
		
		return itsTarget == null || itsTarget.equals(theTarget);
		
	}
}
