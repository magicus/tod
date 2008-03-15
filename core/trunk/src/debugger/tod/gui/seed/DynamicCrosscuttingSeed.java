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

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.AspectInfo;
import tod.gui.IGUIManager;
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

	public DynamicCrosscuttingSeed(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
	}

	@Override
	protected LogView requestComponent()
	{
		DynamicCrosscuttingView theView = new DynamicCrosscuttingView(getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
	}
	
	/**
	 * Represents any kind of Aspect stuff that can be highlighted (full aspect,
	 * or individual advice).
	 * @author gpothier
	 */
	public static abstract class Highlight
	{
		public abstract IEventBrowser createBrowser(ILogBrowser aLogBrowser);
		public abstract void gotoSource(IGUIManager aGUIManager);
	}
	
	public static class AspectHighlight extends Highlight
	{
		private final AspectInfo itsAspectInfo;

		public AspectHighlight(AspectInfo aAspectInfo)
		{
			itsAspectInfo = aAspectInfo;
		}
		
		public AspectInfo getAspectInfo()
		{
			return itsAspectInfo;
		}
		
		@Override
		public IEventBrowser createBrowser(ILogBrowser aLogBrowser)
		{
			ICompoundFilter theUnionFilter = aLogBrowser.createUnionFilter();
			for (int theSourceId : itsAspectInfo.getAdviceIds())
			{
				theUnionFilter.add(aLogBrowser.createAdviceSourceIdFilter(theSourceId));
			}
			return aLogBrowser.createBrowser(theUnionFilter);
		}
		
		@Override
		public void gotoSource(IGUIManager aGUIManager)
		{
			aGUIManager.gotoSource(new SourceRange(itsAspectInfo.getSourceFile(), 1));
		}

		@Override
		public String toString()
		{
			return getAspectInfo().getSourceFile();
		}
	}
	
	public static class AdviceHighlight extends Highlight
	{
		private final int itsAdviceSourceId;
		private final SourceRange itsAdviceSource;


		public AdviceHighlight(int aAdviceSourceId, SourceRange aAdviceSource)
		{
			itsAdviceSourceId = aAdviceSourceId;
			itsAdviceSource = aAdviceSource;
		}

		public SourceRange getAdviceSource()
		{
			return itsAdviceSource;
		}
		
		@Override
		public IEventBrowser createBrowser(ILogBrowser aLogBrowser)
		{
			IEventFilter theFilter = aLogBrowser.createAdviceSourceIdFilter(itsAdviceSourceId);
			return aLogBrowser.createBrowser(theFilter);
		}

		@Override
		public void gotoSource(IGUIManager aGUIManager)
		{
			aGUIManager.gotoSource(itsAdviceSource);
		}

		@Override
		public String toString()
		{
			return getAdviceSource().toString();
		}
	}
	

}
