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
