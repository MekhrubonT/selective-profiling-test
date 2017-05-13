package org.jetbrains.test.profiller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mekhrubon on 12.05.2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Profiling {
    // Fullness of methods names
    NameType type() default NameType.MID;

    String ownName() default "defaultName";

    enum NameType {
        SHORT, MID, LONG, OWN
    }
}
