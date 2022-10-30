package com.gengoai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an object to be preloaded (i.e. have it's static fields initialized on load as part of Config initialization)
 *
 * @author David B. Bracewell
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Preload {

}// END OF Preload
