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

package com.gengoai.collection.disk;

import com.gengoai.function.Unchecked;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.mapdb.Atomic;
import org.mapdb.DB;

import java.io.File;
import java.io.Serializable;

import static com.gengoai.LogUtils.logFine;

/**
 * Wraps and manages a MapDB DB store.
 */
@EqualsAndHashCode(exclude = "store")
@Log
public final class MapDBHandle implements Serializable, AutoCloseable {
   private static final long serialVersionUID = 1L;
   private final boolean compressed;
   private final File file;
   private volatile transient DB store;

   /**
    * Instantiates a new MapDBHandle.
    *
    * @param resource   the file containing the MapDB
    * @param compressed True if compression is used
    */
   public MapDBHandle(@NonNull Resource resource, boolean compressed) {
      this.file = resource.asFile()
                          .orElseGet(Unchecked.supplier(() -> {
                             Resource tempDir = Resources.temporaryDirectory();
                             tempDir.deleteOnExit();
                             resource.copy(tempDir.getChild(resource.baseName()));
                             Resources.from(resource.descriptor() + ".p")
                                      .copy(tempDir.getChild(resource.baseName() + ".p"));
                             Resources.from(resource.descriptor() + ".t")
                                      .copy(tempDir.getChild(resource.baseName() + ".t"));
                             logFine(log, "Copying resources to {0}", tempDir);
                             return tempDir.getChild(resource.baseName()).asFile().orElseThrow();
                          }));
      this.compressed = compressed;
   }

   @Override
   public void close() throws Exception {
      MapDBRegistry.close(file);
   }

   /**
    * Commits changes made to the database
    */
   public void commit() {
      getStore().commit();
      getStore().compact();
   }

   /**
    * Deletes the database files.
    */
   public void delete() {
      String path = file.getAbsolutePath();
      for (File f : new File[]{file, new File(path + ".p"), new File(path + ".t")}) {
         if (f.exists()) {
            f.delete();
         }
      }
   }

   /**
    * Gets the global boolean value with the given name
    *
    * @param name the name
    * @return the boolean
    */
   public Atomic.Boolean getBoolean(String name) {
      return getStore().getAtomicBoolean(name);
   }

   /**
    * Gets global integer value with the given name.
    *
    * @param name the name
    * @return the integer
    */
   public Atomic.Integer getInteger(String name) {
      return getStore().getAtomicInteger(name);
   }

   /**
    * Gets the global long value with the given name.
    *
    * @param name the name
    * @return the long
    */
   public Atomic.Long getLong(String name) {
      return getStore().getAtomicLong(name);
   }

   /**
    * Gets the database store object
    *
    * @return the store
    */
   protected DB getStore() {
      if (store == null || store.isClosed()) {
         synchronized (this) {
            if (store == null || store.isClosed()) {
               store = MapDBRegistry.get(file, compressed);
            }
         }
      }
      return store;
   }

   /**
    * Gets the gloabl string value for the given name.
    *
    * @param name the name
    * @return the string
    */
   public Atomic.String getString(String name) {
      return getStore().getAtomicString(name);
   }

   /**
    * Gets the global value of the given variable name.
    *
    * @param <E>  the type parameter
    * @param name the name
    * @return the var
    */
   public <E> Atomic.Var<E> getVar(String name) {
      return getStore().getAtomicVar(name);
   }

   /**
    * Is closed.
    *
    * @return True if closed, False if not
    */
   public boolean isClosed() {
      return store == null || store.isClosed();
   }
}//END OF MapDBHandle
