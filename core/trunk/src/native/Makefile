
gcc_options = -I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/ -O3

bci-agent-objects = CBuffer.o bci-agent.o NativeCollector.o utils.o CThreadData.o md5.o

bci-agent: $(bci-agent-objects)
	g++ -shared -o ../../libbci-agent.so $(bci-agent-objects)

CBuffer.o: CBuffer.cpp CBuffer.h
	g++ -c $(gcc_options) CBuffer.cpp
	
CThreadData.o: CThreadData.cpp CThreadData.h
	g++ -c $(gcc_options) CThreadData.cpp

NativeCollector.o: NativeCollector.cpp tod_core_transport_NativeCollector.h
	g++ -c $(gcc_options) NativeCollector.cpp

bci-agent.o: bci-agent.c tod_core_ObjectIdentity.h
	g++ -c $(gcc_options) bci-agent.c
	
utils.o: utils.c utils.h
	g++ -c $(gcc_options) utils.c
	
md5.o: md5.c md5.h
	g++ -c $(gcc_options) md5.c