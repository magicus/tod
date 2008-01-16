/*
TOD plugin - Eclipse pluging for TOD
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.plugin;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIClassType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.omg.CosNaming.IstringHelper;

import tod.utils.TODUtils;

/**
 * Utility class that permits to asynchronously reveal particualr source locations
 * @author gpothier
 */
public class SourceRevealerUtils
{
	private static SourceRevealerUtils INSTANCE = new SourceRevealerUtils();

	public static SourceRevealerUtils getInstance()
	{
		return INSTANCE;
	}

	private SourceRevealerUtils()
	{
	}
	
	private Revealer itsCurentRevealer;
	private boolean itsRevealScheduled = false;
	
	private void reveal (Revealer aRevealer)
	{
		itsCurentRevealer = aRevealer;
		if (! itsRevealScheduled)
		{
			itsRevealScheduled = true;
			Display.getDefault().asyncExec(new Runnable ()
					{
						public void run()
						{
							try
							{
								itsCurentRevealer.reveal();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							itsRevealScheduled = false;
						}
					});
		}
	}
	
	/**
	 * Reveal the source code using Debug API. Only for JDT projects.
	 */
	public static void reveal (
			final ILaunch aLaunch, 
			final String aTypeName, 
			final int aLineNumber)
	{
		TODUtils.log(1,"[SourceRevealerUtils.reveal(ILaunch, String, int)]"+aTypeName+" "+aLineNumber);
		getInstance().reveal (new Revealer()
		{
			public void reveal() 
			{
				FakeStackFrame theArtifact = new FakeStackFrame(
						aLaunch,
						aTypeName, 
						aLineNumber);
				
				ISourceLookupResult theResult = DebugUITools.lookupSource(
						theArtifact, 
						aLaunch.getSourceLocator());
				
				DebugUITools.displaySource(theResult, JavaPlugin.getActivePage());
			}
		});
	}
	
	/**
	 * Opens a given method. Should be safe for JDT and PDE projects. 
	 */
	public static void reveal (
			final List<IJavaProject> aJavaProject, 
			final String aTypeName, 
			final String aMethodName)
	{
		TODUtils.log(1,"[SourceRevealerUtils.reveal(IJavaProject, String, String)]" +aTypeName +" "+aMethodName);
		getInstance().reveal (new Revealer()
				{
					public void reveal() throws CoreException, BadLocationException
					{
						IType theType = TODPluginUtils.getType(aJavaProject, aTypeName);
						if (theType == null) {
							TODUtils.logf(0, "The type %s has not been found in the available sources " +
									"of the Eclipse workspace.\n Path were " +
									"to find the sources was: %s", aTypeName, aJavaProject);
							return;
						}
						
						IMethod theMethod = theType.getMethod(aMethodName, null);
						if (theMethod == null){
							TODUtils.logf(0, "The method %s of type %s has not been found in the available sources " +
									"of the Eclipse workspace.", aMethodName, aTypeName);
							return;
						}
						
						// Eclipse 3.3 only
					//	JavaUI.openInEditor(theMethod, false, true);
						
						EditorUtility.openInEditor(theMethod, false);
					}
				});
	}
	
	/**
	 * Opens a given location. Should be safe for JDT and PDE projects. 
	 */
	public static void reveal (
			final List<IJavaProject> aJavaProject, 
			final String aTypeName, 
			final int aLineNumber)
	{
		TODUtils.log(1,"[SourceRevealerUtils.reveal(IJavaProject, String, int)]" +aTypeName +" "+aLineNumber );
		getInstance().reveal (new Revealer()
		{
			public void reveal() throws CoreException, BadLocationException
			{
				IType theType = TODPluginUtils.getType(aJavaProject, aTypeName);
				if (theType == null) {
					TODUtils.logf(0, "The type %s has not been found in the available sources " +
							"of the Eclipse workspace.\n Path were " +
							"to find the sources was: %s", aTypeName, aJavaProject);
					return;
				}
				
				// Eclipse 3.3 only
				//IEditorPart theEditor = JavaUI.openInEditor(theType, false, false);
				
				IEditorPart theEditor = EditorUtility.openInEditor(theType, false);
				if (theEditor instanceof ITextEditor)
				{
					ITextEditor theTextEditor = (ITextEditor) theEditor;
					IDocumentProvider theProvider= theTextEditor.getDocumentProvider();
					IDocument theDocument = theProvider.getDocument(theTextEditor.getEditorInput());
					int theStart= theDocument.getLineOffset(aLineNumber);
					theTextEditor.selectAndReveal(theStart, 0);
				}
			}
		});
	}
	
	private interface Revealer
	{
		public void reveal() throws CoreException, BadLocationException;
	}
	
	/**
	 * This class is a hack that makes Eclipse think we have real stack frames.
	 * This code is rather fragile and highly depends on the inner workings of
	 * Eclipse. It works with Eclipse 3.2.1. 
	 * @author gpothier
	 */
	private static class FakeStackFrame implements IJavaStackFrame
	{
		private final ILaunch itsLaunch;
		private final String itsTypeName;
		private final int itsLineNumber;
		
		private FakeThread itsThread;
		private FakeReferenceType itsType;

		public FakeStackFrame(
				final ILaunch aLaunch, 
				final String aTypeName, 
				final int aLineNumber)
		{
			itsLaunch = aLaunch;
			itsTypeName = aTypeName;
			itsLineNumber = aLineNumber;
			itsThread = new FakeThread(this);
			itsType = new FakeReferenceType(this);
		}
		
		public String getSourcePath() throws DebugException
		{
			return null;
		}
		
		public String getDeclaringTypeName() throws DebugException
		{
			return itsTypeName;
		}
		
		public String getModelIdentifier()
		{
			return "org.eclipse.jdt.debug";
		}
		
		public int getCharStart() throws DebugException
		{
			return -1;
		}
		
		public int getCharEnd() throws DebugException
		{
			return -1;
		}
		
		public int getLineNumber() throws DebugException
		{
			return itsLineNumber;
		}
		
		public IThread getThread()
		{
			return itsThread;
		}
		
		public boolean isTerminated()
		{
			return true;
		}

		public Object getAdapter(Class adapter)
		{
			if (adapter == IJavaStackFrame.class) return this;
			throw new UnsupportedOperationException();
		}

		public boolean isObsolete() throws DebugException
		{
			return false;
		}
		
		public IJavaReferenceType getReferenceType() throws DebugException
		{
			return itsType;
		}

		public String getReceivingTypeName() throws DebugException
		{
			return itsTypeName;
		}
		
		public String getMethodName() throws DebugException
		{
			return null;
		}

		public List getArgumentTypeNames() throws DebugException
		{
			return Collections.EMPTY_LIST;
		}

		public boolean wereLocalsAvailable()
		{
			return false;
		}

		public boolean isNative() throws DebugException
		{
			return false;
		}

		public boolean isOutOfSynch() throws DebugException
		{
			return false;
		}

		public ILaunch getLaunch()
		{
			return itsLaunch;
		}
		
		public boolean canStepInto()
		{
			throw new UnsupportedOperationException();
		}

		public boolean canStepOver()
		{
			throw new UnsupportedOperationException();
		}

		public boolean canStepReturn()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isStepping()
		{
			throw new UnsupportedOperationException();
		}

		public void stepInto() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public void stepOver() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public void stepReturn() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canResume()
		{
			throw new UnsupportedOperationException();
		}

		public boolean canSuspend()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isSuspended()
		{
			throw new UnsupportedOperationException();
		}

		public void resume() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public void suspend() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canStepWithFilters()
		{
			throw new UnsupportedOperationException();
		}

		public void stepWithFilters() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canDropToFrame()
		{
			throw new UnsupportedOperationException();
		}

		public void dropToFrame() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canTerminate()
		{
			throw new UnsupportedOperationException();
		}

		public void terminate() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IDebugTarget getDebugTarget()
		{
			throw new UnsupportedOperationException();
		}

		public IJavaVariable findVariable(String aVariableName) throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IJavaClassType getDeclaringType() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public int getLineNumber(String aStratum) throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IJavaVariable[] getLocalVariables() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public String getSignature() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public String getSourceName() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public String getSourceName(String aStratum) throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public String getSourcePath(String aStratum) throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IJavaObject getThis() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isConstructor() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isStaticInitializer() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isSynchronized() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isVarArgs() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean supportsDropToFrame()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isFinal() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isPackagePrivate() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isPrivate() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isProtected() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isPublic() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isStatic() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean isSynthetic() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public String getName() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IRegisterGroup[] getRegisterGroups() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IVariable[] getVariables() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean hasRegisterGroups() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean hasVariables() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canForceReturn()
		{
			throw new UnsupportedOperationException();
		}

		public void forceReturn(IJavaValue aValue) throws DebugException
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class FakeThread implements IThread
	{
		private final FakeStackFrame itsFrame;

		public FakeThread(FakeStackFrame aFrame)
		{
			itsFrame = aFrame;
		}

		public IStackFrame getTopStackFrame() throws DebugException
		{
			return itsFrame;
		}


		public IBreakpoint[] getBreakpoints()
		{
			throw new UnsupportedOperationException();
		}

		public String getName() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public int getPriority() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IStackFrame[] getStackFrames() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean hasStackFrames() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canStepInto()
		{
			throw new UnsupportedOperationException();
		}

		public boolean canStepOver()
		{
			throw new UnsupportedOperationException();
		}

		public boolean canStepReturn()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isStepping()
		{
			throw new UnsupportedOperationException();
		}

		public void stepInto() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public void stepOver() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public void stepReturn() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canTerminate()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isTerminated()
		{
			throw new UnsupportedOperationException();
		}

		public void terminate() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public boolean canResume()
		{
			throw new UnsupportedOperationException();
		}

		public boolean canSuspend()
		{
			throw new UnsupportedOperationException();
		}

		public boolean isSuspended()
		{
			throw new UnsupportedOperationException();
		}

		public void resume() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public void suspend() throws DebugException
		{
			throw new UnsupportedOperationException();
		}

		public IDebugTarget getDebugTarget()
		{
			throw new UnsupportedOperationException();
		}

		public ILaunch getLaunch()
		{
			throw new UnsupportedOperationException();
		}

		public String getModelIdentifier()
		{
			throw new UnsupportedOperationException();
		}

		public Object getAdapter(Class adapter)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class FakeReferenceType implements IJavaReferenceType
	{
		private FakeStackFrame itsFrame;

		public FakeReferenceType(FakeStackFrame aFrame)
		{
			itsFrame = aFrame;
		}

		public String getDefaultStratum() throws DebugException
		{
			return "Java";
		}

		public String[] getAllFieldNames() throws DebugException
		{
			return null;
		}

		public String[] getAvailableStrata() throws DebugException
		{
			return null;
		}

		public IJavaObject getClassLoaderObject() throws DebugException
		{
			return null;
		}

		public IJavaClassObject getClassObject() throws DebugException
		{
			return null;
		}

		public String[] getDeclaredFieldNames() throws DebugException
		{
			return null;
		}

		public IJavaFieldVariable getField(String aName) throws DebugException
		{
			return null;
		}

		public String getGenericSignature() throws DebugException
		{
			return null;
		}

		public String getSourceName() throws DebugException
		{
			return null;
		}

		public String[] getSourceNames(String aStratum) throws DebugException
		{
			return null;
		}

		public String[] getSourcePaths(String aStratum) throws DebugException
		{
			return null;
		}

		public String getName() throws DebugException
		{
			return null;
		}

		public String getSignature() throws DebugException
		{
			return null;
		}

		public IDebugTarget getDebugTarget()
		{
			return null;
		}

		public ILaunch getLaunch()
		{
			return null;
		}

		public String getModelIdentifier()
		{
			return null;
		}

		public Object getAdapter(Class adapter)
		{
			return null;
		}

		public IJavaObject[] getInstances(long aMax) throws DebugException
		{
			return null;
		}
	}
	
}


