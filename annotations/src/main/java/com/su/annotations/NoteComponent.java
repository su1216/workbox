package com.su.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface NoteComponent {

    String description();

    Parameter[] parameters() default {};

    String action() default "";

    int flags() default 0;

    String buildType() default "release";

    String type();
}
