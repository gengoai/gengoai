package com.gengoai.function;

import java.io.Serializable;

/**
 * Version of Runnable that is serializable and checked
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface CheckedRunnable extends Serializable {

   void run() throws Throwable;

}
