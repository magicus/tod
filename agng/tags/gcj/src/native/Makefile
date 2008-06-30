SHARED_MAGIC = -shared
STATIC_MAGIC = -Wl,-Bstatic

WS_BASE=../../..
GCJ_SRC = ../java
BIN_PATH = ../../bin:$(WS_BASE)/TOD/bin:$(WS_BASE)/zz.utils/bin
TOD_CORE_SRC=$(WS_BASE)/TOD/src/debugger
ZZUTILS_SRC=$(WS_BASE)/zz.utils/src

ifeq ($(PLATFORM), win32)
	WINBASE = ~/vm
	CXX = i586-mingw32msvc-g++

	LDFLAGS += -lws2_32 -Wl,--add-stdcall-alias

	JAVA_HOME = $(WINBASE)/java
	JNI_INC = -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/win32/

	LIBPREFIX =
	LIBEXT = dll
endif

ifeq ($(PLATFORM), macos)
	SHARED_MAGIC = -dynamiclib
	STATIC_MAGIC = 
	
	CXXFLAGS += -Wno-long-double

	JNI_INC = -I $(JAVA_HOME)/Headers/ 
	CXXFLAGS += -arch ppc -arch i386 
	#CXXFLAGS += -arch ppc64 -arch x86_64

	LDFLAGS += -arch ppc -arch i386 
	#LDFLAGS += -arch ppc64 -arch x86_64

	LIBPREFIX = lib
	LIBEXT = dylib
endif

ifeq ($(PLATFORM), linux)
	JNI_INC = -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/
	GCJ_INC = -I /usr/include/c++/4.2 -I /usr/lib/gcc/i486-linux-gnu/4.2/include

	LDFLAGS += -static-libgcc 
	#LDFLAGS += -lstdc++

	LIBPREFIX = lib
	LIBEXT = so
endif

CXXFLAGS += -O3 
GCJFLAGS += -g

GCJ_INC += -I ../java

tod-agent-objects = \
	$(PLATFORM)/tod-agent-skel.o \
	$(PLATFORM)/tod-agent-bridge.o \
	$(PLATFORM)/jniutils.o \
	$(PLATFORM)/TodAgent.o \
	$(PLATFORM)/VMUtils.o \
	$(PLATFORM)/MethodInfo.o \
	$(PLATFORM)/ExceptionProcessor.o \
	$(PLATFORM)/NativeAgentConfig.o \
	$(PLATFORM)/InstrumentationManager.o \
	$(PLATFORM)/ConnectionManager.o \
	$(PLATFORM)/TOD-core.o \
	$(PLATFORM)/zz.utils.o \
	
TOD-core-classes = \
	$(TOD_CORE_SRC)/tod/tools/parsers/*.java \
	$(TOD_CORE_SRC)/tod/tools/parsers/workingset/*.java \
	$(TOD_CORE_SRC)/tod/core/config/ClassSelector.java \
	


zz.utils-classes = \
	$(ZZUTILS_SRC)/zz/utils/IFilter.java \
	$(ZZUTILS_SRC)/zz/utils/Utils.java \
	$(ZZUTILS_SRC)/zz/utils/NamedObject.java \
	$(ZZUTILS_SRC)/zz/utils/Future.java \
	$(ZZUTILS_SRC)/zz/utils/PublicCloneable.java \
	$(ZZUTILS_SRC)/zz/utils/IPublicCloneable.java \
	$(ZZUTILS_SRC)/zz/utils/ITask.java \


##############

all: tod-agent

tod-agent: dirs $(tod-agent-objects)
	gcj $(SHARED_MAGIC) $(GCJFLAGS) -o $(PLATFORM)/$(LIBPREFIX)tod-agent.$(LIBEXT) $(tod-agent-objects) $(LDFLAGS)
	cp $(PLATFORM)/$(LIBPREFIX)tod-agent.$(LIBEXT) ../..

dirs:
	mkdir -p $(PLATFORM)

clean:
	rm -f ../../libtod-agent.so ../../tod-agent.dll 
	rm -f $(GCJ_SRC)/tod/agent/*.h
	rm -rf linux win32 macos

############## 

$(PLATFORM)/tod-agent-skel.o: tod-agent-skel.cpp tod-agent-skel.h tod-agent-bridge.h jniutils.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) $(JNI_INC) tod-agent-skel.cpp -o $(PLATFORM)/tod-agent-skel.o

$(PLATFORM)/tod-agent-bridge.o: tod-agent-bridge.cpp tod-agent-skel.h tod-agent-bridge.h $(GCJ_SRC)/tod/agent/TodAgent.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) $(GCJ_INC) tod-agent-bridge.cpp -o $(PLATFORM)/tod-agent-bridge.o

$(PLATFORM)/jniutils.o: jniutils.cpp jniutils.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) $(JNI_INC) jniutils.cpp -o $(PLATFORM)/jniutils.o

$(GCJ_SRC)/tod/agent/VMUtils.h: $(GCJ_SRC)/tod/agent/VMUtils.java 
# 	gcj -C $(GCJ_SRC)/tod/agent/VMUtils.java
	rm -f $(GCJ_SRC)/tod/agent/VMUtils.h
	gcjh -cp $(GCJ_SRC):$(BIN_PATH) -d $(GCJ_SRC) tod.agent.VMUtils
	echo "Regenerate stub!"

$(PLATFORM)/VMUtils.o: VMUtils.cpp tod-agent-skel.h $(GCJ_SRC)/tod/agent/VMUtils.h $(GCJ_SRC)/tod/agent/MethodInfo.h
	$(CXX) $(SHARED_MAGIC) -c $(CXXFLAGS) $(GCJ_INC) VMUtils.cpp -o $(PLATFORM)/VMUtils.o
	
$(PLATFORM)/TOD-core.o: $(TOD-core-classes)
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(TOD-core-classes) -o $(PLATFORM)/TOD-core.o
	
$(PLATFORM)/zz.utils.o: $(zz.utils-classes)
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(zz.utils-classes) -o $(PLATFORM)/zz.utils.o


$(GCJ_SRC)/tod/agent/TodAgent.h: $(GCJ_SRC)/tod/agent/TodAgent.java
	rm -f $(GCJ_SRC)/tod/agent/TodAgent.h
	gcjh -cp $(GCJ_SRC):$(BIN_PATH) -d $(GCJ_SRC) tod.agent.TodAgent

$(PLATFORM)/TodAgent.o: $(GCJ_SRC)/tod/agent/TodAgent.java
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(GCJ_SRC)/tod/agent/TodAgent.java -o $(PLATFORM)/TodAgent.o

$(GCJ_SRC)/tod/agent/MethodInfo.h: $(GCJ_SRC)/tod/agent/MethodInfo.java
	rm -f $(GCJ_SRC)/tod/agent/MethodInfo.h
	gcjh -cp $(GCJ_SRC):$(BIN_PATH) -d $(GCJ_SRC) tod.agent.MethodInfo
	
$(PLATFORM)/MethodInfo.o: $(GCJ_SRC)/tod/agent/MethodInfo.java
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(GCJ_SRC)/tod/agent/MethodInfo.java -o $(PLATFORM)/MethodInfo.o
	
$(PLATFORM)/ExceptionProcessor.o: $(GCJ_SRC)/tod/agent/ExceptionProcessor.java
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(GCJ_SRC)/tod/agent/ExceptionProcessor.java -o $(PLATFORM)/ExceptionProcessor.o
	
$(PLATFORM)/NativeAgentConfig.o: $(GCJ_SRC)/tod/agent/NativeAgentConfig.java
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(GCJ_SRC)/tod/agent/NativeAgentConfig.java -o $(PLATFORM)/NativeAgentConfig.o
	
$(PLATFORM)/InstrumentationManager.o: $(GCJ_SRC)/tod/agent/InstrumentationManager.java
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(GCJ_SRC)/tod/agent/InstrumentationManager.java -o $(PLATFORM)/InstrumentationManager.o
	
$(PLATFORM)/ConnectionManager.o: $(GCJ_SRC)/tod/agent/ConnectionManager.java
	gcj --classpath=$(BIN_PATH) -c $(GCJFLAGS) $(GCJ_SRC)/tod/agent/ConnectionManager.java -o $(PLATFORM)/ConnectionManager.o
	
	