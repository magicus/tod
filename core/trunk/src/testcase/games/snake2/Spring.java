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
package games.snake2;

public class Spring
{
	private Entity itsEntity1;
	private float itsRadius1;

	private Entity itsEntity2;
	private float itsRadius2;
	
	private float itsK;

	public Spring(Entity aEntity1, float aRadius1, Entity aEntity2, float aRadius2, float k)
	{
		itsEntity1 = aEntity1;
		itsRadius1 = aRadius1;
		itsEntity2 = aEntity2;
		itsRadius2 = aRadius2;
		itsK = k;
	}
	
	public Spring(Entity aEntity1, float aRadius1, float aK)
	{
		itsEntity1 = aEntity1;
		itsRadius1 = aRadius1;
		itsK = aK;
	}

	public void setEntity2(Entity aEntity2, float aRadius2)
	{
		itsEntity2 = aEntity2;
		itsRadius2 = aRadius2;
	}

	public UVector getForce1()
	{
		if (itsEntity1 == null || itsEntity2 == null) return UVector.NULL;
		UVector v = UVector.create(itsEntity1.getPos(), itsEntity2.getPos());
		float norm = v.norm();
		final float D = 1f; 
		final float D3 = D*D*D;
		float d = norm - (itsRadius1+itsRadius2);
		float sgn = d > 0 ? 1f : -1f;
//		float f = Math.abs(d) > D ? d - (D-1)*sgn : d*d*d/D3;
		float f = d;
		return v.mult(itsK*f/norm);
	}
	
	public UVector getForce2()
	{
		return getForce1().mult(-1f);
	}
	
}
