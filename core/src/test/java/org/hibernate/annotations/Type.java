package org.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Stub for unit testing EnumFieldScanner's @Type skip condition.
// The scanner checks annotation class names by string, so this FQN must match exactly.
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {
    String value() default "";
}
