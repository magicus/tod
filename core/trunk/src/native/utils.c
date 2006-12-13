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
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <vector>


#include "utils.h"

void fatal_error(char* message)
{
	printf(message);
	exit(-1);
}

void fatal_ioerror(char* message)
{
	perror(message);
	exit(-1);
}

void writeByte(FILE* f, int i)
{
	fputc(i & 0xff, f);
}

void writeShort(FILE* f, int v)
{
	fputc(0xff & (v >> 8), f);
	fputc(0xff & v, f);
}

void writeInt(FILE* f, int v)
{
	fputc(0xff & (v >> 24), f);
	fputc(0xff & (v >> 16), f);
	fputc(0xff & (v >> 8), f);
	fputc(0xff & v, f);
}

void writeLong(FILE* f, jlong v)
{
	fputc(0xff & (v >> 56), f);
	fputc(0xff & (v >> 48), f);
	fputc(0xff & (v >> 40), f);
	fputc(0xff & (v >> 32), f);
	fputc(0xff & (v >> 24), f);
	fputc(0xff & (v >> 16), f);
	fputc(0xff & (v >> 8), f);
	fputc(0xff & v, f);
}

int readByte(FILE* f)
{
	return fgetc(f);
}

int readShort(FILE* f)
{
	int a = fgetc(f);
	int b = fgetc(f);
	
	return (((a & 0xff) << 8) | (b & 0xff));
}

int readInt(FILE* f)
{
	int a = fgetc(f);
	int b = fgetc(f);
	int c = fgetc(f);
	int d = fgetc(f);
	
	return (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
}

void writeUTF(FILE* f, const char* s)
{
	int len = strlen(s);
	writeShort(f, len);
	fputs(s, f);
}

char* readUTF(FILE* f)
{
	int len = readShort(f);
	char* s = (char*) malloc(len+1);
	fread(s, 1, len, f);
	s[len] = 0;
	
	return s;
}

