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