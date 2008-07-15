/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.seed;

import java.awt.Color;
import java.util.Set;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.gui.view.LogView;
import tod.gui.view.dyncross.DynamicCrosscuttingView;
import zz.utils.list.IList;
import zz.utils.list.ZArrayList;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

public class DynamicCrosscuttingSeed extends LogViewSeed
{
	public final IList<Highlight> pHighlights = new ZArrayList<Highlight>();
	public final IRWProperty<Long> pStart = new SimpleRWProperty<Long>();
	public final IRWProperty<Long> pEnd = new SimpleRWProperty<Long>();

	public DynamicCrosscuttingSeed(ILogBrowser aLog)
	{
		super(aLog);
	}

	@Override
	public Class< ? extends LogView> getComponentClass()
	{
		return DynamicCrosscuttingView.class;
	}
	
	@Override
	public String getKindDescription()
	{
		return "Dynamic crosscutting";
	}

	@Override
	public String getShortDescription()
	{
		return null;
	}

	/**
	 * Represents any kind of Aspect stuff that can be highlighted (full aspect,
	 * or individual advice).
	 * @author gpothier
	 */
	public static class Highlight
	{
		private final Color itsColor;
		private final Set<BytecodeRole> itsRoles;
		
		/**
		 * The highlighted location (should be {@link IAdviceInfo} or {@link IAspectInfo}
		 */
		private final ILocationInfo itsLocation;
		
		public Highlight(Color aColor, Set<BytecodeRole> aRoles, ILocationInfo aLocation)
		{
			itsColor = aColor;
			itsRoles = aRoles;
			itsLocation = aLocation;
		}

		public Color getColor()
		{
			return itsColor;
		}
		
		public Set<BytecodeRole> getRoles()
		{
			return itsRoles;
		}
		
		public ILocationInfo getLocation()
		{
			return itsLocation;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itsColor == null) ? 0 : itsColor.hashCode());
			result = prime * result + ((itsLocation == null) ? 0 : itsLocation.hashCode());
			result = prime * result + ((itsRoles == null) ? 0 : itsRoles.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final Highlight other = (Highlight) obj;
			if (itsColor == null)
			{
				if (other.itsColor != null) return false;
			}
			else if (!itsColor.equals(other.itsColor)) return false;
			if (itsLocation == null)
			{
				if (other.itsLocation != null) return false;
			}
			else if (!itsLocation.equals(other.itsLocation)) return false;
			if (itsRoles == null)
			{
				if (other.itsRoles != null) return false;
			}
			else if (!itsRoles.equals(other.itsRoles)) return false;
			return true;
		}
	}
}
