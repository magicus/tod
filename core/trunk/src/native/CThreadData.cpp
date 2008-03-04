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
