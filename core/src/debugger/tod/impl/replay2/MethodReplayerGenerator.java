/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.replay2;

import static tod.impl.bci.asm2.BCIUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;

import tod.Util;
import tod.core.DebugFlags;
import tod.core.config.TODConfig;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.impl.bci.asm2.BCIUtils;
import tod.impl.bci.asm2.MethodInfo;
import tod.impl.bci.asm2.SyntaxInsnList;
import tod.impl.bci.asm2.MethodInfo.BCIFrame;
import tod.impl.bci.asm2.MethodInfo.NewInvokeLink;
import tod.impl.database.structure.standard.StructureDatabaseUtils;
import zz.utils.SetMap;

public abstract class MethodReplayerGenerator
{
	public static final String REPLAY_CLASS_PREFIX = "$trepl$";
	public static final String SNAPSHOT_CLASS_PREFIX = "$tsnap$";
	public static final String SNAPSHOT_METHOD_NAME = "$tsnap";

	private final TODConfig itsConfig;
	private final IMutableStructureDatabase itsDatabase;
	private final ClassNode itsTarget;
	
	private final IBehaviorInfo itsBehavior;
	private final String itsClassName;
	private final MethodNode itsMethodNode;
	
	private final MethodInfo itsMethodInfo;
	
	private Label lCodeStart;
	private Label lCodeEnd;
//	private Label lExitException = new Label();
	private Label lExit = new Label();
	private List itsOriginalTryCatchNodes;

	private Type[] itsArgTypes;
	private Type itsReturnType;
		
	/**
	 * A variable slot that can hold a normal or double value.
	 */
	private int itsTmpVar;
	
	/**
	 * A temporary variable used to store the target of operations.
	 */
	private int itsTmpTargetVar;
	private int itsTmpValueVar;
	private int itsTmpIndexVar;
	
	/**
	 * Temporarily holds the target of constructor calls
	 */
	private int[] itsTmpTargetVars;
	
	/**
	 * Additional instructions that should be added at the end of the main method
	 * after instrumentation is completed.
	 */
	private InsnList itsAdditionalInstructions = new InsnList();
	
	private int itsThreadReplayerSlot;
	private int itsSaveArgsSlots;
	
	/**
	 * Maps field keys (see {@link #getFieldKey(FieldInsnNode)}) to the corresponding cache slot. 
	 */
	private Map<String, Integer> itsFieldCacheMap = new HashMap<String, Integer>();
	
	public MethodReplayerGenerator(
			TODConfig aConfig, 
			IMutableStructureDatabase aDatabase,
			IBehaviorInfo aBehavior,
			String aClassName, 
			MethodNode aMethodNode)
	{
		itsConfig = aConfig;
		itsDatabase = aDatabase;
		itsBehavior = aBehavior;
		itsClassName = aClassName;
		itsMethodNode = aMethodNode;
		
		itsArgTypes = Type.getArgumentTypes(itsMethodNode.desc);
		itsReturnType = Type.getReturnType(itsMethodNode.desc);
		itsStatic = BCIUtils.isStatic(itsMethodNode.access);
		itsConstructor = "<init>".equals(itsMethodNode.name);

		itsTarget = new ClassNode();
		itsTarget.sourceFile = itsTarget.name+".class";
		itsTarget.superName = CLS_OBJECT;
		itsTarget.methods.add(itsMethodNode);
		itsTarget.version = Opcodes.V1_5;
		itsTarget.access = Opcodes.ACC_PUBLIC;
		
		itsMethodInfo = new MethodInfo(itsDatabase, itsClassName, itsMethodNode);
		
		itsThreadReplayerSlot = (Type.getArgumentsAndReturnSizes(itsMethodNode.desc) >> 2) - 1 + (itsStatic ? 0 : 1);
	}
	
	protected String getClassName()
	{
		return REPLAY_CLASS_PREFIX+itsBehavior.getId();
	}
	
	protected MethodInfo getMethodInfo()
	{
		return itsMethodInfo;
	}
	
	protected MethodNode getMethodNode()
	{
		return itsMethodNode;
	}
	
	protected int getBehaviorId()
	{
		return itsBehavior.getId();
	}

	protected abstract boolean sendAllEvents();
	
	/**
	 * Computes the maximum number of local slots needed to save invocation
	 * arguments.
	 */
	private int computeMaxSaveArgsSpace(InsnList aInsns)
	{
		int theMax = 0;
		ListIterator<AbstractInsnNode> theIterator = aInsns.iterator();
		while(theIterator.hasNext()) 
		{
			AbstractInsnNode theNode = theIterator.next();
			int theOpcode = theNode.getOpcode();
			
			switch(theOpcode)
			{
			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKESPECIAL:
			case Opcodes.INVOKESTATIC:
			case Opcodes.INVOKEINTERFACE:
				MethodInsnNode theMethodNode = (MethodInsnNode) theNode;
				int theSize = Type.getArgumentsAndReturnSizes(theMethodNode.desc) >> 2;
				if (theOpcode != Opcodes.INVOKESTATIC) theSize++;
				if (theSize > theMax) theMax = theSize;
				break;
			}
		}
		
		return theMax;
	}


	public TODConfig getConfig()
	{
		return itsConfig;
	}
	
	public IMutableStructureDatabase getDatabase()
	{
		return itsDatabase;
	}
	
	public byte[] generate()
	{
		if (ThreadReplayer.ECHO && ThreadReplayer.ECHO_FORREAL) System.out.println("Generating replayer for: "+itsClassName+"."+itsMethodNode.name);
		
		itsTarget.name = getClassName();

		itsOriginalTryCatchNodes = itsMethodNode.tryCatchBlocks;
		itsMethodNode.tryCatchBlocks = new ArrayList();
		
		lCodeStart = new Label();
		LabelNode nStart = new LabelNode(lCodeStart);
		lCodeStart.info = nStart;
		itsMethodNode.instructions.insert(nStart);
		
		// If the original method is non-static, the generated method takes the original "this" as a parameter.
		if (! itsStatic) itsMethodNode.maxLocals++;
		itsMethodNode.maxLocals++; // For the ThreadReplayer arg.

		int theSlotsCount = itsMethodInfo.setupLocalCacheSlots(itsMethodNode.maxLocals);
		itsMethodNode.maxLocals += theSlotsCount;
		
		int theSaveArgsSpace = computeMaxSaveArgsSpace(itsMethodNode.instructions);
		itsSaveArgsSlots = nextFreeVar(theSaveArgsSpace);

		allocVars();
		itsMethodNode.maxStack = itsMethodNode.maxLocals+8;
		
		itsTmpTargetVars = new int[itsMethodInfo.getMaxNewInvokeNesting()+1];
//		for (int i=0;i<itsTmpTargetVars.length;i++) itsTmpTargetVars[i] = nextFreeVar(1);
		
		// Add OOS invoke method
		addOutOfScopeInvoke();
		addPartialReplayInvoke();
		
		// Modify method
		processInstructions(itsMethodNode.instructions);

		lCodeEnd = new Label();
		LabelNode nEnd = new LabelNode(lCodeEnd);
		lCodeEnd.info = nEnd;
		itsMethodNode.instructions.add(nEnd);
		
		// Setup/cleanup/handlers
		addSnapshotSetup(itsMethodNode.instructions);
		addExceptionHandling(itsMethodNode.instructions);
		addEvents(itsMethodNode.instructions);
		if (DebugFlags.USE_FIELD_CACHE) itsMethodNode.instructions.insert(itsMethodInfo.getFieldCacheInitInstructions());
		itsMethodNode.instructions.add(itsAdditionalInstructions);

		// Setup infrastructure
		MethodSignature theSignature = getReplayMethodSignature(itsBehavior);
		itsMethodNode.name = theSignature.name;
		itsMethodNode.desc = theSignature.descriptor;
		itsMethodNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		itsMethodNode.exceptions = Collections.EMPTY_LIST;
		
		// Update debug info (local vars are shifted by 1)
		for(Iterator<LocalVariableNode> theIterator = itsMethodNode.localVariables.iterator();theIterator.hasNext();)
		{
			LocalVariableNode theNode = theIterator.next();
			theNode.index = transformSlot(theNode.index);
			if ("this".equals(theNode.name)) theNode.name = "$this$";
		}
		itsMethodNode.localVariables.add(new LocalVariableNode("this", "L"+itsClassName+";", null, nStart, nEnd, 0));
		
		// Output the modified class
		ClassWriter theWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		itsTarget.accept(theWriter);
		
		byte[] theBytecode = theWriter.toByteArray();

		BCIUtils.writeClass(TODConfig.TMPDIR+"/replayer/"+getClassDumpSubpath(), itsTarget.name, theBytecode);

		// Check the methods
		try
		{
			BCIUtils.checkClass(theBytecode);
			for(MethodNode theNode : (List<MethodNode>) itsTarget.methods) 
				BCIUtils.checkMethod(itsTarget, theNode, new ReplayerVerifier(), false);
		}
		catch(Exception e)
		{
			System.err.println("Class "+itsTarget.name+" failed check.");
			e.printStackTrace();
		}
		
		return theBytecode;
	}
	
	/**
	 * Returns the subpath to use when storing classes generated by this generator.
	 */
	protected abstract String getClassDumpSubpath();
	
	protected void allocVars()
	{
		itsTmpVar = nextFreeVar(2);
		itsTmpTargetVar = nextFreeVar(1);
		itsTmpValueVar = nextFreeVar(2);
		itsTmpIndexVar = nextFreeVar(1);
	}
	
	protected Label getCodeStartLabel()
	{
		return lCodeStart;
	}
	
	/**
	 * Returns the type corresponding to the given sort.
	 * If the sort corresponds to object or array, returns {@link ObjectId}
	 */
	private Type getTypeOrId(int aSort)
	{
		return BCIUtils.getType(aSort, TYPE_OBJECTID);
	}
	
	/**
	 * Adds a method that reads arguments from the stream before calling the actual invoke method
	 */
	private void addOutOfScopeInvoke()
	{
		MethodNode theMethod = new MethodNode();

		theMethod.name = "invoke_OOS";
		theMethod.desc = "("+DSC_THREADREPLAYER+")V";
		theMethod.exceptions = Collections.EMPTY_LIST;
		theMethod.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		theMethod.tryCatchBlocks = Collections.EMPTY_LIST;
		
		int theSize = 1;
		
		SList s = new SList();
		
		boolean theSendThis = !itsStatic && !itsConstructor;
		
		int theArgCount = itsArgTypes.length;
		if (theSendThis) theArgCount++;

		if (theArgCount > 0)
		{
			s.ALOAD(0);
			s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "waitArgs", "("+DSC_THREADREPLAYER+")V");
			
			if (! itsStatic)
			{
				if (! itsConstructor) s.invokeReadRef(0);
				else 
				{
					s.ALOAD(0);
					s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "nextTmpId", "("+DSC_THREADREPLAYER+")"+BCIUtils.DSC_TMPOBJECTID);
				}
				theSize++;
			}
			
			for (Type theType : itsArgTypes)
			{
				s.invokeRead(theType, 0);
				theSize += theType.getSize();
			}
		}
		else if (!itsStatic && itsConstructor)
		{
			s.ALOAD(0);
			s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "nextTmpId", "("+DSC_THREADREPLAYER+")"+BCIUtils.DSC_TMPOBJECTID);
			theSize++;
		}
		
		MethodSignature theSignature = getReplayMethodSignature(itsBehavior);
		s.ALOAD(0);
		s.INVOKESTATIC(itsTarget.name, theSignature.name, theSignature.descriptor);
		if (itsReturnType.getSort() != Type.VOID) s.POP(itsReturnType);
		s.RETURN();

		theMethod.maxLocals = 1;
		theMethod.maxStack = theSize;
		
		theMethod.instructions = s;
		itsTarget.methods.add(theMethod);
	}
	
	/**
	 * Similar to {@link #addOutOfScopeInvoke()}, but passes dummy arguments,
	 * as actual values will be obtained from a snapshot
	 */
	private void addPartialReplayInvoke()
	{
		MethodNode theMethod = new MethodNode();
		
		theMethod.name = "invoke_PartialReplay";
		theMethod.desc = "("+DSC_THREADREPLAYER+")V";
		theMethod.exceptions = Collections.EMPTY_LIST;
		theMethod.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		theMethod.tryCatchBlocks = Collections.EMPTY_LIST;
		
		int theSize = 1;
		
		SList s = new SList();
				
		if (! itsStatic)
		{
			s.ACONST_NULL();
			theSize++;
		}
		
		for (Type theType : itsArgTypes)
		{
			s.pushDefaultValue(theType);
			theSize += theType.getSize();
		}
		
		MethodSignature theSignature = getReplayMethodSignature(itsBehavior);
		s.ALOAD(0);
		s.INVOKESTATIC(itsTarget.name, theSignature.name, theSignature.descriptor);
		if (itsReturnType.getSort() != Type.VOID) s.POP(itsReturnType);
		s.RETURN();
		
		theMethod.maxLocals = 1;
		theMethod.maxStack = theSize;
		
		theMethod.instructions = s;
		itsTarget.methods.add(theMethod);
	}
	
	/**
	 * Returns a key that represents the start and end label of the try-catch block
	 */
	private static String getTryCatchBlockKey(InsnList aInstructions, TryCatchBlockNode aNode)
	{
		return ""+aInstructions.indexOf(aNode.start)+"_"+aInstructions.indexOf(aNode.end);
	}
	
	/**
	 * Replaces the exception types for every handler 
	 * (uses {@link HandlerReachedException}), and fixes the handler code as needed.
	 */
	private void addExceptionHandling(InsnList aInsns)
	{
		final Map<Label, Integer> theHandlerIds = new HashMap<Label, Integer>();
		SetMap<String, TryCatchBlockNode> theRegionData = new SetMap<String, TryCatchBlockNode>();
		
		// Assign ids to handlers and register the handlers for each region
		int theNextId = 0;
		int nHandlers = itsOriginalTryCatchNodes.size();
		for(int i=0;i<nHandlers;i++)
		{
			TryCatchBlockNode theNode = (TryCatchBlockNode) itsOriginalTryCatchNodes.get(i);
			Label theLabel = getLabel(theNode.handler);
			
			Integer theId = theHandlerIds.get(theLabel);
			if (theId == null)
			{
				theId = theNextId++;
				theHandlerIds.put(theLabel, theId);
			}
			
			String theKey = getTryCatchBlockKey(itsMethodNode.instructions, theNode);
			theRegionData.add(theKey, theNode);
		}

		SList s = new SList();

		Label lDefault = new Label();
		s.label(lDefault);
		s.createRTEx("Invalid handler id");
		s.ATHROW();
		
		// For each region we keep a single handler and create a dispatching switch statement
		// We iterate again on the original handlers, as we need to maintain the ordering
		for(int i=0;i<nHandlers;i++)
		{
			TryCatchBlockNode theOriginalNode = (TryCatchBlockNode) itsOriginalTryCatchNodes.get(i);
			String theKey = getTryCatchBlockKey(itsMethodNode.instructions, theOriginalNode);
		
			Set<TryCatchBlockNode> theSet = theRegionData.get(theKey);
			TryCatchBlockNode theFirstNode = theSet.iterator().next();
			
			Label lDispatcher = new Label();
			s.label(lDispatcher);

			s.DUP();
			s.GETFIELD(CLS_HANDLERREACHED, "exception", BCIUtils.DSC_OBJECTID);
			s.SWAP();
			s.GETFIELD(CLS_HANDLERREACHED, "handlerId", "I");
			
			int n = theSet.size();
			TryCatchBlockNode[] theNodes = theSet.toArray(new TryCatchBlockNode[n]);
			Arrays.sort(theNodes, new Comparator<TryCatchBlockNode>()
			{
				private int getValue(TryCatchBlockNode aNode)
				{
					Label theLabel = getLabel(aNode.handler);
					return theHandlerIds.get(theLabel); 
				}
				
				public int compare(TryCatchBlockNode n1, TryCatchBlockNode n2)
				{
					return getValue(n1) - getValue(n2);
				}
			});
			
			int[] theValues = new int[n];
			Label[] theLabels = new Label[n];
			
			int j=0;
			for(TryCatchBlockNode theNode : theNodes)
			{
				Label theLabel = getLabel(theNode.handler);
				theLabels[j] = theLabel;
				theValues[j] = theHandlerIds.get(theLabel); 
				j++;
			}
			
			s.LOOKUPSWITCH(lDefault, theValues, theLabels);
			
			itsMethodNode.visitTryCatchBlock(getLabel(theFirstNode.start), getLabel(theFirstNode.end), lDispatcher, CLS_HANDLERREACHED);
		}
		
//		s.label(lExitException);
//		s.POP();
//		s.ALOAD(itsThreadReplayerSlot);
//		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectException", "("+DSC_THREADREPLAYER+")V");
//		s.pushDefaultValue(itsReturnType);
//		s.RETURN(itsReturnType);
//
//		itsMethodNode.visitTryCatchBlock(lCodeStart, lCodeEnd, lExitException, CLS_BEHAVIOREXITEXCEPTION);

		aInsns.add(s);
	}
	
	private void addEvents(InsnList aInsns)
	{
		SList s = new SList();

		Label lStart = new Label();
		Label lFinally = new Label();
		Label lEnd = new Label();

		// Insert entry instructions
		{
			if (sendAllEvents())
			{
				pushCollector(s);
				s.DUP();
				s.ASTORE(itsTmpVar);
				
				s.pushInt(getBehaviorId());
				int theArgsCount = itsArgTypes.length;
				if (! itsStatic) theArgsCount++;
				s.pushInt(theArgsCount);
				s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "enter", "(II)V");
				
				int theSlot = 0;
				if (! itsStatic)
				{
					s.ALOAD(itsTmpVar);
					s.ALOAD(0);
					invokeValue(s, TYPE_OBJECTID);
					theSlot++;
				}
				for(Type theType : itsArgTypes)
				{
					s.ALOAD(itsTmpVar);
					s.ILOAD(theType, theSlot);
					invokeValue(s, theType);
					theSlot += theType.getSize();
				}
			}
			else
			{
				pushCollector(s);
				s.LDC(getBehaviorId());
				s.pushInt(0);
				s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "enter", "(II)V");
			}

			s.GOTO(lStart);
		}
		
		// Insert exit instructions (every return statement is replaced by a GOTO to this block)
		{
			s.label(lExit);
			
			pushCollector(s);
			s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "exit", "()V");

			s.RETURN(Type.getReturnType(getMethodNode().desc));
		}
		
		// Insert finally instructions
		{
			s.label(lFinally);
			
			pushCollector(s);
			s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "exitException", "()V");

			s.ATHROW();
		}

		s.label(lStart);

		aInsns.insert(s);
		
		SList s2 = new SList();
		s2.label(lEnd);
		s2.NOP();
		
		aInsns.add(s2);
		getMethodNode().visitTryCatchBlock(lStart, lEnd, lFinally, null);

	}
	
	protected void addAdditionalInstructions(InsnList aInsns)
	{
		itsAdditionalInstructions.add(aInsns);
	}
	
	private int getFieldCacheSlot(FieldInsnNode aNode)
	{
		Integer theSlot = itsFieldCacheMap.get(getFieldKey(aNode));
		return theSlot != null ? theSlot.intValue() : -1;
	}
	
	private String getFieldKey(IFieldInfo aField)
	{
		return aField.getDeclaringType().getName()+"_"+aField.getName();
	}
	
	private String getFieldKey(FieldInsnNode aNode)
	{
		return aNode.owner+"_"+aNode.name;
	}

	protected int nextFreeVar(int aSize)
	{
		int theVar = itsMethodNode.maxLocals;
		itsMethodNode.maxLocals += aSize;
		return theVar;
	}
	
	/**
	 * Generates the bytecode that pushes the current collector on the stack.
	 */
	private void pushCollector(SList s)
	{
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKEVIRTUAL(CLS_THREADREPLAYER, "getCollector", "()"+DSC_EVENTCOLLECTOR_REPLAY);
	}

	/**
	 * Invokes one of the value() methods of {@link EventCollector}. 
	 * Assumes that the collector and the value are on the stack
	 * @param s
	 */
	public static void invokeValue(SList s, Type aType)
	{
		s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "value", "("+getActualReplayType(aType).getDescriptor()+")V");
	}
	
	private void processInstructions(InsnList aInsns)
	{
		ListIterator<AbstractInsnNode> theIterator = aInsns.iterator();
		while(theIterator.hasNext()) 
		{
			AbstractInsnNode theNode = theIterator.next();
			int theOpcode = theNode.getOpcode();
			
			switch(theOpcode)
			{
			case Opcodes.ATHROW:
				processThrow(aInsns, (InsnNode) theNode);
				break;
				
			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKESPECIAL:
			case Opcodes.INVOKESTATIC:
			case Opcodes.INVOKEINTERFACE:
				processInvoke(aInsns, (MethodInsnNode) theNode);
				break;

			case Opcodes.NEWARRAY:
			case Opcodes.ANEWARRAY:
				processNewArray(aInsns, theNode, 1);
				break;
				
			case Opcodes.MULTIANEWARRAY:
				processNewArray(aInsns, theNode, ((MultiANewArrayInsnNode) theNode).dims);
				break;
				
			case Opcodes.NEW:
				processNew(aInsns, (TypeInsnNode) theNode);
				break;
				
			case Opcodes.GETFIELD:
			case Opcodes.GETSTATIC:
				processGetField(aInsns, (FieldInsnNode) theNode);
				break;
				
			case Opcodes.PUTFIELD:
			case Opcodes.PUTSTATIC:
				processPutField(aInsns, (FieldInsnNode) theNode);
				break;
				
			case Opcodes.IALOAD:
			case Opcodes.LALOAD:
			case Opcodes.FALOAD:
			case Opcodes.DALOAD:
			case Opcodes.AALOAD:
			case Opcodes.BALOAD:
			case Opcodes.CALOAD:
			case Opcodes.SALOAD:
				processGetArray(aInsns, (InsnNode) theNode);
				break;
				
			case Opcodes.ARRAYLENGTH:
				processArrayLength(aInsns, (InsnNode) theNode);
				break;
				
			case Opcodes.IASTORE:
			case Opcodes.LASTORE:
			case Opcodes.FASTORE:
			case Opcodes.DASTORE:
			case Opcodes.AASTORE:
			case Opcodes.BASTORE:
			case Opcodes.CASTORE:
			case Opcodes.SASTORE:
				processPutArray(aInsns, (InsnNode) theNode);
				break;
				
			case Opcodes.ILOAD:
			case Opcodes.LLOAD:
			case Opcodes.FLOAD:
			case Opcodes.DLOAD:
			case Opcodes.ALOAD:
				processGetVar(aInsns, (VarInsnNode) theNode);
				break;
				
			case Opcodes.ISTORE:
			case Opcodes.LSTORE:
			case Opcodes.FSTORE:
			case Opcodes.DSTORE:
			case Opcodes.ASTORE:
				processPutVar(aInsns, (VarInsnNode) theNode);
				break;
				
			case Opcodes.IINC:
				processIinc(aInsns, (IincInsnNode) theNode);
				break;
				
			case Opcodes.LDC:
				processLdc(aInsns, (LdcInsnNode) theNode);
				break;
				
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ACMPNE:
				processIfAcmp(aInsns, (JumpInsnNode) theNode);
				break;
				
			case Opcodes.IDIV:
			case Opcodes.LDIV:
			case Opcodes.IREM:
			case Opcodes.LREM:
				processDiv(aInsns, (InsnNode) theNode);
				break;
				
			case Opcodes.MONITORENTER:
			case Opcodes.MONITOREXIT:
				processMonitor(aInsns, (InsnNode) theNode);
				break;
				
			case Opcodes.INSTANCEOF:
				processInstanceOf(aInsns, (TypeInsnNode) theNode);
				break;
				
			case Opcodes.CHECKCAST:
				processCheckCast(aInsns, (TypeInsnNode) theNode);
				break;
				
			case Opcodes.RET:
				processRet(aInsns, (VarInsnNode) theNode);
				break;
				
			case Opcodes.IRETURN:
			case Opcodes.LRETURN:
			case Opcodes.FRETURN:
			case Opcodes.DRETURN:
			case Opcodes.ARETURN:
			case Opcodes.RETURN:
				processReturn(aInsns, (InsnNode) theNode);
				break;

			}
		}
	}

	private void processThrow(InsnList aInsns, InsnNode aNode)
	{
		SList s = new SList();

		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectException", "("+DSC_THREADREPLAYER+")V");
		s.pushDefaultValue(itsReturnType);
		s.RETURN(itsReturnType);

		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	/**
	 * Returns the behavior invoked by the given node.
	 */
	private IBehaviorInfo getTargetBehavior(MethodInsnNode aNode)
	{
		IClassInfo theClass = getDatabase().getClass(Util.jvmToScreen(aNode.owner), false);
		if (theClass == null) return null;
		IBehaviorInfo theBehavior = theClass.getBehavior(aNode.name, aNode.desc);
		return theBehavior;
	}
	
	protected int getThreadReplayerSlot()
	{
		return itsThreadReplayerSlot;
	}
	
	private boolean isCalleeInScope(MethodInsnNode aNode)
	{
		return getDatabase().isInScope(aNode.owner);
	}

	private class InvocationInfo
	{
		public final boolean isStatic;
		public final boolean isChaining;
		public final boolean isConstructor;
		public final boolean expectObjectInitialized;
		
		public InvocationInfo(MethodInsnNode aNode)
		{
			isStatic = aNode.getOpcode() == Opcodes.INVOKESTATIC;
			isChaining = itsMethodInfo.isChainingInvocation(aNode);
			
			isConstructor = "<init>".equals(aNode.name);
			
			expectObjectInitialized = 
				isConstructor 
				&& ! isChaining
				&& ! getDatabase().isInScope(aNode.owner);
		}
	}
	
	private void processInvoke(InsnList aInsns, MethodInsnNode aNode)
	{
		InvocationInfo invocationInfo = new InvocationInfo(aNode);
		
		SList s = new SList();
		
		// Add ThreadReplayer arg
		s.ALOAD(itsThreadReplayerSlot);

		MethodSignature theSignature = getDispatchMethodSignature(
				aNode.desc, 
				invocationInfo.isStatic, 
				invocationInfo.isConstructor);
		
		s.INVOKESTATIC(CLS_THREADREPLAYER, theSignature.name, theSignature.descriptor);
		
		InsnNode theSnapshotProbePlaceholder = new InsnNode(Opcodes.NOP); 
		s.add(theSnapshotProbePlaceholder);
		
		if (! isCalleeInScope(aNode))
		{
			if (invocationInfo.expectObjectInitialized)
			{
				NewInvokeLink theNewInvokeLink = itsMethodInfo.getNewInvokeLink(aNode);
				int theLevel = theNewInvokeLink.getNestingLevel();
				int theVar = itsTmpTargetVars[theLevel]; // Should have been initialized in processNew
	
				// Do wait
				s.ALOAD(itsThreadReplayerSlot);
				s.ALOAD(theVar);
				s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "waitObjectInitialized", "("+DSC_THREADREPLAYER+DSC_OBJECTID+")V");
			}
			else if (invocationInfo.isChaining)
			{
				s.ALOAD(itsThreadReplayerSlot);
				s.ALOAD(0); // Original "this" 
				s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "waitConstructorTarget", "("+DSC_THREADREPLAYER+DSC_OBJECTID+")V");
			}
		}
		
		// Insert snapshot probe
		SList s2 = new SList();
		insertSnapshotProbe(s2, aNode, true);
		s.insert(theSnapshotProbePlaceholder, s2);
		s.remove(theSnapshotProbePlaceholder);
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	/**
	 * Returns the type of all the stack slots in the given frame.
	 */
	protected static Type[] getStackTypes(BCIFrame aFrame)
	{
		int theSize = aFrame.getStackSize();
		Type[] theResult = new Type[theSize];
		for(int i=0;i<theSize;i++) theResult[i] = getActualReplayType(aFrame.getStack(i).getType());
		return theResult;
	}
	
	/**
	 * Saves as many stack slots as there are items in the types array.
	 */
	protected void genSaveStack(SList s, Type[] aSlotTypes)
	{
		genSaveArgs(s, aSlotTypes, true);
	}
	
	private void genSaveArgs(SList s, Type[] aArgTypes, boolean aStatic)
	{
		int theSlot = itsSaveArgsSlots;
		for(int i=aArgTypes.length-1;i>=0;i--)
		{
			Type theType = aArgTypes[i];
			s.ISTORE(theType, theSlot);
			theSlot += theType.getSize();
		}
		if (! aStatic) s.ASTORE(theSlot);
	}
	
	/**
	 * Loads as many stack slots as there are items in the types array.
	 */
	protected void genLoadStack(SList s, Type[] aSlotTypes)
	{
		genLoadArgs(s, aSlotTypes, true);
	}
	
	private void genLoadArgs(SList s, Type[] aArgTypes, boolean aStatic)
	{
		int theSlot = itsSaveArgsSlots;
		for(int i=0;i<aArgTypes.length;i++) theSlot += aArgTypes[i].getSize();
		if (! aStatic) s.ALOAD(theSlot);
		for(int i=0;i<aArgTypes.length;i++) 
		{
			Type theType = aArgTypes[i];
			theSlot -= theType.getSize();
			s.ILOAD(theType, theSlot);
		}
	}
	
	protected void genReverseLoadStack(SList s, Type[] aArgTypes)
	{
		int theSlot = itsSaveArgsSlots;
		for(int i=aArgTypes.length-1;i>=0;i--)
		{
			Type theType = aArgTypes[i];
			s.ILOAD(theType, theSlot);
			theSlot += theType.getSize();
		}
	}
	
//	public static MethodSignature getInvokeMethodSignature(boolean aStatic, Type[] aArgTypes, Type aReturnType)
//	{
//		List<Type> theArgTypes = new ArrayList<Type>();
//		if (! aStatic) theArgTypes.add(TYPE_OBJECTID); // First arg is the target
//		for (Type theType : aArgTypes) theArgTypes.add(getActualReplayType(theType));
//		
//		return new MethodSignature(
//				"invoke"+SUFFIX_FOR_SORT[aReturnType.getSort()]+(aStatic ? "_S" : ""),
//				Type.getMethodDescriptor(
//						getActualReplayType(aReturnType), 
//						theArgTypes.toArray(new Type[theArgTypes.size()])));
//	}

	public static MethodSignature getDispatchMethodSignature(String aDescriptor, boolean aStatic, boolean aConstructor)
	{
		Type[] theArgumentTypes = Type.getArgumentTypes(aDescriptor);
		Type theReturnType = Type.getReturnType(aDescriptor);
		
		List<Type> theArgTypes = new ArrayList<Type>();
		if (! aStatic) theArgTypes.add(TYPE_OBJECTID); // First arg is the target
		for (Type theType : theArgumentTypes) theArgTypes.add(getReplayDispatchType(theType));
		theArgTypes.add(TYPE_THREADREPLAYER); 
		
		return new MethodSignature(
				"dispatch_"+getCompleteSigForType(theReturnType)+(aStatic ? "_S" : "")+(aConstructor ? "_c" : ""),
				Type.getMethodDescriptor(
						getReplayDispatchType(theReturnType), 
						theArgTypes.toArray(new Type[theArgTypes.size()])));
	}
	
	public static MethodSignature getOOSDispatchMethodSignature(IBehaviorInfo aBehavior)
	{
		String theDescriptor = aBehavior.getDescriptor();
		Type[] theArgTypes = Type.getArgumentTypes(theDescriptor);
		Type theReturnType = Type.getReturnType(theDescriptor);
		
		char[] sig = new char[theArgTypes.length];
		for(int i=0;i<sig.length;i++) sig[i] = getCompleteSigForType(theArgTypes[i]);
		
		return new MethodSignature(
				"dispatch_"+new String(sig)+"_"+getCompleteSigForType(theReturnType)
					+(aBehavior.isStatic() ? "_S" : "")+(aBehavior.isConstructor() ? "_c" : "")
					+"_OOS",
				"(I"+DSC_THREADREPLAYER+")V");
	}
	
	public static MethodSignature getReplayMethodSignature(IBehaviorInfo aBehavior)
	{
		String theDescriptor = aBehavior.getDescriptor();
		Type[] theArgumentTypes = Type.getArgumentTypes(theDescriptor);
		Type theReturnType = Type.getReturnType(theDescriptor);
		
		List<Type> theArgTypes = new ArrayList<Type>();
		if (! aBehavior.isStatic()) theArgTypes.add(TYPE_OBJECTID); 
		for (Type theType : theArgumentTypes) theArgTypes.add(getReplayDispatchType(theType));
		theArgTypes.add(TYPE_THREADREPLAYER);
		
		return new MethodSignature(
				"replay",
				Type.getMethodDescriptor(
						getReplayDispatchType(theReturnType), 
						theArgTypes.toArray(new Type[theArgTypes.size()])));
	}
	
	
	private static final String[] SUFFIX_FOR_SORT = new String[11];
	private boolean itsStatic;
	private boolean itsConstructor;
	
	static
	{
		SUFFIX_FOR_SORT[Type.OBJECT] = "Ref";
		SUFFIX_FOR_SORT[Type.ARRAY] = "Ref";
		SUFFIX_FOR_SORT[Type.BOOLEAN] = "Boolean";
		SUFFIX_FOR_SORT[Type.BYTE] = "Byte";
		SUFFIX_FOR_SORT[Type.CHAR] = "Char";
		SUFFIX_FOR_SORT[Type.DOUBLE] = "Double";
		SUFFIX_FOR_SORT[Type.FLOAT] = "Float";
		SUFFIX_FOR_SORT[Type.INT] = "Int";
		SUFFIX_FOR_SORT[Type.LONG] = "Long";
		SUFFIX_FOR_SORT[Type.SHORT] = "Short";
		SUFFIX_FOR_SORT[Type.VOID] = "Void";
	}
	
	private void processNew(InsnList aInsns, TypeInsnNode aNode)
	{
		SList s = new SList();
		
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "nextTmpId_skipClassloading", "("+DSC_THREADREPLAYER+")"+BCIUtils.DSC_TMPOBJECTID);
		
		NewInvokeLink theNewInvokeLink = itsMethodInfo.getNewInvokeLink(aNode);
		MethodInsnNode theInvoke = theNewInvokeLink.getInvokeInsn();
		if (! isCalleeInScope(theInvoke))
		{
			InvocationInfo invocationInfo = new InvocationInfo(theInvoke);
			if (invocationInfo.expectObjectInitialized)
			{
				// Save the target, which will be used during the constructor invocation
				int theLevel = theNewInvokeLink.getNestingLevel();
				int theVar = itsTmpTargetVars[theLevel];
				if (theVar == 0)
				{
					theVar = nextFreeVar(1);
					itsTmpTargetVars[theLevel] = theVar;
				}
				s.DUP();
				s.ASTORE(theVar);
				
				// Add the variable to all the frame between the NEW and the INVOKE so that it is
				// picked up for snapshots
				AbstractInsnNode theNode = aNode;
				do
				{
					theNode = theNode.getNext();
					BCIFrame theFrame = getMethodInfo().getFrame(theNode);
					theFrame.setExtraLocal(theVar-1, TYPE_OBJECTID); //-1 because the slot is transformed when generating snapshot code
				} while(theNode != theInvoke);
			}
		}
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}

	private void processNewArray(InsnList aInsns, AbstractInsnNode aNode, int aDimensions)
	{
		SList s = new SList();
		
		for (int i=0;i<aDimensions;i++) s.POP(); // Pop array size(s)
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectNewArray", "("+DSC_THREADREPLAYER+")"+BCIUtils.DSC_OBJECTID);
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	/**
	 * LDC of class constant can throw an exception/cause class loading 
	 */
	private void processLdc(InsnList aInsns, LdcInsnNode aNode)
	{
		if (! (aNode.cst instanceof Type) && ! (aNode.cst instanceof String)) return;
		
		SList s = new SList();
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectConstant", "("+DSC_THREADREPLAYER+")"+DSC_OBJECTID);
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	private void processInstanceOf(InsnList aInsns, TypeInsnNode aNode)
	{
		SList s = new SList();
		s.POP();
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectInstanceofOutcome", "("+DSC_THREADREPLAYER+")I");
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	
	private Label getLabel(LabelNode aNode)
	{
		Label theLabel = aNode.getLabel();
		theLabel.info = aNode;
		return theLabel;
	}
	
	/**
	 * References are transformed into {@link ObjectId} so we must compare ids.
	 */
	private void processIfAcmp(InsnList aInsns, JumpInsnNode aNode)
	{
		SList s = new SList();

		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "cmpId", "("+BCIUtils.DSC_OBJECTID+BCIUtils.DSC_OBJECTID+")Z");
		Label theLabel = getLabel(aNode.label);
		switch(aNode.getOpcode())
		{
		case Opcodes.IF_ACMPEQ: s.IFtrue(theLabel); break;
		case Opcodes.IF_ACMPNE: s.IFfalse(theLabel); break;
		default:
			throw new RuntimeException("Not handled: "+aNode);
		}
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	private void processGetField(InsnList aInsns, FieldInsnNode aNode)
	{
		Type theType = getTypeOrId(Type.getType(aNode.desc).getSort());
		String theExpectMethodName = "expectAndSend"+SUFFIX_FOR_SORT[theType.getSort()]+"FieldRead";

		SList s = new SList();

		if (aNode.getOpcode() == Opcodes.GETSTATIC) s.ACONST_NULL(); // Push "null" target
		s.ALOAD(itsThreadReplayerSlot);
		s.SWAP();
		s.pushInt(StructureDatabaseUtils.getFieldSlotIndex(itsDatabase, aNode.owner, aNode.name, true));
		
		if (DebugFlags.USE_FIELD_CACHE)
		{
			Integer theCacheSlot = itsMethodInfo.getCacheSlot(aNode);
			s.ILOAD(theCacheSlot);
			String theExpectMethodDesc = "("+DSC_THREADREPLAYER+DSC_OBJECTID+"I"+theType.getDescriptor()+")"+theType.getDescriptor();
			s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, theExpectMethodName, theExpectMethodDesc);
		}
		else
		{
			String theExpectMethodDesc = "("+DSC_THREADREPLAYER+DSC_OBJECTID+"I)"+theType.getDescriptor();
			s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, theExpectMethodName, theExpectMethodDesc);
		}

		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	private void processPutField(InsnList aInsns, FieldInsnNode aNode)
	{
		Type theType = getTypeOrId(Type.getType(aNode.desc).getSort());
		
		SList s = new SList();
		s.ISTORE(theType, itsTmpValueVar);
		if (aNode.getOpcode() == Opcodes.PUTSTATIC) s.ACONST_NULL(); // Push "null" target
		s.ASTORE(itsTmpTargetVar); // Store target
		
		// Register event
		pushCollector(s);
		s.DUP();
		
		s.ALOAD(itsTmpTargetVar);
		s.LDC(StructureDatabaseUtils.getFieldSlotIndex(itsDatabase, aNode.owner, aNode.name, true));
		s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "fieldWrite", "("+DSC_OBJECTID+"I)V");
		
		s.ILOAD(theType, itsTmpValueVar);
		invokeValue(s, theType);
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	private void processPutArray(InsnList aInsns, InsnNode aNode)
	{
		Type theElementType = getTypeOrId(BCIUtils.getSort(aNode.getOpcode()));
		
		SList s = new SList();
		s.ISTORE(theElementType, itsTmpValueVar);
		s.ISTORE(itsTmpIndexVar);
		s.ASTORE(itsTmpTargetVar); // Store target
		
		// Register event
		pushCollector(s);
		s.DUP();
		
		s.ALOAD(itsTmpTargetVar);
		s.ILOAD(itsTmpIndexVar);
		s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "arrayWrite", "("+DSC_OBJECTID+"I)V");
		
		s.ILOAD(theElementType, itsTmpValueVar);
		invokeValue(s, theElementType);

		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	


	private void processGetArray(InsnList aInsns, InsnNode aNode)
	{
		Type theType = getTypeOrId(BCIUtils.getSort(aNode.getOpcode()));
		String theExpectMethodName = "expectAndSend"+SUFFIX_FOR_SORT[theType.getSort()]+"ArrayRead";

		SList s = new SList();

		s.ISTORE(itsTmpIndexVar);
		s.ASTORE(itsTmpTargetVar); // Store target

		s.ALOAD(itsThreadReplayerSlot);
		s.ALOAD(itsTmpTargetVar);
		s.ILOAD(itsTmpIndexVar);
		String theExpectMethodDesc = "("+DSC_THREADREPLAYER+DSC_OBJECTID+"I)"+theType.getDescriptor();
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, theExpectMethodName, theExpectMethodDesc);

		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}

	private void processArrayLength(InsnList aInsns, InsnNode aNode)
	{
		SList s = new SList();

		s.POP(); // Pop target
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectArrayLength", "("+DSC_THREADREPLAYER+")I");
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}
	
	protected int transformSlot(int aSlot)
	{
		return aSlot < itsThreadReplayerSlot ? aSlot : aSlot+1;
	}
	
	private void processGetVar(InsnList aInsns, VarInsnNode aNode)
	{
		int theOriginalVar = aNode.var;
		int theTransformedVar = transformSlot(theOriginalVar);
		aNode.var = theTransformedVar;
	}

	private static boolean isStackTopRet(BCIFrame aFrame)
	{
		int theStackSize = aFrame.getStackSize();
		return aFrame.getStack(theStackSize-1).getBasicValue() == BasicValue.RETURNADDRESS_VALUE;
	}
	
	private static boolean isLocalRet(BCIFrame aFrame, int aSlot)
	{
		return aFrame.getLocal(aSlot).getBasicValue() == BasicValue.RETURNADDRESS_VALUE;
	}
	
	private void processPutVar(InsnList aInsns, VarInsnNode aNode)
	{
		int theOriginalVar = aNode.var;
		int theTransformedVar = transformSlot(theOriginalVar);
		aNode.var = theTransformedVar;
		
		BCIFrame theFrame = itsMethodInfo.getFrame(aNode);
		if (sendAllEvents() && ! isStackTopRet(theFrame))
		{
			Type theType = getTypeOrId(BCIUtils.getSort(aNode.getOpcode()));
			
			SList s = new SList();
			s.ISTORE(theType, theTransformedVar);
			
			// Register event
			pushCollector(s);
			s.DUP();
			
			s.LDC(theOriginalVar);
			s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "localWrite", "(I)V");
			
			s.ILOAD(theType, theTransformedVar);
			invokeValue(s, theType);
			
			aInsns.insert(aNode, s);
			aInsns.remove(aNode);
		}
	}
	
	private void processIinc(InsnList aInsns, IincInsnNode aNode)
	{
		int theOriginalVar = aNode.var;
		int theTransformedVar = transformSlot(theOriginalVar);
		aNode.var = theTransformedVar;

		BCIFrame theFrame = itsMethodInfo.getFrame(aNode);
		if (sendAllEvents() && ! isLocalRet(theFrame, theOriginalVar))
		{
			SList s = new SList();
			s.IINC(theTransformedVar, aNode.incr);
			
			// Register event
			pushCollector(s);
			s.DUP();
			
			s.LDC(theOriginalVar);
			s.INVOKEVIRTUAL(CLS_EVENTCOLLECTOR_REPLAY, "localWrite", "(I)V");
			
			s.ILOAD(theTransformedVar);
			invokeValue(s, Type.INT_TYPE);
			
			aInsns.insert(aNode, s);
			aInsns.remove(aNode);
		}
	}

	/**
	 * We need to check if an exception is going to be thrown.
	 */
	private void processDiv(InsnList aInsns, InsnNode aNode)
	{
		Type theType = BCIUtils.getType(BCIUtils.getSort(aNode.getOpcode()), null);

		SList s = new SList();
		Label lNormal = new Label();
		s.DUP(theType);
		switch(theType.getSort()) 
		{
		case Type.BOOLEAN:
		case Type.BYTE:
		case Type.CHAR:
		case Type.SHORT:
		case Type.INT: break;
		
		case Type.LONG:
			s.pushLong(0);
			s.LCMP();
			break;
		
		default: throw new RuntimeException("Unexpected type: "+theType);
		}
		
		s.IFNE(lNormal);
		
		{
			// An arithmetic exception will occur
			s.POP(theType);
			s.POP(theType);
			
			s.ALOAD(itsThreadReplayerSlot);
			s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "expectException", "("+DSC_THREADREPLAYER+")V");
			s.pushDefaultValue(itsReturnType);
			s.RETURN(itsReturnType);
		}
		
		s.label(lNormal);
		aInsns.insertBefore(aNode, s);
	}
	
	private void processCheckCast(InsnList aInsns, TypeInsnNode aNode)
	{
		SList s = new SList();
		s.ALOAD(itsThreadReplayerSlot);
		s.INVOKESTATIC(CLS_INSCOPEREPLAYERFRAME, "checkCast", "("+DSC_THREADREPLAYER+")V");

		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}

	
	private void processMonitor(InsnList aInsns, InsnNode aNode)
	{
		SList s = new SList();
		s.POP();
		
		aInsns.insert(aNode, s);
		aInsns.remove(aNode);
	}

	private void processRet(InsnList aInsns, VarInsnNode aNode)
	{
		aNode.var = transformSlot(aNode.var);
	}
	
	private void processReturn(InsnList aInsns, InsnNode aNode)
	{
		SyntaxInsnList s = new SyntaxInsnList();
		s.GOTO(lExit);
		
		replace(aInsns, aNode, s);
	}
	
	private void replace(InsnList aInsns, AbstractInsnNode aNode, InsnList aList)
	{
		aInsns.insert(aNode, aList);
		aInsns.remove(aNode);
	}

	protected abstract void addSnapshotSetup(InsnList aInsns);
	protected abstract void insertSnapshotProbe(SList s, AbstractInsnNode aReferenceNode, boolean aSaveStack);
	
	public static class MethodSignature
	{
		public final String name;
		public final String descriptor;
		
		public MethodSignature(String aName, String aDescriptor)
		{
			name = aName;
			descriptor = aDescriptor;
		}
	}
}
