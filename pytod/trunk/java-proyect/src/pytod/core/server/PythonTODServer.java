package pytod.core.server;

import hep.io.xdr.XDRInputStream;

import java.io.IOException;
import java.net.Socket;

import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.server.ITODServerFactory;
import tod.core.server.TODServer;
import tod.gui.eventlist.LocalVariableWriteNode;

/**
 * A Python TOD server accepts connections from debugged script Python and process instrumentation
 * requests as well as logged events.
 * The actual implementation of the instrumenter and database are left
 * to delegates.
 * @author minostro
 */

public class PythonTODServer extends TODServer
{
	private final IMutableStructureDatabase itsStructureDatabase;
	private final ILogCollector itsLogCollector;

	public PythonTODServer(
			TODConfig aConfig,
			IMutableStructureDatabase aStructureDatabase,
			ILogCollector aLogCollector) 
	{
		super(aConfig);
		itsStructureDatabase = aStructureDatabase;
		itsLogCollector = aLogCollector;
		System.out.println("Hola soy Pilton");
	}

	@Override
	protected void accepted(Socket aSocket) 
	{
		try
		{
			XDRInputStream theStream = new XDRInputStream(aSocket.getInputStream());
			new Receiver(theStream);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	//objects
	private static final int OBJECT_CLASS = 0;
	private static final int OBJECT_METHOD = 1;
	private static final int OBJECT_ATTRIBUTE = 2;
	private static final int OBJECT_FUNCTION = 3;
	private static final int OBJECT_LOCAL = 4;
	private static final int OBJECT_PROBE = 5;
	private static final int OBJECT_THREAD = 6;
	//dataTypes
	private static final int DATA_INT = 0;
	private static final int DATA_STR = 1;
	private static final int DATA_FLOAT = 2;
	private static final int DATA_LONG = 3;
	private static final int DATA_BOOL = 4;
	private static final int DATA_OTHER = 5;
	//events
	private static final int REGISTER_EVENT = 0;
	private static final int CALL_EVENT = 1;
	private static final int SET_EVENT = 2;
	private static final int RETURN_EVENT = 3;
	
	private class Argument
	{
		private int id;
		private String name;
		

		public Argument(int aId, String aName)
		{
			id = aId;
			name = new String(aName);
			
		}
	}
	
	private class calledArgument
	{
		private int id;
		private int value;
		
		public calledArgument(int aId, int aValue)
		{
			id = aId;
			value = aValue;
		}
		
	}
	
	private class Receiver extends Thread
	{
		private XDRInputStream itsStream;
		
		
		public Receiver(XDRInputStream aInputStream)
		{
			itsStream = aInputStream;
			start();
		}


		public void registerFunction(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			Argument args[];
			try
			{
				int functionId = theStream.readInt();
				String functionName = new String(theStream.readString());
				int argsN = theStream.readInt();
				if (argsN != 0){
					args = new Argument[argsN];
					for(int i=0;i<argsN;i=i+1)
					{
						int argId = theStream.readInt();
						String argName = new String(theStream.readString());
						args[i] = new Argument(argId,argName);
					}
				}
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void registerLocal(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				int localId = theStream.readInt();
				int parentId = theStream.readInt();
				String localName = new String(theStream.readString());
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}

		public void registerClass(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			IMutableClassInfo itsClass;
			try
			{
				int classId = theStream.readInt();
				String className = new String(theStream.readString());
				int classBases = theStream.readInt();
				itsClass = itsStructureDatabase.addClass(classId, className);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}

		public void registerMethod(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			IMutableClassInfo theClass;
			IMutableBehaviorInfo theBehavior;
			try
			{
				int methodId = theStream.readInt();
				int classId = theStream.readInt();
				String methodName = theStream.readString();
				int argsN = theStream.readInt();
				theClass = itsStructureDatabase.getClass(classId, true);
				theBehavior = theClass.addBehavior(methodId, methodName, ""+argsN);
				if (argsN != 0){
					for(int i=0;i<argsN;i=i+1)
					{
						int argId = theStream.readInt();
						String argName = theStream.readString();
						theBehavior.addLocalVariableInfo(new LocalVariableInfo(0,0,argName," ",argId));
					}
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}		
		
		public void registerAttribute(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				int attributeId = theStream.readInt();
				int parentId = theStream.readInt();
				String attributeName = new String(theStream.readString());
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}

		public void registerProbe(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				int probeId = theStream.readInt();
				int parentId = theStream.readInt();				
				int probeCurrentLasti = theStream.readInt();
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void registerThread(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				int threadId = theStream.readInt();
				int sysId = theStream.readInt();
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void callMethod(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			calledArgument args[];
			try
			{
				int methodId = theStream.readInt();
				int parentId = theStream.readInt();
				int classId = theStream.readInt();
				int argsN = theStream.readInt();
				if (argsN != 0){
					args = new calledArgument[argsN];
					for(int i=0;i<argsN;i=i+1)
					{
						int argId = theStream.readInt();
						//preguntar como lo haremos con string o int
						int argValue = theStream.readInt();
						args[i] = new calledArgument(argId,argValue);
					}
				}
				int probeId = theStream.readInt();
				double parentTimeStampFrame = theStream.readDouble();
				int depth = theStream.readInt();
				double currentTimeStamp = theStream.readDouble();
				int threadId = theStream.readInt();
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public void callFunction(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			calledArgument args[];
			try
			{
				int functionId = theStream.readInt();
				int parentId = theStream.readInt();
				int argsN = theStream.readInt();
				if (argsN != 0){
					args = new calledArgument[argsN];
					for(int i=0;i<argsN;i=i+1)
					{
						int argId = theStream.readInt();
						//preguntar como lo haremos con string o int
						int argValue = theStream.readInt();
						args[i] = new calledArgument(argId,argValue);
					}
				}
				int probeId = theStream.readInt();
				double parentTimeStampFrame = theStream.readDouble();
				int depth = theStream.readInt();
				double currentTimeStamp = theStream.readDouble();
				int threadId = theStream.readInt();
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		public void setAttribute(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				int attributeId = theStream.readInt();
				int parentId = theStream.readInt();
				//ver el asunto de los valores..el tipo
				int attributeValue = theStream.readInt();
				int probeId = theStream.readInt();
				double parentTimeStampFrame = theStream.readDouble();
				int depth = theStream.readInt();
				double currentTimeStamp = theStream.readDouble();
				int threadId = theStream.readInt();				
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		
		public void setLocal(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				int localId = theStream.readInt();
				int parentId = theStream.readInt();
				//ver el asunto de los valores..el tipo
				int localValue = theStream.readInt();
				int probeId = theStream.readInt();
				double parentTimeStampFrame = theStream.readDouble();
				int depth = theStream.readInt();
				double currentTimeStamp = theStream.readDouble();
				int threadId = theStream.readInt();				
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void returnEvent(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			try
			{
				//que pasa con el id del return?
				//ver tipo de datos
				int returnValue = theStream.readInt();
				int probeId = theStream.readInt();
				boolean hasThrown = theStream.readBoolean();				
				//mandar registro a la base de datos
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					String theEvent = itsStream.readString();
					System.out.println(theEvent);
					int theEvent1 = 1;
					switch (theEvent1)
					{
					case REGISTER_EVENT:
					{
						int theObject = itsStream.readInt();
						switch(theObject)
						{
						case OBJECT_CLASS:
							registerClass(itsStream);
							break;
						case OBJECT_METHOD:
							registerMethod(itsStream);
							break;
						case OBJECT_ATTRIBUTE:
							registerAttribute(itsStream);						
							break;
						case OBJECT_FUNCTION:
							registerFunction(itsStream);
							break;
						case OBJECT_LOCAL:
							registerLocal(itsStream);
							break;
						case OBJECT_PROBE:
							registerProbe(itsStream);
							break;
						case OBJECT_THREAD:
							registerThread(itsStream);
							break;
						default:
							break;
						}	
					}
					break;
					case CALL_EVENT:
					{
						int theObject = itsStream.readInt();
						switch (theObject)
						{
						case OBJECT_METHOD:
							callMethod(itsStream);
							break;
						case OBJECT_FUNCTION:
							callFunction(itsStream);
							break;
						default:
							break;
						}
					}
					break;
					case SET_EVENT:
					{
						int theObject = itsStream.readInt();
						switch (theObject)
						{
						case OBJECT_ATTRIBUTE:
							setAttribute(itsStream);
							break;
						case OBJECT_LOCAL:
							setLocal(itsStream);
							break;
						default:
							break;
						}
					}
					break;	
					case RETURN_EVENT:
						returnEvent(itsStream);
						break;

					default:
						break;
					}
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		
	}

}