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
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include <vector>


#include "utils.h"

void fatal_error(char* message)
{
	fprintf(stderr, "FATAL ERROR, ABORTING: ");
	fprintf(stderr, message);
	fprintf(stderr, "\n");
	exit(-1);
}

void fatal_ioerror(char* message)
{
	perror(message);
	exit(-1);
}

void writeByte(STREAM* f, int i)
{
	f->put((char) (i & 0xff));
}

void writeShort(STREAM* f, int v)
{
	char buf[2];
	buf[0] = 0xff & (v >> 8);
	buf[1] = 0xff & v;
	f->write(buf, 2);
}

void writeInt(STREAM* f, int v)
{
	char buf[4];
	buf[0] = 0xff & (v >> 24);
	buf[1] = 0xff & (v >> 16);
	buf[2] = 0xff & (v >> 8);
	buf[3] = 0xff & v;
	f->write(buf, 4);
}

void writeLong(STREAM* f, jlong v)
{
	char buf[8];
	buf[0] = 0xff & (v >> 56);
	buf[1] = 0xff & (v >> 48);
	buf[2] = 0xff & (v >> 40);
	buf[3] = 0xff & (v >> 32);
	buf[4] = 0xff & (v >> 24);
	buf[5] = 0xff & (v >> 16);
	buf[6] = 0xff & (v >> 8);
	buf[7] = 0xff & v;
	f->write(buf, 8);
}

int readByte(STREAM* f)
{
	return f->get();
}

int readShort(STREAM* f)
{
	char buf[2];
	f->read(buf, 2);
	
	return (((buf[0] & 0xff) << 8) | (buf[1] & 0xff));
}

int readInt(STREAM* f)
{
	char buf[4];
	f->read(buf, 4);
	
	return (((buf[0] & 0xff) << 24) 
		| ((buf[1] & 0xff) << 16) 
		| ((buf[2] & 0xff) << 8) 
		| (buf[3] & 0xff));
}

void writeUTF(STREAM* f, const char* s)
{
	int len = strlen(s);
	writeShort(f, len);
	f->write(s, len);
}

char* readUTF(STREAM* f)
{
	int len = readShort(f);
	char* s = (char*) malloc(len+1);
	f->read(s, len);
	s[len] = 0;
	
	return s;
}

void flush(STREAM* f)
{
	f->flush();
}
