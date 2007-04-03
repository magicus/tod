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

/**
 * A point in the universe
 * @author gpothier
 */
public class UPoint
{
	public static final UPoint ORIGIN = new UPoint(0, 0);
	
	public final float x;
	public final float y;
	
	public UPoint(float aX, float aY)
	{
		x = aX;
		y = aY;
	}
	
	public UPoint translate(float dx, float dy)
	{
		return new UPoint(x+dx, y+dy);
	}
	
	public UPoint translate(UVector v)
	{
		return new UPoint(x+v.dx, y+v.dy);
	}
	
	public float dist(UPoint p) 
	{
		return dist(this, p);
	}
	
	public float distSq(UPoint p) 
	{
		return distSq(this, p);
	}
	
	public static float dist(UPoint p1, UPoint p2) 
	{
		return (float) Math.sqrt(distSq(p1, p2));
	}
	
	public static float distSq(UPoint p1, UPoint p2) 
	{
		return (p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y);
	}
}
