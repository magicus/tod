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
package tod.vbuilder;

import java.util.List;

import zz.csg.api.constraints.Constraint;
import zz.csg.api.constraints.ConstraintParser;
import zz.csg.api.constraints.ConstraintSystem;
import zz.csg.api.constraints.IConstrainedDouble;
import zz.csg.impl.constraints.ConstrainedDouble;
import EDU.Washington.grad.gjb.cassowary.ClStrength;

public class BaseTreeLayout extends CellEventProcessor
implements ICellListener
{
	public static final double H_SIBLING_GAP = 1;
	public static final double H_COUSIN_GAP = 5;
	public static final double V_GAP = 3;
	
	
	public static final NodeAttribute<Constraint> _vAlignConstraint = 
		new NodeAttribute<Constraint> ("vAlignConstraint");
	
	public static final NodeAttribute<Constraint> _hAlignConstraint = 
		new NodeAttribute<Constraint> ("hAlignConstraint");
	
	public static final NodeAttribute<Constraint> _parentCenteringConstraint = 
		new NodeAttribute<Constraint> ("parentCenteringConstraint");
	
	public static final NodeAttribute<IConstrainedDouble> _childrenSpacingVar = 
		new NodeAttribute<IConstrainedDouble> ("childrenSpacingVar");
	
	public static final NodeAttribute<Constraint> _childrenSpacingVarConstraint = 
		new NodeAttribute<Constraint> ("childrenSpacingVarConstraint");

	private ConstraintSystem itsSystem = new ConstraintSystem();
	
	public void changed (List<CellEvent> aChangeSet)
	{
		CellEvent.dispatch (aChangeSet, this);
	}

	@Override
	public void nodeImported(IObjectNode aNode)
	{
		ConstraintParser cp = new ConstraintParser();
		cp.addVar ("x", aNode.pBounds().pX());
		cp.addVar ("y", aNode.pBounds().pY());
		
		itsSystem.addConstraint(cp.createConstraint("x >= 0"));
		itsSystem.addConstraint(cp.createConstraint("y >= 0"));
	}
	
	public void attributeChanged(IObjectNode aNode, String aAttributeName, Object aOldValue, Object aNewValue)
	{
		if (aAttributeName.equals(BaseTreeStructure._parent.getName())) parentChanged (
				aNode, 
				(IObjectNode) aOldValue, 
				(IObjectNode) aNewValue);
		
		else if (aAttributeName.equals (BaseTreeStructure._nextSibling.getName())) nextSiblingChanged (
				aNode, 
				(IObjectNode) aOldValue, 
				(IObjectNode) aNewValue);
		
		else if (aAttributeName.equals (BaseTreeStructure._firstChild.getName())) firstChildChanged (
				aNode, 
				(IObjectNode) aOldValue, 
				(IObjectNode) aNewValue);
		
		else if (aAttributeName.equals (BaseTreeStructure._lastChild.getName())) lastChildChanged (
				aNode, 
				(IObjectNode) aOldValue, 
				(IObjectNode) aNewValue);
	}
	
	private void parentChanged(IObjectNode aNode, IObjectNode aOldParent, IObjectNode aNewParent)
	{
		System.out.println("parentChanged");
				
		if (aOldParent != null)
		{
			Constraint vAlignConstraint = aOldParent.get(_vAlignConstraint);
			itsSystem.removeConstraint(vAlignConstraint);
			aOldParent.set(_vAlignConstraint, null);
		}
		
		if (aNewParent != null)
		{
			ConstraintParser cp = new ConstraintParser();
			cp.addVar  ("py", aNewParent.pBounds().pY());
			cp.addCnst ("ph", aNewParent.pBounds().pH());
			cp.addVar  ("ny", aNode.pBounds().pY());
			cp.addCnst ("VG", V_GAP);
		
			Constraint vAlignConstraint = cp.createConstraint("ny = py + ph + VG");
			itsSystem.addConstraint(vAlignConstraint);
			aNewParent.set(_vAlignConstraint, vAlignConstraint);
		}
	}
	
	private void nextSiblingChanged(IObjectNode aNode, IObjectNode aOldNextSibling, IObjectNode aNewNextSibling)
	{
		System.out.println("nextSiblingChanged");
				
		if (aOldNextSibling != null)
		{
			Constraint hAlignConstraint = aNode.get(_hAlignConstraint);
			itsSystem.removeConstraint(hAlignConstraint);
			aNode.set(_hAlignConstraint, null);
		}
		
		if (aNewNextSibling != null)
		{
			IObjectNode parent = aNode.get(BaseTreeStructure._parent);
			IConstrainedDouble childrenSpacingVar = parent.get(_childrenSpacingVar);
			
			ConstraintParser cp = new ConstraintParser();
			cp.addVar  ("nx", aNewNextSibling.pBounds().pX());
			cp.addVar  ("x", aNode.pBounds().pX());
			cp.addCnst  ("W", aNode.pBounds().pW());
		
			if (childrenSpacingVar == null)
			{
				childrenSpacingVar = new ConstrainedDouble();
				parent.set(_childrenSpacingVar, childrenSpacingVar);
				cp.addVar  ("l", childrenSpacingVar);		
				
				cp.addCnst ("HG", H_SIBLING_GAP);
				Constraint childrenSpacingVarConstraint = cp.createConstraint("l = HG", ClStrength.strong);
				itsSystem.addConstraint(childrenSpacingVarConstraint);
				parent.set(_childrenSpacingVarConstraint, childrenSpacingVarConstraint);
			}
			else 
			{
				cp.addVar  ("l", childrenSpacingVar);		
			}
		
			Constraint hAlignConstraint = cp.createConstraint("nx - (x+W) = l");
			itsSystem.addConstraint(hAlignConstraint);
			aNode.set(_hAlignConstraint, hAlignConstraint);
		}
	}
	
	private void firstChildChanged(IObjectNode aNode, IObjectNode aOld, IObjectNode aNew)
	{
		updateParentCenteringConstraint(aNode);
	}
	
	private void lastChildChanged(IObjectNode aNode, IObjectNode aOldLastChild, IObjectNode aNewLastChild)
	{
		System.out.println("lastChildChanged");
		
		if (aOldLastChild != null)
		{
			Constraint hAlignConstraint = aOldLastChild.get(_hAlignConstraint);
			
			if (hAlignConstraint != null)
			{
				itsSystem.removeConstraint(hAlignConstraint);
				aOldLastChild.set(_hAlignConstraint, null);
			}
		}
		
		if (aNewLastChild != null)
		{
			IObjectNode nextBFT = aNewLastChild.get(BaseTreeStructure._nextBFT);
			
			if (nextBFT != null)
			{
				ConstraintParser cp = new ConstraintParser();
				cp.addVar  ("nx", nextBFT.pBounds().pX());
				cp.addVar  ("x", aNewLastChild.pBounds().pX());
				cp.addCnst ("W", aNewLastChild.pBounds().pW());
				cp.addCnst ("HG", H_COUSIN_GAP);
				
				Constraint hAlignConstraint = cp.createConstraint("nx - (x+W) >= HG");
				itsSystem.addConstraint(hAlignConstraint);
				aNewLastChild.set(_hAlignConstraint, hAlignConstraint);
			}
		}

		updateParentCenteringConstraint(aNode);		
	}
	
	private void updateParentCenteringConstraint (IObjectNode aNode)
	{
		Constraint parentCenteringConstraint = aNode.get(_parentCenteringConstraint);
		if (parentCenteringConstraint != null) itsSystem.removeConstraint(parentCenteringConstraint);
		
		IObjectNode firstChild = aNode.get(BaseTreeStructure._firstChild);
		IObjectNode lastChild = aNode.get(BaseTreeStructure._lastChild);
		
		if (firstChild != null && lastChild != null)
		{
			ConstraintParser cp = new ConstraintParser();
			cp.addVar ("px", aNode.pBounds().pX());
			cp.addVar ("fx", firstChild.pBounds().pX());
			cp.addVar ("lx", lastChild.pBounds().pX());
			
			cp.addCnst("pw", aNode.pBounds().pW());
			cp.addCnst("lw", lastChild.pBounds().pW());

			parentCenteringConstraint = cp.createConstraint("px + pw/2 = (fx + lx + lw)/2");
			aNode.set(_parentCenteringConstraint, parentCenteringConstraint);
			itsSystem.addConstraint(parentCenteringConstraint);
		}
	}
}