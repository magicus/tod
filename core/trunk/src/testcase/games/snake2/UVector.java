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
 * A vector in the universe
 * @author gpothier
 */
public class UVector
{
	public static final UVector NULL = new UVector(0, 0);
	
	public final float dx;
	public final float dy;
	
	public UVector(float aDx, float aDy)
	{
		dx = aDx;
		dy = aDy;
	}
	
	/**
	 * Returns the norm (length) of this vector.
	 */
	public float norm() 
	{
		return (float) Math.sqrt(normSq());
	}
	
	/**
	 * Returns the squared norm (length) of this vector.
	 */
	public float normSq() 
	{
		return dx*dx + dy*dy;
	}

	/**
	 * Returns a new vector with coordinates multiplied by k
	 */
	public UVector mult(float k)
	{
		return new UVector(k*dx, k*dy);
	}
	
	public UVector add(UVector v) 
	{
		return new UVector(dx+v.dx, dy+v.dy);
	}
	
	/**
	 * Returns a unit-length vector with the same direction as this one.
	 */
	public UVector unit()
	{
		return mult(1f/norm());
	}
	
	/**
	 * Creates the vector from p1 to p2.
	 */
	public static UVector create(UPoint p1, UPoint p2)
	{
		return new UVector(p2.x-p1.x, p2.y-p1.y);
	}
	
	public static UVector radial(float norm, float angle)
	{
		return new UVector(
				(float) (norm*Math.cos(angle)), 
				(float) (norm*Math.sin(angle)));
	}
	
	/**
	 * Returns the sum of all specified vectors.
	 */
	public static UVector sum(UVector... vects)
	{
		float dx = 0;
		float dy = 0;
		for (UVector v : vects)
		{
			dx += v.dx;
			dy += v.dy;
		}
		return new UVector(dx, dy);
	}
}
