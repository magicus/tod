#ifndef _Included_CThreadData_h
#define _Included_CThreadData_h

#include "CBuffer.h"

#define THREAD_BUFFER_SIZE 32768


class CThreadData
{
	// Buffer for in-construction packets
	CBuffer* _tmpBuffer;
	
	// Buffer where complete packets are stored before they are sent.
	char* _sendBuffer;
	char* _pointer;
	
public:
	CThreadData();
	~CThreadData();
	
	CBuffer* packetStart();
	void packetEnd();
	
	void flush();
};

#endif
