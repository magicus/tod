package pytod.core.server;

import hep.io.xdr.XDRInputStream;

import java.io.IOException;
import java.net.Socket;

import tod.agent.transport.ValueWriter;
import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.server.TODServer;

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
		//truculencia planteada por Guillaume
		IMutableClassInfo theClass = itsStructureDatabase.addClass(100, "functionClass");
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
	
	//events
	private static final int REGISTER_EVENT = 0;
	private static final int CALL_EVENT = 1;
	private static final int SET_EVENT = 2;
	private static final int RETURN_EVENT = 3;
	private static final int INSTANTIATION_EVENT = 4;	
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
	private static final int DATA_TUPLE = 5;
	private static final int DATA_LIST = 6;
	private static final int DATA_DICT = 7;
	private static final int DATA_OTHER = 8;
	
	
	private class Receiver extends Thread
	{
		private XDRInputStream itsStream;
		
		
		public Receiver(XDRInputStream aInputStream)
		{
			super("PythonTODServer.Receiver");
			itsStream = aInputStream;
			start();
		}

		private String generateSignature(int aArgsCount)
		{
			StringBuilder theSignatureBuilder = new StringBuilder("(");
			for(int i=0;i<aArgsCount;i++) theSignatureBuilder.append("Ljava.lang.Object;");
			theSignatureBuilder.append(")Ljava.lang.Object;");
			
			return theSignatureBuilder.toString();
		}

		public void registerFunction(XDRInputStream aInputStream)
		{
			IMutableClassInfo theClass;
			IMutableBehaviorInfo theBehavior;
			try
			{
				int functionId = aInputStream.readInt();
				String functionName = new String(aInputStream.readString());
				int argsN = aInputStream.readInt();
				theClass = itsStructureDatabase.getClass(100, true);
				theBehavior = theClass.addBehavior(functionId, functionName, generateSignature(argsN), false);
				if (argsN != 0){
					for(int i=0;i<argsN;i=i+1)
					{
						String argName = new String(aInputStream.readString());						
						int argId = aInputStream.readInt();
						theBehavior.addLocalVariableInfo(new LocalVariableInfo(0, 32000, argName, "java.lang.Object", argId));
						System.out.println("Registrando variable local "+argName);
					}
				}
				String fileName = aInputStream.readString();
				System.out.println("Registrando la funcion "+functionName + "id = "+functionId);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void registerLocal(XDRInputStream aInputStream)
		{
			IMutableBehaviorInfo theBehavior;			
			try
			{
				int localId = aInputStream.readInt();
				int parentId = aInputStream.readInt();
				String localName = new String(aInputStream.readString());
				//for this moment only register locals of methods
				//TODO: guillaume must write {I hope} a handler for functions
				System.out.println(localId);
				System.out.println(parentId);
				System.out.println(localName);
				theBehavior = itsStructureDatabase.getBehavior(parentId, true);
				theBehavior.addLocalVariableInfo(new LocalVariableInfo(0, 32000, localName, "java.lang.Object", localId));
				System.out.println("Registrando variable local "+localName);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}

		public void registerClass(XDRInputStream aInputStream)
		{
			IMutableClassInfo theClass;
			try
			{
				int classId = aInputStream.readInt();
				System.out.println(classId);
				String className = new String(aInputStream.readString());
				//TODO: change register format class  without field classBases
				int classBases = aInputStream.readInt();
				theClass = itsStructureDatabase.addClass(classId, className);
				System.out.println("Registrando clase "+className);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void registerStaticField(XDRInputStream aInputStream)
		{
			IMutableClassInfo theClass;
			try {
//				theClass.addField(aId, aName, aType)
//				itsLogCollector.
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		public void registerMethod(XDRInputStream aInputStream)
		{
			IMutableClassInfo theClass;
			IMutableBehaviorInfo theBehavior;
			try
			{
				int methodId = aInputStream.readInt();
				int classId = aInputStream.readInt();
				String methodName = aInputStream.readString();
				int argsN = aInputStream.readInt();
				
				theClass = itsStructureDatabase.getClass(classId, true);
				theBehavior = theClass.addBehavior(methodId, methodName, generateSignature(argsN), false);
				//theBehavior = theClass.addBehavior(methodId, methodName, ""+argsN);
				if (argsN != 0){
					for(int i=0;i<argsN;i=i+1)
					{
						String argName = aInputStream.readString();
						int argId = aInputStream.readInt();
						theBehavior.addLocalVariableInfo(new LocalVariableInfo(0, 32000, argName, "java.lang.Object", argId));
						System.out.println("Registrando variable local "+argName);
					}
				}
				String fileName = aInputStream.readString();
				System.out.println("Registrando el metodo "+methodName + "id = "+methodId);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}		
		
		public void registerAttribute(XDRInputStream aInputStream)
		{
			IMutableClassInfo theClass;
			try
			{
				int attributeId = aInputStream.readInt();
				int parentId = aInputStream.readInt();
				String attributeName = new String(aInputStream.readString());	
				theClass = itsStructureDatabase.getClass(parentId, true);
				theClass.addField(attributeId, attributeName, null, true);
				System.out.println("Registrando un atributo con id "+attributeId);
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
				int currentLineno = theStream.readInt();
				itsStructureDatabase.addProbe(probeId, parentId, probeCurrentLasti, null, 0);
				System.out.println("Registrando probe "+probeId);
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
				itsLogCollector.thread(threadId, sysId, "()V");
				System.out.println("Registrando thread");
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}

		public void instantiationEvent(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			Object args[] = null;
			try
			{
				int behaviorId = theStream.readInt();
				int targetId = theStream.readInt();
				int argsN = theStream.readInt();
				if (argsN != 0){
					args = new Object[argsN];
					for(int i=0;i<argsN;i=i+1)
					{
						int argType = theStream.readInt();
						Object theValue = getObjectValue(argType, aInputStream);
						args[i] = theValue;
					}
				}
				int probeId = theStream.readInt();
				long parentTimeStampFrame = theStream.readLong();
				int depth = theStream.readInt();
				long currentTimeStamp = theStream.readLong();
				int threadId = theStream.readInt();
				itsLogCollector.instantiation(
						threadId,
						parentTimeStampFrame,
						(short)depth, 
						currentTimeStamp, 
						null,
						probeId,
						true, 
						-1,
						behaviorId,
						new ObjectId(targetId),
						args);
				System.out.println("instanciacion "+ targetId);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public void methodCall(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			Object args[] = null;
			try
			{
				int methodId = theStream.readInt();
				int targetId = theStream.readInt();
				int argsN = theStream.readInt();
				if (argsN != 0){
					args = new Object[argsN];
					for(int i=0;i<argsN;i=i+1)
					{
						int argType = theStream.readInt();
						Object theValue = getObjectValue(argType, aInputStream);
						args[i] = theValue;
					}
				}
				int probeId = theStream.readInt();
				long parentTimeStampFrame = theStream.readLong();
				int depth = theStream.readInt();
				long currentTimeStamp = theStream.readLong();
				int threadId = theStream.readInt();
				itsLogCollector.methodCall(
						threadId,
						parentTimeStampFrame,
						(short)depth, 
						currentTimeStamp, 
						null,
						probeId,
						true, 
						-1,
						methodId,
						new ObjectId(targetId),
						args);
				System.out.println("llamando a método "+ methodId);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public void functionCall(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			Object args[] = null;
			try
			{
				int functionId = theStream.readInt();
				int argsN = theStream.readInt();
				if (argsN != 0){
					args = new Object[argsN];
					for(int i=0;i<argsN;i=i+1)
					{
						int argType = theStream.readInt();
						Object theValue = getObjectValue(argType, aInputStream);
						args[i] = theValue;
					}
				}
				int probeId = theStream.readInt();
				long parentTimeStampFrame = theStream.readLong();
				int depth = theStream.readInt();
				long currentTimeStamp = theStream.readLong();
				int threadId = theStream.readInt();
				//TODO: guillaume must be write a handler for function
				itsLogCollector.methodCall(
						threadId,
						parentTimeStampFrame,
						(short)depth, 
						currentTimeStamp, 
						null,
						probeId,
						false, 
						-1,
						functionId,
						null,
						args);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		public Object getObjectValue(int aTypeId, XDRInputStream aInputStream)
		{
			try {
				Object theValue;
				switch (aTypeId) {
				case DATA_INT:
				{
					theValue = aInputStream.readInt();
					break;
				}
				case DATA_STR:
				{
					theValue = aInputStream.readString();
					break;
				}
				case DATA_FLOAT:
				{
					theValue = aInputStream.readFloat();
					break;
				}
				case DATA_LONG:
				{
					theValue = aInputStream.readLong();
					break;
				}
				case DATA_BOOL:
				{
					//theValue = aInputStream.readBoolean();
					theValue = aInputStream.readInt();
					if (theValue.equals(1)) {
						theValue = Boolean.TRUE;
					}
					else {
						theValue = Boolean.FALSE;
					}
					break;
				}
				case DATA_TUPLE:
				{
					theValue = aInputStream.readInt();
					break;
				}
				case DATA_LIST:
				{
					theValue = aInputStream.readInt();
					break;
				}
				case DATA_DICT:
				{
					theValue = aInputStream.readInt();
					break;
				}
				case DATA_OTHER:
				{
					theValue = aInputStream.readInt();
					break;
				}
				default:
					theValue = aInputStream.readInt();					
					break;
				}
				return theValue;
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
				int targetId = theStream.readInt();
				int typeId = aInputStream.readInt();
				Object theValue = getObjectValue(typeId, aInputStream);
				int probeId = theStream.readInt();
				long parentTimeStampFrame = theStream.readLong();
				int depth = theStream.readInt();
				long currentTimeStamp = theStream.readLong();
				int threadId = theStream.readInt();
				itsLogCollector.fieldWrite(
						threadId, 
						parentTimeStampFrame, 
						(short)depth, 
						currentTimeStamp, 
						null, 
						probeId, 
						attributeId, 
						new ObjectId(targetId), 
						theValue);
				System.out.println("modificando atributo "+ attributeId);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		
		public void setLocal(XDRInputStream aInputStream)
		{
			try
			{
				int localId = aInputStream.readInt();
				int parentId = aInputStream.readInt();
				int typeId = aInputStream.readInt();
				Object theValue = getObjectValue(typeId, aInputStream);
				int probeId = aInputStream.readInt();
				long parentTimeStampFrame = aInputStream.readLong();
				int depth = aInputStream.readInt();
				long currentTimeStamp = aInputStream.readLong();
				int threadId = aInputStream.readInt();				
				itsLogCollector.localWrite(
						threadId,
						parentTimeStampFrame, 
						(short)depth, 
						currentTimeStamp, 
						null, 
						probeId, 
						localId, 
						theValue);
				System.out.println("modificando variable "+ localId);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}			

		}
		
		public void returnEvent(XDRInputStream aInputStream)
		{
			XDRInputStream theStream = aInputStream;
			int exitId = 101;
			try
			{
				boolean hasThrown = false;
				int behaviorId = aInputStream.readInt();
				int typeId = aInputStream.readInt();
				Object theValue = getObjectValue(typeId, aInputStream);
				int iHasThrown = theStream.readInt();
				int probeId = theStream.readInt();
				long parentTimeStampFrame = aInputStream.readLong();
				int depth = aInputStream.readInt();
				long currentTimeStamp = aInputStream.readLong();
				int threadId = aInputStream.readInt();
				if (iHasThrown == 1)
				{
					hasThrown = true;
					byte[] theValueS = ValueWriter.serialize(theValue);
					itsLogCollector.register(
							exitId,
							theValueS, 
							currentTimeStamp, 
							false);
					itsLogCollector.behaviorExit(
							threadId, 
							parentTimeStampFrame, 
							(short)depth, 
							currentTimeStamp, 
							null,
							probeId, 
							behaviorId,
							hasThrown, 
							new ObjectId(exitId));
					
				}
				else{
				itsLogCollector.behaviorExit(
						threadId, 
						parentTimeStampFrame, 
						(short)depth, 
						currentTimeStamp, 
						null,
						probeId, 
						behaviorId,
						hasThrown, 
						theValue);
				}
				System.out.println("Registrando return");
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
					int theEvent = itsStream.readInt();
					//System.out.println(theEvent);
					//int theEvent1 = 1;
					switch (theEvent)
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
							methodCall(itsStream);
							break;
						case OBJECT_FUNCTION:
							functionCall(itsStream);
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
						
					case INSTANTIATION_EVENT:
						instantiationEvent(itsStream);
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
