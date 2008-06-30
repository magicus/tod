/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng;

import static tod.impl.evdbng.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;

import java.util.Random;

import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.queries.BehaviorCondition;
import tod.impl.evdbng.queries.BytecodeLocationCondition;
import tod.impl.evdbng.queries.CompoundCondition;
import tod.impl.evdbng.queries.Conjunction;
import tod.impl.evdbng.queries.DepthCondition;
import tod.impl.evdbng.queries.Disjunction;
import tod.impl.evdbng.queries.EventCondition;
import tod.impl.evdbng.queries.FieldCondition;
import tod.impl.evdbng.queries.ThreadCondition;
import tod.impl.evdbng.queries.VariableCondition;

/**
 * Randomly generates {@link EventCondition}s.
 * @author gpothier
 */
public class ConditionGenerator
{
	private Random itsRandom;
	private EventGenerator itsEventGenerator;
	
	/**
	 * Current depth of the generated condition.
	 * Not thread-safe!
	 */
	private int itsLevel = 0;
	
	public ConditionGenerator(IMutableStructureDatabase aStructureDatabase, long aSeed)
	{
		this(aSeed, new EventGenerator(aStructureDatabase, aSeed)); 
	}
	
	public ConditionGenerator(long aSeed, EventGenerator aEventGenerator)
	{
		itsRandom = new Random(aSeed);
		itsEventGenerator = aEventGenerator;
	}

	public EventCondition next()
	{
		itsLevel = 0;
		return next(0.5f);
	}
	
	/**
	 * Generates a random condition, with a specified probability of generating
	 * a simple condition (vs. compound contition).
	 */
	public EventCondition next(float aSimpleProbability)
	{
		float f = itsRandom.nextFloat();
		
		return f < aSimpleProbability ?
				nextSimpleCondition()
				: nextCompoundCondition();
	}
	
	public EventCondition nextSimpleCondition()
	{
		switch(itsRandom.nextInt(7))
		{
		case 0: return new BehaviorCondition(itsEventGenerator.genBehaviorId(), genBehaviorRole());
		case 1: return new BytecodeLocationCondition(itsEventGenerator.genBytecodeIndex());
		case 2: return new FieldCondition(itsEventGenerator.genFieldId());
		case 3:
			int theId = itsRandom.nextInt(STRUCTURE_OBJECT_COUNT-1) + 1;
			return SplittedConditionHandler.OBJECTS.createCondition(theId, genObjectRole());
			
		case 4: return new ThreadCondition(itsEventGenerator.genThreadId());
		case 5: return new VariableCondition(itsEventGenerator.genVariableId());
		case 6: return new DepthCondition(itsEventGenerator.genDepth());
		default: throw new RuntimeException("Not handled");
		}
	}
	
	private byte genBehaviorRole()
	{
		switch(itsRandom.nextInt(3))
		{
		case 0: return RoleIndexSet.ROLE_BEHAVIOR_ANY;
		case 1: return RoleIndexSet.ROLE_BEHAVIOR_CALLED;
		case 2: return RoleIndexSet.ROLE_BEHAVIOR_EXECUTED;
		default: throw new RuntimeException("Not handled");
		}
	}
	
	private byte genObjectRole()
	{
		switch(itsRandom.nextInt(5))
		{
		case 0: return RoleIndexSet.ROLE_OBJECT_EXCEPTION;
		case 1: return RoleIndexSet.ROLE_OBJECT_RESULT;
		case 2: return RoleIndexSet.ROLE_OBJECT_TARGET;
		case 3: return RoleIndexSet.ROLE_OBJECT_VALUE;
		case 4: return (byte) itsRandom.nextInt(10);
		default: throw new RuntimeException("Not handled");
		}
	}
	
	public EventCondition nextCompoundCondition()
	{
		itsLevel++;
		if (itsRandom.nextBoolean()) return nextConjunction();
		else return nextDisjunction();
	}
	
	public EventCondition nextConjunction()
	{
		Conjunction theConjunction = new Conjunction(false);
		fillCompoundCondition(theConjunction);
		return theConjunction;
	}
	
	public EventCondition nextDisjunction()
	{
		Disjunction theDisjunction = new Disjunction();
		fillCompoundCondition(theDisjunction);
		return theDisjunction;
	}
	
	private void fillCompoundCondition(CompoundCondition aCondition)
	{
		int theCount = itsRandom.nextInt(9)+1;
		for(int i=0;i<theCount;i++)
		{
			aCondition.addCondition(next(itsLevel < 3 ? 0.9f : 1f));
		}
	}
}
