package com.cheercent.xtomcat.httpserver.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XLogicConfig {

    String name() default "";
    
    ActionMethod method() default ActionMethod.GET;
    
    String[] requiredParameters() default {};
    
    boolean requiredPeerid() default true;
    
    String[] allow() default {};
    
    int version() default 1;
    
	public enum ActionMethod {
		GET, POST
	}
}