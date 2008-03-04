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
	
	public static UPoint random(float w, float h)
	{
		return new UPoint((float) (Math.random()*w), (float) (Math.random()*h));
	}
}
