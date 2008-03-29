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

import java.util.List;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IAspectInfo;
import tod.core.database.structure.SourceRange;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.dyncross.DynamicCrosscuttingView;
import tod.impl.database.structure.standard.AspectInfo;
import zz.utils.list.IList;
import zz.utils.list.ZArrayList;
import zz.utils.primitive.IntArray;
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
		public abstract int[] getAdviceSourceIds();
		public abstract void gotoSource(IGUIManager aGUIManager);
		
		/**
		 * Strips the package name from the given file name.
		 */
		protected String strippedName(String aFileName)
		{
			int i = aFileName.lastIndexOf('.');
			return aFileName.substring(i+1);
		}
	}
	
	public static class AspectHighlight extends Highlight
	{
		private final IAspectInfo itsAspect;

		public AspectHighlight(IAspectInfo aAspectInfo)
		{
			itsAspect = aAspectInfo;
		}
		
		public IAspectInfo getAspect()
		{
			return itsAspect;
		}
		
		@Override
		public int[] getAdviceSourceIds()
		{
			List<IAdviceInfo> theAdvices = itsAspect.getAdvices();
			int[] theResult = new int[theAdvices.size()];
			int i=0;
			for(IAdviceInfo theAdvice : theAdvices) theResult[i++] = theAdvice.getId();
			return theResult;
		}
		
		@Override
		public void gotoSource(IGUIManager aGUIManager)
		{
			aGUIManager.gotoSource(new SourceRange(itsAspect.getSourceFile(), 1));
		}

		@Override
		public String toString()
		{
			return strippedName(getAspect().getSourceFile());
		}
	}
	
	public static class AdviceHighlight extends Highlight
	{
		private final IAdviceInfo itsAdvice;

		public AdviceHighlight(IAdviceInfo aAdvice)
		{
			itsAdvice = aAdvice;
		}

		public SourceRange getAdviceSource()
		{
			return itsAdvice.getSourceRange();
		}
		
		@Override
		public int[] getAdviceSourceIds()
		{
			int[] theResult = new int[1];
			theResult[0] = itsAdvice.getId();
			return theResult;
		}

		@Override
		public void gotoSource(IGUIManager aGUIManager)
		{
			aGUIManager.gotoSource(getAdviceSource());
		}

		@Override
		public String toString()
		{
			return strippedName(getAdviceSource().sourceFile)+":"+getAdviceSource().startLine;
		}
	}
	

}
