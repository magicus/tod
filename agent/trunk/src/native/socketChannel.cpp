#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <boost/asio.hpp>
#include <vector>

typedef std::iostream STREAM;
using boost::asio::ip::tcp;

// Maps file descriptors to streams
std::vector<STREAM*> fds;

#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jint JNICALL Java_java_tod_io__1SocketChannel_open0
  (JNIEnv* jni, jclass, jstring jHost, jint port)
{
	int len = jni->GetStringUTFLength(jHost);
	const char* bHost = jni->GetStringUTFChars(jHost, NULL);
	char* host = (char*) malloc(len+1);
	memcpy(host, bHost, len);
	host[len] = 0;
	
	char sPort[12];
	sprintf(sPort, "%d", port);

	STREAM* s = new tcp::iostream(host, sPort);
	int fd;
	if (! s->fail())
	{
		fd = fds.size();
		fds.push_back(s);
	}
	else
	{
		fd = -1;
		printf("Cannot connect to %s:%s\n", host, sPort);
		fflush(stdout);
	}

	free(host);
	jni->ReleaseStringUTFChars(jHost, bHost);
	
	return fd;
}

JNIEXPORT jint JNICALL Java_java_tod_io__1SocketChannel_flush0
  (JNIEnv *, jclass, jint fd)
{
	STREAM* s = fds[fd];
	if (! s->good()) return -1;
	s->flush();
	if (! s->good()) return -2;
	return 0;
}

JNIEXPORT jint JNICALL Java_java_tod_io__1SocketChannel_close0
  (JNIEnv *, jclass, jint)
{
	return 0;
}

JNIEXPORT jint JNICALL Java_java_tod_io__1SocketChannel_write0
  (JNIEnv* jni, jclass, jint fd, jbyteArray bytes, jint pos, jint len)
{
	STREAM* s = fds[fd];
	int result;
	char* carray = (char*) jni->GetPrimitiveArrayCritical(bytes, NULL);
	
	if (! s->good()) result = -1;
	else
	{
		s->write(carray+pos, len);
		if (! s->good()) result = -1;
		else result = len;
	}
	
	jni->ReleasePrimitiveArrayCritical(bytes, carray, JNI_ABORT);
	
	return result;
}

JNIEXPORT jint JNICALL Java_java_tod_io__1SocketChannel_read0
  (JNIEnv* jni, jclass, jint fd, jbyteArray bytes, jint pos, jint len)
{
	STREAM* s = fds[fd];
	int result;
	char* carray = (char*) jni->GetPrimitiveArrayCritical(bytes, NULL);
	
	if (! s->good()) result = -2;
	else
	{
		s->read(carray+pos, len);
		if (s->bad() || s->fail()) result = -3;
		else
		{
			result = s->gcount();
			if (result == 0 && s->eof()) result = -1;
		}
	}
	
	jni->ReleasePrimitiveArrayCritical(bytes, carray, 0);
	
	return result;
}

JNIEXPORT jint JNICALL Java_java_tod_io__1SocketChannel_in_1avail0
  (JNIEnv* jni, jclass, jint fd)
{
	STREAM* s = fds[fd];
	return s->rdbuf()->in_avail();
}

#ifdef __cplusplus
}
#endif
