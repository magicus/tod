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
package tod.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;

import zz.utils.ui.GridStackLayout;
import zz.utils.ui.StackLayout;
import zz.utils.ui.ZLabel;
import zz.utils.ui.ResourceUtils.ImageResource;
import zz.utils.ui.text.XFont;

/**
 * Utility methods for creating common SVG components
 * @author gpothier
 */
public class GUIUtils
{
	public static Map<BytecodeRole, ImageResource> itsRoleIconMap;
	
	public static JComponent createMessage(
			String aHeader, 
			Color aHeaderColor,
			String aText, 
			Color aTextColor)
	{
		// Create container
		JPanel theContainer = new JPanel(new GridStackLayout(1, 0, 0, false, false));
		
		if (aHeader != null && aHeader.length() > 0)theContainer.add(ZLabel.create(aHeader, FontConfig.STD_HEADER_FONT, aHeaderColor));
		if (aText != null && aText.length() > 0) theContainer.add(ZLabel.create(aText, FontConfig.STD_FONT, aTextColor));

		return theContainer;
	}
	
	/**
	 * Creates a standard sequence layout.
	 */
	public static FlowLayout createSequenceLayout()
	{
		return new FlowLayout(FlowLayout.LEFT, 0, 0);
	}
	
	/**
	 * Creates a standard stack layout (ie children are stacked vertically,
	 * not like {@link StackLayout}).
	 */
	public static LayoutManager createStackLayout()
	{
		return new GridStackLayout(1, 0, 0, true, false);
	}
	
	/**
	 * Creates a standard border layout.
	 */
	public static LayoutManager createBorderLayout()
	{
		return new BorderLayout(0, 0);
	}
	
	/**
	 * return a label that delegates mouse events to its parents 
	 */
	public static ZLabel createLabel(String aText, XFont aFont, Color aColor)
	{
		ZLabel theLabel = ZLabel.create(aText, aFont, aColor);
		return theLabel;
	}
	
	public static ZLabel createLabel(String aText)
	{
		return createLabel(aText, FontConfig.STD_FONT, Color.BLACK);
	}
	
	/**
	 * Returns the icon corresponding to a given bytecode role.
	 */
	public static ImageResource getRoleIcon(BytecodeRole aRole)
	{
		if (itsRoleIconMap == null)
		{
			itsRoleIconMap = new HashMap<BytecodeRole, ImageResource>();
			itsRoleIconMap.put(BytecodeRole.ADVICE_ARG_SETUP, Resources.ICON_ROLE_CONTEXT_EXPOSURE);
			itsRoleIconMap.put(BytecodeRole.ADVICE_EXECUTE, Resources.ICON_ROLE_ADVICE_EXECUTION);
			itsRoleIconMap.put(BytecodeRole.ADVICE_TEST, Resources.ICON_ROLE_RESIDUE_EVALUATION);
			itsRoleIconMap.put(BytecodeRole.INLINED_ADVICE, Resources.ICON_ROLE_ADVICE_EXECUTION);
		}
		
		return itsRoleIconMap.get(aRole);
	}
}
