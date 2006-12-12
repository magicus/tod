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

#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <pthread.h>


#include "CThreadData.h"

pthread_mutex_t socketMutex = PTHREAD_MUTEX_INITIALIZER;
extern FILE* gEventSocket;

CThreadData::CThreadData()
{
	_tmpBuffer = new CBuffer(1024);
	_sendBuffer = (char*) malloc(THREAD_BUFFER_SIZE);
	_pointer = _sendBuffer;
}

CThreadData::~CThreadData()
{
	free(_sendBuffer);
	delete _tmpBuffer;
}

CBuffer* CThreadData::packetStart()
{
	return _tmpBuffer;
}

void CThreadData::packetEnd()
{
	int reqSize = _tmpBuffer->size();
	int remaining = THREAD_BUFFER_SIZE - (_pointer - _sendBuffer);
	
	if (remaining < reqSize) flush();
	
	memcpy(_pointer, _tmpBuffer->getBuffer(), reqSize);
	_pointer += reqSize;
	
	_tmpBuffer->reset();
}

void CThreadData::flush()
{
	pthread_mutex_lock(&socketMutex);
// 	fwrite(_sendBuffer, 1, _pointer - _sendBuffer, gEventSocket);
	pthread_mutex_unlock(&socketMutex);
	
	_pointer = _sendBuffer;
}
