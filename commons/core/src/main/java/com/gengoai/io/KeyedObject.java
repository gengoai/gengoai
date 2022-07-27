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
 *
 */

package com.gengoai.io;

import com.gengoai.conversion.Cast;

import java.util.function.Consumer;

import static com.gengoai.Validation.checkArgument;

/**
 * @author David B. Bracewell
 */
abstract class KeyedObject<T> implements AutoCloseable {
   public final Object key = new Object();
   public final T resource;

   protected KeyedObject(T resource) {
      this.resource = resource;
   }

   public static <T> KeyedObject<T> create(T object) {
      checkArgument(object instanceof AutoCloseable, "Object must be AutoCloseable");
      return Cast.as(new KeyedAutoCloseable<>(Cast.as(object)));
   }

   public static <T> KeyedObject<T> create(T object, Consumer<T> onClose) {
      return new KeyedOther<>(object, onClose);
   }

   private static class KeyedOther<T> extends KeyedObject<T> {
      private final Consumer<T> onClose;

      protected KeyedOther(T resource, Consumer<T> onClose) {
         super(resource);
         this.onClose = onClose;
      }

      @Override
      public void close() throws Exception {
         onClose.accept(resource);
      }
   }

   private static class KeyedAutoCloseable<T extends AutoCloseable> extends KeyedObject<T> {
      protected KeyedAutoCloseable(T resource) {
         super(resource);
      }

      @Override
      public void close() throws Exception {
         resource.close();
      }
   }
}//END OF KeyedObject
