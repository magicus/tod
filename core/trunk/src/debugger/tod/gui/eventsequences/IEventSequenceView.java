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
package tod.gui.eventsequences;

import java.awt.Image;
import java.util.Collection;

import javax.swing.JComponent;

import tod.gui.eventsequences.mural.EventMural;

import zz.utils.ItemAction;
import zz.utils.properties.IRWProperty;

/**
 * A view of an horizontal event sequence.
 * Each indivudual components of the view must be requested
 * through corresponding methods. For instance, to obtain the main
 * graphic object (the one that displays the events), use
 * {@link #getEventStripe()};
 * for obtaining the available actions, use {@link #getActions()}.
 * @author gpothier
 */
public interface IEventSequenceView 
{
	/**
	 * Starting timestamp of the displayed time range.
	 */
	public IRWProperty<Long> pStart ();
	
	/**
	 * Ending timestamp of the displayed time range.
	 */
	public IRWProperty<Long> pEnd ();
	
	/**
	 * Sets the timestamp bounds of this view.  
	 */
	public void setLimits(long aFirstTimestamp, long aLastTimestamp);	
	
	/**
	 * Returns the horizontal stripe that displays events.
	 */
	public EventMural getEventStripe();
	
	/**
	 * Returns a collection of available actions for this sequence view.
	 */
	public Collection<ItemAction> getActions();
	
	/**
	 * Returns an icon representing this sequence view.
	 */
	public Image getIcon();
	
	/**
	 * Returns the title of this sequence view.
	 */
	public String getTitle();
	
	/**
	 * Returns the timestamp of the first event in this view.
	 */
	public long getFirstTimestamp();
	
	/**
	 * Returns the timestamp of the last event in this view.
	 */
	public long getLastTimestamp();

}
