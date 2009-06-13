/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
 */
package tod.impl.bci.asm2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import tod.core.config.ClassSelector;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.database.structure.standard.PrimitiveTypeInfo;
import tod.impl.replay.InScopeMethodReplayer;
import zz.utils.Utils;

public class BCIUtils implements Opcodes
{
	public static final String CLS_EVENTCOLLECTOR = "java/tod/EventCollector";
	public static final String DSC_EVENTCOLLECTOR = "L"+CLS_EVENTCOLLECTOR+";";
	public static final String CLS_AGENTREADY = "java/tod/AgentReady";
	public static final String CLS_EXCEPTIONGENERATEDRECEIVER = "java/tod/ExceptionGeneratedReceiver";
	public static final String CLS_TRACEDMETHODS = "java/tod/TracedMethods";
	public static final String CLS_THREADDATA = "java/tod/ThreadData";
	public static final String DSC_THREADDATA = "L"+CLS_THREADDATA+";";
	public static final String CLS_OBJECT = getJvmClassName(Object.class);
	public static final String DSC_OBJECT = "L"+CLS_OBJECT+";";
	public static final String CLS_THROWABLE = getJvmClassName(Throwable.class);
	public static final String DSC_THROWABLE = "L"+CLS_THROWABLE+";";
	public static final String CLS_STRING = getJvmClassName(String.class);
	public static final String DSC_STRING = "L"+CLS_STRING+";";
	public static final String CLS_CLASS = getJvmClassName(Class.class);
	public static final String DSC_CLASS = "L"+CLS_CLASS+";";
	public static final String CLS_OBJECTID = getJvmClassName(ObjectId.class);
	public static final String DSC_OBJECTID = "L"+CLS_OBJECTID+";";
	public static final String CLS_REPLAYER = getJvmClassName(InScopeMethodReplayer.class);

	private static final Type[] TYPE_FOR_SORT = new Type[11];
	static
	{
		TYPE_FOR_SORT[Type.OBJECT] = Type.getType(Object.class);
		TYPE_FOR_SORT[Type.ARRAY] = Type.getType(Object.class);
		TYPE_FOR_SORT[Type.BOOLEAN] = Type.BOOLEAN_TYPE;
		TYPE_FOR_SORT[Type.BYTE] = Type.BYTE_TYPE;
		TYPE_FOR_SORT[Type.CHAR] = Type.CHAR_TYPE;
		TYPE_FOR_SORT[Type.DOUBLE] = Type.DOUBLE_TYPE;
		TYPE_FOR_SORT[Type.FLOAT] = Type.FLOAT_TYPE;
		TYPE_FOR_SORT[Type.INT] = Type.INT_TYPE;
		TYPE_FOR_SORT[Type.LONG] = Type.LONG_TYPE;
		TYPE_FOR_SORT[Type.SHORT] = Type.SHORT_TYPE;
		TYPE_FOR_SORT[Type.VOID] = Type.VOID_TYPE;
	}

	public static String getJvmClassName(Class<?> aClass)
	{
		String theName = aClass.getName();
		return theName.replace('.', '/');
	}
	
	/**
	 * Return the normal Java class name corresponding to the given internal
	 * name
	 */
	public static String getClassName(String aJVMClassName)
	{
		return Type.getType("L" + aJVMClassName + ";").getClassName();
	}

	public static boolean isInterface(int access)
	{
		return (access & Opcodes.ACC_INTERFACE) != 0;
	}

	public static boolean isStatic(int access)
	{
		return (access & Opcodes.ACC_STATIC) != 0;
	}

	public static boolean isFinal(int access)
	{
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	public static boolean isNative(int access)
	{
		return (access & Opcodes.ACC_NATIVE) != 0;
	}

	public static boolean isPrivate(int access)
	{
		return (access & Opcodes.ACC_PRIVATE) != 0;
	}

	public static boolean isAbstract(int access)
	{
		return (access & Opcodes.ACC_ABSTRACT) != 0;
	}
	
	public static boolean isConstructorCall(AbstractInsnNode aNode)
	{
		if (aNode.getOpcode() == Opcodes.INVOKESPECIAL)
		{
			MethodInsnNode theMethodNode = (MethodInsnNode) aNode;
			if ("<init>".equals(theMethodNode.name)) return true;
		}
		return false;
	}

	/**
	 * Generates the bytecode that pushes the given value onto the stack
	 */
	public static AbstractInsnNode pushInt(int aValue)
	{
		switch (aValue)
		{
		case -1:
			return new InsnNode(ICONST_M1);
		case 0:
			return new InsnNode(ICONST_0);
		case 1:
			return new InsnNode(ICONST_1);
		case 2:
			return new InsnNode(ICONST_2);
		case 3:
			return new InsnNode(ICONST_3);
		case 4:
			return new InsnNode(ICONST_4);
		case 5:
			return new InsnNode(ICONST_5);
		}

		if (aValue >= Byte.MIN_VALUE && aValue <= Byte.MAX_VALUE) return new IntInsnNode(
				BIPUSH,
				aValue);
		else if (aValue >= Short.MIN_VALUE && aValue <= Short.MAX_VALUE) return new IntInsnNode(
				SIPUSH,
				aValue);
		else return new LdcInsnNode(new Integer(aValue));
	}

	/**
	 * Generates the bytecode that pushes the given value onto the stack
	 */
	public static AbstractInsnNode pushLong(long aValue)
	{
		if (aValue == 0) return new InsnNode(LCONST_0);
		else if (aValue == 1) return new InsnNode(LCONST_1);
		else return new LdcInsnNode(new Long(aValue));
	}

	/**
	 * Returns the sort of data (as defined by {@link Type} that correponds to
	 * the given opcode.
	 */
	public static int getSort(int aOpcode)
	{
		switch (aOpcode)
		{
		case AALOAD:
		case AASTORE:
		case ACONST_NULL:
		case ALOAD:
		case ANEWARRAY:
		case ARETURN:
		case ASTORE:
		case IF_ACMPEQ:
		case IF_ACMPNE:
			return Type.OBJECT;

		case BALOAD:
		case BASTORE:
			return Type.BYTE;

		case CALOAD:
		case CASTORE:
			return Type.CHAR;

		case DADD:
		case DALOAD:
		case DASTORE:
		case DCMPG:
		case DCMPL:
		case DCONST_0:
		case DCONST_1:
		case DDIV:
		case DLOAD:
		case DMUL:
		case DNEG:
		case DREM:
		case DRETURN:
		case DSTORE:
		case DSUB:
			return Type.DOUBLE;

		case FADD:
		case FALOAD:
		case FASTORE:
		case FCMPG:
		case FCMPL:
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
		case FDIV:
		case FLOAD:
		case FMUL:
		case FNEG:
		case FREM:
		case FRETURN:
		case FSTORE:
		case FSUB:
			return Type.FLOAT;

		case BIPUSH:
		case IADD:
		case IALOAD:
		case IAND:
		case IASTORE:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
		case ICONST_M1:
		case IDIV:
		case IF_ICMPEQ:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
		case IF_ICMPLT:
		case IF_ICMPNE:
		case IINC:
		case ILOAD:
		case IMUL:
		case INEG:
		case IOR:
		case IREM:
		case IRETURN:
		case ISHL:
		case ISHR:
		case ISTORE:
		case ISUB:
		case IUSHR:
		case IXOR:
			return Type.INT;

		case LADD:
		case LALOAD:
		case LAND:
		case LASTORE:
		case LCMP:
		case LCONST_0:
		case LCONST_1:
		case LDIV:
		case LLOAD:
		case LMUL:
		case LNEG:
		case LOR:
		case LREM:
		case LRETURN:
		case LSHL:
		case LSHR:
		case LSTORE:
		case LSUB:
		case LUSHR:
		case LXOR:
			return Type.LONG;

		case SALOAD:
		case SASTORE:
		case SIPUSH:
			return Type.SHORT;

		default:
			return -1;
		}
	}

	public static Type getType(int aSort)
	{
		return TYPE_FOR_SORT[aSort];
	}

	/**
	 * Returns the primitive type that corresponds to the given operand
	 * 
	 * @param aOperand
	 *            {@link Opcodes#T_BOOLEAN} etc.
	 */
	public static PrimitiveTypeInfo getPrimitiveType(int aOperand)
	{
		switch (aOperand)
		{
		case Opcodes.T_BOOLEAN:
			return PrimitiveTypeInfo.BOOLEAN;
		case Opcodes.T_BYTE:
			return PrimitiveTypeInfo.BYTE;
		case Opcodes.T_CHAR:
			return PrimitiveTypeInfo.CHAR;
		case Opcodes.T_DOUBLE:
			return PrimitiveTypeInfo.DOUBLE;
		case Opcodes.T_FLOAT:
			return PrimitiveTypeInfo.FLOAT;
		case Opcodes.T_INT:
			return PrimitiveTypeInfo.INT;
		case Opcodes.T_LONG:
			return PrimitiveTypeInfo.LONG;
		case Opcodes.T_SHORT:
			return PrimitiveTypeInfo.SHORT;
		default:
			return null;
		}
	}

	public static boolean acceptClass(String aClassName, ClassSelector aSelector)
	{
		return aSelector.accept(getClassName(aClassName));
	}

	/**
	 * Checks that a method is correct (ie. likely to pass the JVM verifier).
	 */
	public static void checkMethod(ClassNode aClassNode, MethodNode aNode)
	{
		Analyzer theAnalyzer = new Analyzer(new BasicVerifier());
		try
		{
			theAnalyzer.analyze(aNode.name, aNode);
		}
		catch (AnalyzerException e)
		{
			Utils.rtex(
					e,
					"Error in %s.%s%s at instruction #%d: %s",
					aClassNode.name,
					aNode.name,
					aNode.desc,
					getBytecodeRank(aNode, e.node),
					e.getMessage());
		}
		catch (Exception e)
		{
			Utils.rtex(
					e,
					"Exception while analyzing %s.%s%s",
					aClassNode.name,
					aNode.name,
					aNode.desc);
		}
	}

	public static int getBytecodeRank(MethodNode aNode, AbstractInsnNode aInstruction)
	{
		if (aInstruction == null) return -1;
		int theRank = 1;
		ListIterator<AbstractInsnNode> theIterator = aNode.instructions.iterator();
		while (theIterator.hasNext())
		{
			AbstractInsnNode theNode = theIterator.next();
			if (theNode == aInstruction) return theRank;

			int theOpcode = theNode.getOpcode();
			if (theOpcode >= 0) theRank++;
		}

		return -1;
	}

	public static void checkClass(byte[] aBytecode)
	{
		// StringWriter sw = new StringWriter();
		// PrintWriter pw = new PrintWriter(sw);
		// CheckClassAdapter.verify(new ClassReader(aBytecode), false, pw);
		//		
		// String theResult = sw.toString();
		// if (theResult.length() != 0)
		// {
		// Utils.rtex(theResult);
		// }
	}

	public static void writeClass(String aRoot, ClassNode aNode, byte[] aData)
	{
		try
		{
			File theDir = new File(aRoot);
			theDir = new File(theDir, "err");
			theDir.mkdirs();

			File theFile = new File(theDir, aNode.name.replace('/', '.') + ".class");
			theFile.getParentFile().mkdirs();
			theFile.createNewFile();
			FileOutputStream theFileOutputStream = new FileOutputStream(theFile);
			theFileOutputStream.write(aData);
			theFileOutputStream.flush();
			theFileOutputStream.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void throwRTEx(SyntaxInsnList s, String aMessage)
	{
		s.NEW("java/lang/RuntimeException");
		s.DUP();
		s.LDC(aMessage);
		s.INVOKESPECIAL("java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
		s.ATHROW();
	}
}
