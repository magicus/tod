/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.monitoring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Probe 
{
	AggregationType aggr() default AggregationType.NONE;
	String key() default "";
}
