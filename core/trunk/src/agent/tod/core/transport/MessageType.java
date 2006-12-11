/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.core.transport;

/**
 * @author gpothier
 */
public enum MessageType
{
	// Events
	INSTANTIATION,
	SUPER_CALL,
	METHOD_CALL,
	BEHAVIOR_EXIT,
	EXCEPTION,
	FIELD_WRITE,
	ARRAY_WRITE,
	LOCAL_VARIABLE_WRITE,
	OUTPUT,
	
	//Arguments
	OBJECT_ARRAY, SIMPLE_OBJECT, NONE,
	
	// Argument values
	NULL, BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, REGISTERED, 
	OBJECT_UID, OBJECT_HASH,
	
	// Registering
	REGISTER_CLASS, REGISTER_BEHAVIOR, REGISTER_FIELD, 
	REGISTER_FILE, REGISTER_THREAD, REGISTER_LOCAL_VARIABLE,
    REGISTER_BEHAVIOR_ATTRIBUTES
}
