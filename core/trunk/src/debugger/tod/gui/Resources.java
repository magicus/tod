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
	
	public static final ImageResource ICON_FULL_OBLIVIOUISNESS = loadIcon("fullObliviousness.png");
	
	private static ImageResource loadIcon (String aName)
	{
		return ResourceUtils.loadImageResource(Resources.class, aName);
	}
	
}
