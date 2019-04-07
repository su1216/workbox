package com.su.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by su on 18-1-3.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Parameter {

    String parameter() default "";

    String parameterName() default "";

    boolean parameterRequired() default true;

    Class<?> parameterClass();
}
