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
		return new ImageResource(ResourceUtils.loadImageResource(Resources.class, aName));
	}
	
	public static class ImageResource
	{
		private BufferedImage itsImage;

		public ImageResource(BufferedImage aImage)
		{
			itsImage = aImage;
		}
		
		public ImageIcon asIcon(int aSize)
		{
			int theWidth = itsImage.getWidth();
			int theHeight = itsImage.getHeight();
			int theSide = Math.max(theWidth, theHeight);
			
			if (theSide != aSize)
			{
				float theRatio = 1f * aSize / theSide;
				
				Image theImage = itsImage.getScaledInstance(
						(int) (theWidth*theRatio), 
						(int) (theHeight*theRatio), 
						Image.SCALE_SMOOTH);
				
				return new ImageIcon(theImage);
			}
			else return new ImageIcon(itsImage);
		}
		
		/**
		 * Returns an icon of the specified height.
		 */
		public ImageIcon asIconH(int aHeight)
		{
			int theWidth = itsImage.getWidth();
			int theHeight = itsImage.getHeight();
			
			if (theHeight != aHeight)
			{
				float theRatio = 1f * aHeight / theHeight;
				
				Image theImage = itsImage.getScaledInstance(
						(int) (theWidth*theRatio), 
						(int) (theHeight*theRatio), 
						Image.SCALE_SMOOTH);
				
				return new ImageIcon(theImage);
			}
			else return new ImageIcon(itsImage);
		}
		
		/**
		 * Returns an icon of the specified width.
		 */
		public ImageIcon asIconW(int aWidth)
		{
			int theWidth = itsImage.getWidth();
			int theHeight = itsImage.getHeight();
			
			if (theWidth != aWidth)
			{
				float theRatio = 1f * aWidth / theWidth;
				
				Image theImage = itsImage.getScaledInstance(
						(int) (theWidth*theRatio), 
						(int) (theHeight*theRatio), 
						Image.SCALE_SMOOTH);
				
				return new ImageIcon(theImage);
			}
			else return new ImageIcon(itsImage);
		}
	}

}
