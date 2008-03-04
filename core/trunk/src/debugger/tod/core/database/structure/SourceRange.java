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
package tod.core.database.structure;

import java.io.Serializable;

/**
 * Denotes a range of code in a source file.
 * Corresponds to Soot's Position class
 * @author gpothier
 */
public class SourceRange implements Serializable
{
	private static final long serialVersionUID = 3583352414851419066L;

	public final String sourceFile;
	public final int startLine;
	public final int startColumn;
	public final int endLine;
	public final int endColumn;
	
	/**
	 * Creates a source range for a single line
	 */
	public SourceRange(String aSourceFile, int aLine)
	{
		this(aSourceFile, aLine, 1, aLine, 1);
	}
	
	public SourceRange(String aSourceFile, int aStartLine, int aStartColumn, int aEndLine, int aEndColumn)
	{
		sourceFile = aSourceFile;
		startLine = aStartLine;
		startColumn = aStartColumn;
		endLine = aEndLine;
		endColumn = aEndColumn;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s:%d (%d,%d-%d,%d)", sourceFile, startLine, startLine, startColumn, endLine, endColumn);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + endColumn;
		result = prime * result + endLine;
		result = prime * result + ((sourceFile == null) ? 0 : sourceFile.hashCode());
		result = prime * result + startColumn;
		result = prime * result + startLine;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final SourceRange other = (SourceRange) obj;
		if (endColumn != other.endColumn) return false;
		if (endLine != other.endLine) return false;
		if (sourceFile == null)
		{
			if (other.sourceFile != null) return false;
		}
		else if (!sourceFile.equals(other.sourceFile)) return false;
		if (startColumn != other.startColumn) return false;
		if (startLine != other.startLine) return false;
		return true;
	}
}