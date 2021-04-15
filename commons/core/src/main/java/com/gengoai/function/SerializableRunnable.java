package com.gengoai.function;

import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface SerializableRunnable extends Runnable, Serializable {


   static SerializableRunnable chain(SerializableRunnable r1, SerializableRunnable r2) {
      return () -> {
         r1.run();
         r2.run();
      };
   }

}
