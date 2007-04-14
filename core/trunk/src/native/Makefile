

ifeq ($(PLATFORM), win32)
	WINBASE = ~/vm
	CXX = i586-mingw32msvc-g++
	CXXFLAGS += -I $(WINBASE)/boost/include/boost-1_33_1 -I $(WINBASE)/asio/include
	LDFLAGS_BOOST += -L $(WINBASE)/boost/lib
	LDFLAGS_BOOST += -lboost_thread-gcc-mt-1_33_1 -lboost_filesystem-gcc-mt-1_33_1 -lws2_32 -Wl,--add-stdcall-alias
	
	JAVA_HOME = $(WINBASE)/java
	CXXFLAGS += -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/win32/
	
	LIBPREFIX =
	LIBEXT = dll
	
else
	PLATFORM = linux
	LDFLAGS_BOOST += -lboost_thread -lboost_filesystem
	
	CXXFLAGS += -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/
	
	LIBPREFIX = lib
	LIBEXT = so
endif

CXXFLAGS += -O3

bci-agent-objects = \
	$(PLATFORM)/utils.o \
	$(PLATFORM)/jniutils.o \
	$(PLATFORM)/md5.o \
	$(PLATFORM)/bci-agent.o 
	
array-cast-objects = \
	$(PLATFORM)/array-cast.o
	
#nc-objects = CBuffer.o NativeCollector.o utils.o CThreadData.o md5.o
#native-stream-objects = native-stream.o

##############

all: bci-agent array-cast

bci-agent: dirs $(bci-agent-objects)
	$(CXX) -shared -o $(PLATFORM)/$(LIBPREFIX)bci-agent.$(LIBEXT) $(bci-agent-objects) $(LDFLAGS) $(LDFLAGS_BOOST)
	cp $(PLATFORM)/$(LIBPREFIX)bci-agent.$(LIBEXT) ../..
	
# native-stream: $(native-stream-objects)
# 	$(CXX) -shared -o ../../$(LIBPREFIX)native-stream.$(LIBEXT) $(native-stream-objects) $(LDFLAGS)
	
array-cast: dirs $(array-cast-objects)
	$(CXX) -shared -o $(PLATFORM)/$(LIBPREFIX)array-cast.$(LIBEXT) $(array-cast-objects) $(LDFLAGS)
	cp $(PLATFORM)/$(LIBPREFIX)array-cast.$(LIBEXT) ../..
	
dirs:
	mkdir -p $(PLATFORM)


clean:
	rm -f ../../libbci-agent.so ../../libnative-stream.so ../../bci-agent.dll ../../native-stream.dll
	rm -rf linux win32

############## 

$(PLATFORM)/CBuffer.o: CBuffer.cpp CBuffer.h
	$(CXX) -shared -c $(CXXFLAGS) CBuffer.cpp -o $(PLATFORM)/CBuffer.o
	
$(PLATFORM)/CThreadData.o: CThreadData.cpp CThreadData.h
	$(CXX) -shared -c $(CXXFLAGS) CThreadData.cpp -o $(PLATFORM)/CThreadData.o

$(PLATFORM)/NativeCollector.o: NativeCollector.cpp tod_core_transport_NativeCollector.h
	$(CXX) -shared -c $(CXXFLAGS) NativeCollector.cpp -o $(PLATFORM)/NativeCollector.o

$(PLATFORM)/bci-agent.o: bci-agent.cpp tod_core_ObjectIdentity.h utils.h jniutils.h
	$(CXX) -shared -c $(CXXFLAGS) bci-agent.cpp -o $(PLATFORM)/bci-agent.o
	
$(PLATFORM)/utils.o: utils.cpp utils.h
	$(CXX) -shared -c $(CXXFLAGS) utils.cpp -o $(PLATFORM)/utils.o
	
$(PLATFORM)/jniutils.o: jniutils.cpp jniutils.h
	$(CXX) -shared -c $(CXXFLAGS) jniutils.cpp -o $(PLATFORM)/jniutils.o
	
$(PLATFORM)/md5.o: md5.cpp md5.h
	$(CXX) -shared -c $(CXXFLAGS) md5.cpp -o $(PLATFORM)/md5.o
	
$(PLATFORM)/native-stream.o: native-stream.cpp
	$(CXX) -shared -c $(CXXFLAGS) native-stream.cpp -o $(PLATFORM)/native-stream.o

$(PLATFORM)/array-cast.o: array-cast.cpp
	$(CXX) -shared -c $(CXXFLAGS) array-cast.cpp -o $(PLATFORM)/array-cast.o

