/*
 * Created on Oct 15, 2006
 */
package tod.gui;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import zz.utils.ui.ResourceUtils;

public class Resources
{
//	public static final ImageIcon ICON_FORWARD_STEP_INTO = loadIcon("forwardStepInto.png");
//	public static final ImageIcon ICON_BACKWARD_STEP_INTO = loadIcon("backwardStepInto.png");
//	public static final ImageIcon ICON_FORWARD_STEP_OVER = loadIcon("forwardStepOver.png");
//	public static final ImageIcon ICON_BACKWARD_STEP_OVER = loadIcon("backwardStepOver.png");
//	public static final ImageIcon ICON_STEP_OUT = loadIcon("stepOut.png");
	
	private static ImageIcon loadIcon (String aName)
	{
		return ResourceUtils.loadIconResource(Resources.class, aName);
	}
	
	private static BufferedImage loadImage (String aName)
	{
		return ResourceUtils.loadImageResource(Resources.class, aName);
	}

}
