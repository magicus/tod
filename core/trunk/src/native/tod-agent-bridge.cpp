#include <gcj/cni.h>
#include <java/lang/System.h>
#include <java/io/PrintStream.h>
#include <java/lang/Throwable.h>

using namespace java::lang;
using namespace std;

void agVMStart()
{
	try
	{
		String *message = JvNewStringLatin1("GCJ - VMStart");
		System::out->println(message);
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception:"));
		t->printStackTrace();
	}
}

void agOnLoad()
{
	try
	{
		JvCreateJavaVM(NULL);
		JvAttachCurrentThread(NULL, NULL);
		JvInitClass(&System::class$);
		
		String *message = JvNewStringLatin1("GCJ - OnLoad");
		System::out->println(message);
	}
	catch (Throwable *t)
	{
		System::err->println(JvNewStringLatin1("Unhandled Java exception:"));
		t->printStackTrace();
	}
}



