package com.su.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by su on 17-10-25.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface NoteJsFunction {

    String description() default "";

    NoteFilepath jsFilepath();

    Parameter[] parameters() default {};

    Class resultClass() default String.class;
}
