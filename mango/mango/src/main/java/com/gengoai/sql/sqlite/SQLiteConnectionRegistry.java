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

package com.gengoai.sql.sqlite;

import com.gengoai.io.ResourceMonitor;
import lombok.NonNull;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Registry to prevent more than one SQLite connection from being opened to the same database. Also will auto close the
 * connection when there are no longer any references to the connection in memory.
 */
public final class SQLiteConnectionRegistry {
   private static final ReentrantLock lock = new ReentrantLock();
   public static final Map<String, WeakReference<Connection>> registry = new ConcurrentHashMap<>();

   public static void cleanup() {
      lock.lock();
      registry.values().removeIf(w -> w.get() == null);
      lock.unlock();
   }

   /**
    * Gets connection.
    *
    * @param jdbc the jdbc
    * @return the connection
    * @throws SQLException the sql exception
    */
   public static Connection getConnection(@NonNull String jdbc) throws SQLException {
      lock.lock();
      try {
         WeakReference<Connection> reference = registry.get(jdbc);
         if (reference == null || reference.get() == null) {
            reference = new WeakReference<>(ResourceMonitor.monitor(DriverManager.getConnection(jdbc)));
            registry.put(jdbc, reference);
         }
         return reference.get();
      } finally {
         lock.unlock();
      }
   }

   private SQLiteConnectionRegistry() {
      throw new IllegalAccessError();
   }

}//END OF SQLiteConnectionRegistry
