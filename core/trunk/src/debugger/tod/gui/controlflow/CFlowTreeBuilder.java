/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import reflex.lib.logging.miner.gui.formatter.EventFormatter;
import tod.core.model.event.EventUtils;
import tod.core.model.event.IAfterMethodCallEvent;
import tod.core.model.event.IBeforeMethodCallEvent;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IBehaviorExitEvent;
import tod.core.model.event.IParentEvent;
import tod.core.model.event.IExceptionGeneratedEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.event.ILocalVariableWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.TypeInfo;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

/**
 * Permits to build the nodes that represent events in a CFlow tree.
 * @author gpothier
 */
public class CFlowTreeBuilder
{
	private static final MatcherBuilder[] BUILDERS = {
		new InstantiationBuilder1(), new InstantiationBuilder2(),
		new MethodBuilder1(), new MethodBuilder2(), new MethodBuilder3(),
	};
	
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(12);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, 14);
	
	private CFlowView itsView;
	
	public CFlowTreeBuilder(CFlowView aView)
	{
		itsView = aView;
	}

	public IRectangularGraphicObject buildRootNode (IParentEvent aRootEvent)
	{
		return new RootEventNode(itsView, (IParentEvent) aRootEvent);		
	}
	
	public List<IRectangularGraphicObject> buildNodes (IParentEvent aContainer)
	{
		List<IRectangularGraphicObject> theNodes = new ArrayList<IRectangularGraphicObject>();
		
		List<ILogEvent> theChildren = aContainer.getChildren();
		if (theChildren == null || theChildren.size() == 0) return theNodes;
		
		EventIterator theIterator = new EventIterator(theChildren);
		while (theIterator.hasNext())
		{
			IRectangularGraphicObject theNode = buildNode(theIterator);
			if (theNode != null) 
			{
				theNodes.add(theNode);
				theNode.checkValid();
			}
		}		
		
		return theNodes;
	}

	private IRectangularGraphicObject buildNode(EventIterator aIterator)
	{
		ILogEvent theNextEvent = aIterator.next();
		
		if (theNextEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) theNextEvent;
			return new FieldWriteNode(itsView, theEvent);
		}
		else if (theNextEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) theNextEvent;
			return new LocalVariableWriteNode(itsView, theEvent);
		}
		else if (theNextEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) theNextEvent;
			if (EventUtils.isIgnorableException(theEvent)) return null;
			else return new ExceptionGeneratedNode(itsView, theEvent);
		}
		else if (theNextEvent instanceof IBehaviorExitEvent)
		{
			if (! aIterator.hasNext()) return null;
			else throw new RuntimeException("Case not handled: behavior exit not at end");
		}
		else
		{
			aIterator.rewind(1);
			
			// Search behavior enter and after method call event.
			for (MatcherBuilder theBuilder : BUILDERS)
			{
				IRectangularGraphicObject theResult = 
					theBuilder.matchAndBuild (aIterator, itsView);
				
				if (theResult != null) return theResult;				
			}
		}

		aIterator.next();
		String theText = "Not handled: "+EventFormatter.getInstance().getPlainText(theNextEvent);
		return SVGFlowText.create(theText, FONT, Color.RED);
	}

	/**
	 * Represents a particular kind of event sequence.
	 * @see CFlowTreeBuilder#matchAndBuild(List, CFlowView)
	 * @author gpothier
	 */
	private static abstract class MatcherBuilder
	{
		/**
		 * Check if the current events of the given iterator match, and if so builds
		 * the corresponding nodes. Otherwise returns null.
		 * @param aView TODO
		 */
		public abstract IRectangularGraphicObject matchAndBuild (
				EventIterator aIterator, 
				CFlowView aView);
		
	}
	
	private static abstract class ClassMatcherBuilder extends MatcherBuilder
	{
		private Class[] itsClasses;
		
		public ClassMatcherBuilder(Class... aClasses)
		{
			itsClasses = aClasses;
		}

		public abstract IRectangularGraphicObject buildNode(
				CFlowView aView, 
				ILogEvent[] aEvents);
		
		
		/**
		 * Checks if the events at the current iterator position match the given classes.
		 * If they match, they are returned in an array corresponding
		 * to the given classes array and the iterator's position is incremented; 
		 * otherwise the method returns null and the iterator's position is not incremented.
		 */
		protected ILogEvent[] match(EventIterator aIterator)
		{
			ILogEvent[] theEvents = new ILogEvent[itsClasses.length];
			
			int i = 0;
			while (aIterator.hasNext() && i < itsClasses.length)
			{
				ILogEvent theEvent = aIterator.next();
				if (itsClasses[i].isInstance(theEvent))
				{
					theEvents[i] = theEvent;
					i++;					
				}
				else 
				{
					aIterator.rewind (i+1);
					return null;
				}
			}
			if (i < itsClasses.length)
			{
				aIterator.rewind (i);
				return null;
			}
			
			return theEvents;
		}
		

		@Override
		public IRectangularGraphicObject matchAndBuild(
				EventIterator aIterator, 
				CFlowView aView)
		{
			ILogEvent[] theEvents = match(aIterator);
			if (theEvents != null) return buildNode(aView, theEvents);
			else return null;
		}
	}
	
	/**
	 * This builder matches the case where we have before and after method call events surrounding
	 * a behavior enter event. 
	 * @author gpothier
	 */
	private static class MethodBuilder1 extends ClassMatcherBuilder
	{
		public MethodBuilder1()
		{
			super(IBeforeMethodCallEvent.class, IBehaviorEnterEvent.class, IAfterMethodCallEvent.class);
		}

		@Override
		public IRectangularGraphicObject buildNode(CFlowView aView, ILogEvent[] aEvents)
		{
			IBeforeMethodCallEvent theBeforeMethodCallEvent = (IBeforeMethodCallEvent) aEvents[0];
			IBehaviorEnterEvent theBehaviorEnterEvent = (IBehaviorEnterEvent) aEvents[1];
			IAfterMethodCallEvent theAfterMethodCallEvent = (IAfterMethodCallEvent) aEvents[2];
			
			return new MethodCallNode(
					aView,
					theBeforeMethodCallEvent,
					theBehaviorEnterEvent,
					theAfterMethodCallEvent);
		}
	}
	
	/**
	 * This builder matches the case where we have before and after method call events 
	 * without a behavior enter event. 
	 * @author gpothier
	 */
	private static class MethodBuilder2 extends ClassMatcherBuilder
	{
		public MethodBuilder2()
		{
			super(IBeforeMethodCallEvent.class, IAfterMethodCallEvent.class);
		}
		
		@Override
		public IRectangularGraphicObject buildNode(CFlowView aView, ILogEvent[] aEvents)
		{
			IBeforeMethodCallEvent theBeforeMethodCallEvent = (IBeforeMethodCallEvent) aEvents[0];
			IAfterMethodCallEvent theAfterMethodCallEvent = (IAfterMethodCallEvent) aEvents[1];
			
			return new MethodCallNode(
					aView,
					theBeforeMethodCallEvent,
					theAfterMethodCallEvent);
		}
	}
	
	/**
	 * This builder matches a single behavior enter event. 
	 * @author gpothier
	 */
	private static class MethodBuilder3 extends ClassMatcherBuilder
	{
		public MethodBuilder3()
		{
			super(IBehaviorEnterEvent.class);
		}
		
		@Override
		public IRectangularGraphicObject buildNode(CFlowView aView, ILogEvent[] aEvents)
		{
			IBehaviorEnterEvent theEvent = (IBehaviorEnterEvent) aEvents[0];
			
			return new MethodCallNode(aView, theEvent);
		}
	}
	
	private static class InstantiationBuilder1 extends ClassMatcherBuilder
	{
		public InstantiationBuilder1()
		{
			super (IInstantiationEvent.class);
		}

		@Override
		public IRectangularGraphicObject buildNode(CFlowView aView, ILogEvent[] aEvents)
		{
			IInstantiationEvent theInstantiationEvent = (IInstantiationEvent) aEvents[0];
			
			return new InstantiationNode(aView, theInstantiationEvent);
		}
	}
	
	/**
	 * Matches a behavior enter whose behavior is &lt;init&gt; followed by an instantiation
	 * @author gpothier
	 */
	private static class InstantiationBuilder2 extends ClassMatcherBuilder
	{
		public InstantiationBuilder2()
		{
			super(IBehaviorEnterEvent.class, IInstantiationEvent.class);
		}
		
		@Override
		protected ILogEvent[] match(EventIterator aIterator)
		{
			ILogEvent[] theEvents = super.match(aIterator);
			if (theEvents != null)
			{
				IBehaviorEnterEvent theBehaviorEnterEvent = (IBehaviorEnterEvent) theEvents[0];
				IInstantiationEvent theInstantiationEvent = (IInstantiationEvent) theEvents[1];
				
				BehaviorInfo theBehavior = theBehaviorEnterEvent.getBehavior();
				TypeInfo theType = theBehavior.getType();
				
				if ("<init>".equals(theBehavior.getName()) && theType == theInstantiationEvent.getType())
				{
					return theEvents;
				}
				else
				{
					aIterator.rewind(2);
					return null;
				}
			}
			else return null;
		}

		@Override
		public IRectangularGraphicObject buildNode(CFlowView aView, ILogEvent[] aEvents)
		{
			IBehaviorEnterEvent theBehaviorEnterEvent = (IBehaviorEnterEvent) aEvents[0];
			IInstantiationEvent theInstantiationEvent = (IInstantiationEvent) aEvents[1];
			
			BehaviorInfo theBehavior = theBehaviorEnterEvent.getBehavior();
			TypeInfo theType = theBehavior.getType();
			
			return new InstantiationNode(
					aView,
					theInstantiationEvent,
					theBehaviorEnterEvent);
		}
	}
	
	
	private static class EventIterator 
	{
		private ListIterator<ILogEvent> itsIterator;
		
		public EventIterator(List<ILogEvent> aEvents)
		{
			itsIterator = aEvents.listIterator();
		}

		public ILogEvent next()
		{
			return itsIterator.next();
		}
		
		public boolean hasNext()
		{
			return itsIterator.hasNext();
		}
		
		public void rewind(int aAmount)
		{
			for (int i=0;i<aAmount;i++) itsIterator.previous();
		}
	}
	
}
