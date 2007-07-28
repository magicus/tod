
BOOST_VERSION = 1_34_1
SHARED_MAGIC = -shared
STATIC_MAGIC = -Wl,-Bstatic 

ifeq ($(PLATFORM), win32)
	BOOST_FLAVOR = mgw34-mt
	
	WINBASE = ~/vm
	CXX = i586-mingw32msvc-g++
	CXXFLAGS += -I $(WINBASE)/boost/include/boost-$(BOOST_VERSION) -I $(WINBASE)/asio/include

	LDFLAGS_BOOST += -L $(WINBASE)/boost/lib
	LDFLAGS += -lws2_32 -Wl,--add-stdcall-alias

	JAVA_HOME = $(WINBASE)/java
	CXXFLAGS += -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/win32/

	LIBPREFIX =
	LIBEXT = dll
endif

ifeq ($(PLATFORM), macos)
	BOOST_FLAVOR = mt
	SHARED_MAGIC = -dynamiclib
	STATIC_MAGIC = 
	CXXFLAGS += -I /usr/local/include/boost-$(BOOST_VERSION) -I /usr/local/include/asio-0.3.8rc3/
	
	CXXFLAGS += -Wno-long-double

	CXXFLAGS += -I $(JAVA_HOME)/Headers/ 
	CXXFLAGS += -arch ppc -arch i386 
	#CXXFLAGS += -arch ppc64 -arch x86_64

	LDFLAGS += -arch ppc -arch i386 
	#LDFLAGS += -arch ppc64 -arch x86_64

	LIBPREFIX = lib
	LIBEXT = dylib
endif

ifeq ($(PLATFORM), linux)
	BOOST_FLAVOR = gcc41-mt
	
	CXXFLAGS += -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/
	LDFLAGS += -static-libgcc 

	LIBPREFIX = lib
	LIBEXT = so
endif

CXXFLAGS += -O3 
LDFLAGS_BOOST += \
	$(STATIC_MAGIC) -lboost_thread-$(BOOST_FLAVOR)-$(BOOST_VERSION) \
	$(STATIC_MAGIC) -lboost_filesystem-$(BOOST_FLAVOR)-$(BOOST_VERSION)

bci-agent-objects = \
	$(PLATFORM)/bci-agent.o \
	$(PLATFORM)/utils.o \
	$(PLATFORM)/md5.o \
	$(PLATFORM)/jniutils.o 

array-cast-objects = \
	$(PLATFORM)/array-cast.o

#nc-objects = CBuffer.o NativeCollector.o utils.o CThreadData.o md5.o
#native-stream-objects = native-stream.o

##############

all: bci-agent #array-cast

bci-agent: dirs $(bci-agent-objects)
	$(CXX) $(SHARED_MAGIC) -o $(PLATFORM)/$(LIBPREFIX)bci-agent.$(LIBEXT) $(bci-agent-objects) $(LDFLAGS) $(LDFLAGS_BOOST)
	cp $(PLATFORM)/$(LIBPREFIX)bci-agent.$(LIBEXT) ../..

#pathtest: dirs $(PLATFORM)/pathtest.o
#	$(CXX) -o $(PLATFORM)/pathtest $(PLATFORM)/pathtest.o $(LDFLAGS) $(LDFLAGS_BOOST)

# native-stream: $(native-stream-objects)
# 	$(CXX) $(SHARED_MAGIC) -o ../../$(LIBPREFIX)native-stream.$(LIBEXT) $(native-stream-objects) $(LDFLAGS)

array-cast: dirs $(array-cast-objects)
	$(CXX) $(SHARED_MAGIC) -o $(PLATFORM)/$(LIBPREFIX)array-cast.$(LIBEXT) $(array-cast-objects) $(LDFLAGS)
	cp $(PLATFORM)/$(LIBPREFIX)array-cast.$(LIBEXT) ../..

dirs:
	mkdir -p $(PLATFORM)


clean:
	rm -f ../../libbci-agent.so ../../libnative-stream.so ../../bci-agent.dll ../../native-stream.dll
	rm -rf linux win32 macos

############## 

$(PLATFORM)/CBuffer.o: CBuffer.cpp CBuffer.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) CBuffer.cpp -o $(PLATFORM)/CBuffer.o

$(PLATFORM)/CThreadData.o: CThreadData.cpp CThreadData.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) CThreadData.cpp -o $(PLATFORM)/CThreadData.o

$(PLATFORM)/NativeCollector.o: NativeCollector.cpp tod_core_transport_NativeCollector.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) NativeCollector.cpp -o $(PLATFORM)/NativeCollector.o

$(PLATFORM)/bci-agent.o: bci-agent.cpp tod_core_ObjectIdentity.h utils.h jniutils.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) bci-agent.cpp -o $(PLATFORM)/bci-agent.o

$(PLATFORM)/utils.o: utils.cpp utils.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) utils.cpp -o $(PLATFORM)/utils.o

$(PLATFORM)/jniutils.o: jniutils.cpp jniutils.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) jniutils.cpp -o $(PLATFORM)/jniutils.o

$(PLATFORM)/md5.o: md5.cpp md5.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) md5.cpp -o $(PLATFORM)/md5.o

$(PLATFORM)/native-stream.o: native-stream.cpp
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) native-stream.cpp -o $(PLATFORM)/native-stream.o

$(PLATFORM)/array-cast.o: array-cast.cpp
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) array-cast.cpp -o $(PLATFORM)/array-cast.o

#$(PLATFORM)/pathtest.o: pathtest.cpp
#	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) pathtest.cpp -o $(PLATFORM)/pathtest.o

