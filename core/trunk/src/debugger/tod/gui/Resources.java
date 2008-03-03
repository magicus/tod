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
package tod.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import zz.utils.ui.ResourceUtils;
import zz.utils.ui.ResourceUtils.ImageResource;

public class Resources
{
	public static final ImageResource ICON_FORWARD_STEP_INTO = loadIcon("forwardStepInto.png");
	public static final ImageResource ICON_BACKWARD_STEP_INTO = loadIcon("backwardStepInto.png");
	public static final ImageResource ICON_FORWARD_STEP_OVER = loadIcon("forwardStepOver.png");
	public static final ImageResource ICON_BACKWARD_STEP_OVER = loadIcon("backwardStepOver.png");
	public static final ImageResource ICON_STEP_OUT = loadIcon("stepOut.png");
		
	public static final ImageResource ICON_ROLE_ADVICE_EXECUTION = loadIcon("roleAdviceExecution.png");
	public static final ImageResource ICON_ROLE_ASPECT_INSTANCE_SELECTION = loadIcon("roleAspectInstanceSelection.png");
	public static final ImageResource ICON_ROLE_CONTEXT_EXPOSURE = loadIcon("roleContextExposure.png");
	public static final ImageResource ICON_ROLE_RESIDUE_EVALUATION = loadIcon("roleResidueEvaluation.png");
	
	private static ImageResource loadIcon (String aName)
	{
		return ResourceUtils.loadImageResource(Resources.class, aName);
	}
	
}
