/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.apollo.ml.model.sequence;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Utility class for making suring the CRFSuite library is loaded</p>
 *
 * @author David B. Bracewell
 */
public enum CrfSuiteLoader {
   INSTANCE;

   private AtomicBoolean loaded = new AtomicBoolean(false);

   /**
    * Loads the library if not already loaded.
    */
   public void load() {
      if(!loaded.get()) {
         synchronized(this) {
            if(!loaded.get()) {
               loaded.set(true);
               try {
                  com.gengoai.jcrfsuite.util.CrfSuiteLoader.load();
               } catch(Exception e) {
                  throw new RuntimeException(e);
               }
            }
         }
      }
   }

}//END OF CrfSuiteLoader
