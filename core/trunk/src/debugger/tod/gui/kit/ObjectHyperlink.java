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
package tod.gui.kit;

import java.awt.Color;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.JobProcessor;
import tod.gui.SVGHyperlink;
import tod.gui.Hyperlinks.ISeedFactory;
import tod.gui.JobProcessor.IJobListener;
import tod.gui.JobProcessor.Job;
import tod.gui.seed.Seed;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.figures.IGOFlowText.DefaultSizeComputer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

/**
 * A hyperlink representing an object.
 * Object details are fetched asynchronously through the provided
 * {@link JobProcessor}.
 * @author gpothier
 */
public class ObjectHyperlink extends SVGHyperlink
implements IJobListener<ITypeInfo>
{
	private final ObjectId itsObject;

	public ObjectHyperlink(
			Seed aSeed,
			final ILogBrowser aLogBrowser,
			JobProcessor aJobProcessor,
			ObjectId aObject, 
			XFont aFont)
	{
		itsObject = aObject;
		
		setSeed(aSeed);
		pStrokePaint().set(Color.BLUE);
		pFont().set(aFont);
		setSizeComputer(DefaultSizeComputer.getInstance());
		
		Job<ITypeInfo> theJob = new Job<ITypeInfo>()
		{
			@Override
			public ITypeInfo run()
			{
				return aLogBrowser.createObjectInspector(itsObject).getType();
			}
		};
		
		aJobProcessor.submit(theJob, this);

		String theText = "... (" + aObject + ")";
		pText().set(theText);
	}

	public void jobFinished(ITypeInfo aType)
	{
		String theText = aType.getName() + " (" + itsObject + ")";
		pText().set(theText);
	}
}
