
bci-agent-objects = bci-agent.o utils.o md5.o
nc-objects = CBuffer.o NativeCollector.o utils.o CThreadData.o md5.o
native-stream-objects = native-stream.o
array-cast-objects = array-cast.o

ifeq ($(PLATFORM), win32)
	WINBASE = ~/vm
	CXX = i586-mingw32msvc-g++
	CXXFLAGS += -I $(WINBASE)/boost/include/boost-1_33_1 -I $(WINBASE)/asio/include
	LDFLAGS += -L $(WINBASE)/boost/lib
	LDFLAGS += -lboost_thread-gcc-mt-1_33_1 -lboost_filesystem-gcc-mt-1_33_1 -lws2_32 -Wl,--add-stdcall-alias
	
	JAVA_HOME = $(WINBASE)/java
	CXXFLAGS += -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/win32/
	
	LIBPREFIX =
	LIBEXT = dll
	
else
	LDFLAGS += -lboost_thread -lboost_filesystem
	
	CXXFLAGS += -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/
	
	LIBPREFIX = lib
	LIBEXT = so
endif

CXXFLAGS += -O3


##############

all: bci-agent array-cast

bci-agent: $(bci-agent-objects)
	$(CXX) -shared -o ../../$(LIBPREFIX)bci-agent.$(LIBEXT) $(bci-agent-objects) $(LDFLAGS)
	
native-stream: $(native-stream-objects)
	$(CXX) -shared -o ../../$(LIBPREFIX)native-stream.$(LIBEXT) $(native-stream-objects) $(LDFLAGS)
	
array-cast: $(array-cast-objects)
	$(CXX) -shared -o ../../$(LIBPREFIX)array-cast.$(LIBEXT) $(array-cast-objects) $(LDFLAGS)
	
clean:
	rm -f *.o *.obj ../../libbci-agent.so ../../libnative-stream.so ../../bci-agent.dll ../../native-stream.dll

############## 

CBuffer.o: CBuffer.cpp CBuffer.h
	$(CXX) -shared -c $(CXXFLAGS) CBuffer.cpp -o CBuffer.o
	
CThreadData.o: CThreadData.cpp CThreadData.h
	$(CXX) -shared -c $(CXXFLAGS) CThreadData.cpp -o CThreadData.o

NativeCollector.o: NativeCollector.cpp tod_core_transport_NativeCollector.h
	$(CXX) -shared -c $(CXXFLAGS) NativeCollector.cpp -o NativeCollector.o

bci-agent.o: bci-agent.cpp tod_core_ObjectIdentity.h
	$(CXX) -shared -c $(CXXFLAGS) bci-agent.cpp -o bci-agent.o
	
utils.o: utils.cpp utils.h
	$(CXX) -shared -c $(CXXFLAGS) utils.cpp -o utils.o
	
md5.o: md5.cpp md5.h
	$(CXX) -shared -c $(CXXFLAGS) md5.cpp -o md5.o
	
native-stream.o: native-stream.cpp
	$(CXX) -shared -c $(CXXFLAGS) native-stream.cpp -o native-stream.o

array-cast.o: array-cast.cpp
	$(CXX) -shared -c $(CXXFLAGS) array-cast.cpp -o array-cast.o

