/*
 * Copyright (c) 2000-2004 Eric Tanter (etanter@dcc.uchile.cl)
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package reflex.lib.logging.core.impl.mop;

import reflex.api.ReflexConfig;
import reflex.lib.logging.core.impl.mop.behavior.ConstructorLogger;
import reflex.lib.logging.core.impl.mop.behavior.MethodLogger;
import reflex.lib.logging.core.impl.mop.behavior.MsgSendLogger;
import reflex.lib.logging.core.impl.mop.field.FieldWriteLogger;
import reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier;
import reflex.lib.logging.core.impl.mop.instantiation.InstantiationLogger;
import reflex.lib.logging.core.impl.mop.locals.LocalVariableWriteLogger;
import tod.core.ILogCollector;
import tod.core.PrintLogCollector;
import tod.core.config.StaticConfig;

/**
 * @author gpothier
 */
public class Config extends ReflexConfig
{
	public static ILogCollector COLLECTOR = new PrintLogCollector();
	
	@Override
	public void initReflex()
	{		
		StaticConfig.getInstance().readConfig();
    	StaticConfig.getInstance().freeze();

   		ObjectIdentifier.getInstance().setup(this);
    	
    	if (StaticConfig.getInstance().getLogMethods())
    	{
    		MsgSendLogger.getInstance().setup(this);
    		MethodLogger.getInstance().setup(this);
    		ConstructorLogger.getInstance().setup(this);
    	}
    	
    	if (StaticConfig.getInstance().getLogFieldWrite())
    	{
    		FieldWriteLogger.getInstance().setup(this);
    	}

    	if (StaticConfig.getInstance().getLogLocalVariableWrite())
    	{
    		LocalVariableWriteLogger.getInstance().setup(this);
    	}
    	
    	if (StaticConfig.getInstance().getLogInstantiations())
    	{
    		InstantiationLogger.getInstance().setup(this);
    	}
    }
    
}
