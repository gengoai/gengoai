package com.gengoai.config;

import com.gengoai.io.Resources;
import com.gengoai.reflection.Reflect;
import lombok.extern.java.Log;

import java.io.IOException;

import static com.gengoai.LogUtils.logWarning;

/**
 * <p>Does a safe <code>Class.forName</code> on entries in <code>META-INF/preload.classes</code></p>
 *
 * @author David B. Bracewell
 */
@Log
public final class Preloader {

   /**
    * Preloads using the current thread's context class loader and the Preloader's, class loader.
    */
   public static void preload() {
      Resources.findAllClasspathResources("META-INF/preload.classes")
               .forEachRemaining(r -> {
                  try {
                     r.readLines().forEach(Reflect::getClassForNameQuietly);
                  } catch(IOException e) {
                     logWarning(log, "Exception Preloading: {0}", e);
                  }
               });
   }

}// END OF Preloader
