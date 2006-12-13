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
#ifndef _Included_utils_h
#define _Included_utils_h

#include <stdio.h>
#include <jni.h>


void fatal_error(char*);
void fatal_ioerror(char*);

void writeByte(FILE* f, int i);
void writeShort(FILE* f, int v);
void writeInt(FILE* f, int v);
void writeLong(FILE* f, jlong v);
int readByte(FILE* f);
int readShort(FILE* f);
int readInt(FILE* f);
void writeUTF(FILE* f, const char* s);
char* readUTF(FILE* f);


#endif
