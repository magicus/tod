package tod.impl.bci.asm;

import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IBehaviorInfo.BytecodeTagType;

/**
 * Implements the mechanism to transform soot attributes into tod tags.
 * @author gpothier
 */
public abstract class Tagger
{
	public static final Tagger INSTRUCTION_KIND = new SootInstructionKindTagger();
	public static final Tagger INSTRUCTION_SHADOW = new SootInstructionShadowTagger();
	public static final Tagger INSTRUCTION_SOURCE = new SootInstructionSourceTagger();
	
	public abstract <T> T getTag(BytecodeTagType<T> aType, int aValue);
	
	/**
	 * Handles the instruction kind soot tag provided by the abc compiler.
	 * It indicates the role of each bytecode
	 * @author gpothier
	 */
	private static class SootInstructionKindTagger extends Tagger
	{
		@Override
		public <T> T getTag(BytecodeTagType<T> aType, int aValue)
		{
			if (aType == BytecodeTagType.BYTECODE_ROLE)
			{
				// Constants come from abc.weaving.tagkit.InstructionKindTag
				switch (aValue)
				{
				case 0: return (T) BytecodeRole.BASE_CODE;
				case 1: return (T) BytecodeRole.ADVICE_EXECUTE;
				case 2: return (T) BytecodeRole.ADVICE_ARG_SETUP;
				case 3: return (T) BytecodeRole.ADVICE_TEST;
				case 36: return (T) BytecodeRole.INLINED_ADVICE;
				default: return (T) BytecodeRole.UNKNOWN;
				}
			}
			else return null;
		}
		
	}
	
	/**
	 * Handles the instruction shadow soot tag provided by the abc compiler.
	 * @author gpothier
	 */
	private static class SootInstructionShadowTagger extends Tagger
	{
		@Override
		public <T> T getTag(BytecodeTagType<T> aType, int aValue)
		{
			if (aType == BytecodeTagType.INSTR_SHADOW)
			{
				return (T) (Integer) aValue;
			}
			else return null;
		}
	}
	
	/**
	 * Handles the instruction source soot tag provided by the abc compiler.
	 * @author gpothier
	 */
	private static class SootInstructionSourceTagger extends Tagger
	{
		@Override
		public <T> T getTag(BytecodeTagType<T> aType, int aValue)
		{
			if (aType == BytecodeTagType.INSTR_SHADOW)
			{
				return (T) (Integer) aValue;
			}
			else return null;
		}
	}
	

}