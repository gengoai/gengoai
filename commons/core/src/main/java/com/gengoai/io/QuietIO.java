/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai.io;

import lombok.extern.java.Log;

import java.io.Closeable;

import static com.gengoai.LogUtils.logWarning;

/**
 * IO Operations that suppress exceptions
 *
 * @author David B. Bracewell
 */
@Log
public class QuietIO {

   /**
    * Closes the <code>Closeable</code>ignoring any exception
    *
    * @param closeable thing to close
    */
   public static void closeQuietly(Closeable closeable) {
      if(closeable == null) {
         return;
      }
      try {
         closeable.close();
      } catch(Exception e) {
         logWarning(log, e);
      }
   }

   /**
    * Closes the <code>AutoCloseable</code>ignoring any exception
    *
    * @param closeable thing to close
    */
   public static void closeQuietly(AutoCloseable closeable) {
      if(closeable == null) {
         return;
      }
      try {
         closeable.close();
      } catch(Exception e) {
         logWarning(log, e);
      }
   }

}// END OF QuietIO
