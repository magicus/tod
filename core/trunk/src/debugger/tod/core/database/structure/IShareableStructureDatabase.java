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
package tod.core.database.structure;

import java.util.Map;

import tod.impl.database.structure.standard.TagMap;

/**
 * Not to be used by clients.
 * Provides additional methods used to synchronize remote structure databases.
 * @author gpothier
 */
public interface IShareableStructureDatabase extends IMutableStructureDatabase
{
	public byte[] _getClassBytecode(int aClassId);
	public Map<String, IMutableFieldInfo> _getClassFieldMap(int aClassId);
	public Map<String, IMutableBehaviorInfo> _getClassBehaviorsMap(int aClassId);
	public Map<Integer, SourceRange> _getClassAdviceSourceMap(int aClassId);
	public LocalVariableInfo[] _getBehaviorLocalVariableInfo(int aBehaviorId);
	public LineNumberInfo[] _getBehaviorLineNumberInfo(int aBehaviorId);
	public TagMap _getBehaviorTagMap(int aBehaviorId);
	public IClassInfo _getBehaviorClass(int aBehaviorId, boolean aFailIfAbsent);
	public IClassInfo _getFieldClass(int aFieldId, boolean aFailIdAbsent);
}
