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
package games.snake2;

import java.awt.Color;
import java.awt.Graphics2D;

public class Apple extends FreeEntity
{
	private static final float RADIUS = 15;
	
	private static final int RIPE_AGE = 100;
	private static final int ROTTEN_AGE = 500;
	private static final int BLAST_AGE = 1000;

	private int itsAge;

	public Apple(Universe aUniverse)
	{
		super(aUniverse);
	}

	@Override
	public void updatePosition()
	{
		itsAge++;
		if (itsAge > BLAST_AGE) getUniverse().appleBlast(this);
		super.updatePosition();
	}
	
	@Override
	protected UVector getForces()
	{
		return UVector.NULL;
	}

	@Override
	public void draw(Graphics2D g)
	{
		if (itsAge < RIPE_AGE)
		{
			// Not ripe
			DrawUtils.drawBall(g, getPos(), RADIUS, Color.GREEN);
		}
		else if (itsAge < ROTTEN_AGE)
		{
			// Ripe
			DrawUtils.drawBall(g, getPos(), RADIUS, Color.RED);
		}
		else
		{
			// Rotten
			DrawUtils.drawBall(g, getPos(), RADIUS, Color.BLACK);
		}
	}

	public void eat()
	{
		if (itsAge < RIPE_AGE)
		{
			// Not ripe
		}
		else if (itsAge < ROTTEN_AGE)
		{
			// Ripe
			getUniverse().appleEat(this);
		}
		else
		{
			// Rotten
			getUniverse().appleBlast(this);
		}
	}
}
