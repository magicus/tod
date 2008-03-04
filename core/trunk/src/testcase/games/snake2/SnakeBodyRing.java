/*
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
package games.snake2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class SnakeBodyRing extends FreeEntity implements ISnakePart
{
	private static final float BASE_RADIUS = 10;
	private static final Color COLOR = Color.BLUE;
	
	/**
	 * Rank of this ring in the body
	 */
	private int itsRank;
	
	private float itsRadius = BASE_RADIUS;
	
	private Spring itsHeadSpring;
	private Spring itsTailSpring = new Spring(this, BASE_RADIUS, SPRING_K);

	public SnakeBodyRing(Universe aUniverse, int aRank)
	{
		super(aUniverse);
		itsRank = aRank;
	}
	
	public Spring getTailSpring()
	{
		return itsTailSpring;
	}
	
	public void connect(ISnakePart aSnakePart)
	{
		aSnakePart.getTailSpring().setEntity2(this, itsRadius);
		itsHeadSpring = aSnakePart.getTailSpring();
	}
	
	@Override
	public void updatePosition()
	{
		if (isLastRing())
		{
			float w = getUniverse().getEnnemyWeightAt(getPos(), itsRadius+2f);
			itsRadius -= w*0.01f;
			
			if (itsRadius < 1f) getUniverse().ringLost(this);
			
			itsRadius = Math.min(BASE_RADIUS, itsRadius+0.01f); 
		}
		super.updatePosition();
	}
	
	protected boolean isLastRing()
	{
		return itsRank == getUniverse().getSnakeSize();
	}

	@Override
	public UVector getForceFor(String aField, Entity e)
	{
		if ("matter".equals(aField))
		{
			return ForceUtils.repell(getPos(), itsRadius, e.getPos());
		}
		else if ("snakeTail".equals(aField) && isLastRing())
		{
			UVector v = UVector.create(getPos(), e.getPos());
			float norm = v.norm();
			float d = norm-itsRadius;
			float sgn = d/Math.abs(d);
			return norm > itsRadius*5 ?
					v.mult(-100f*sgn/(float) Math.pow(norm, 2f))
					: v.mult(-1f*sgn/norm);
		}
		else return super.getForceFor(aField, e);
	}

	@Override
	protected UVector getForces()
	{
		return UVector.sum(
				getUniverse().getForce("matter", this),
//				getUniverse().getForce(itsPrevious, this),
				itsHeadSpring != null ? itsHeadSpring.getForce2() : UVector.NULL,
//				itsTailSpring.getForce1(),
				viscosity(0.85f)
				);
	}

	@Override
	public void draw(Graphics2D g)
	{
		UPoint p = getPos();
		DrawUtils.drawBall(g, p, itsRadius, COLOR);
	}
	
	
}
