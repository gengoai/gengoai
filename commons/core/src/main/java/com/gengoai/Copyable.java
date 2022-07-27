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

package com.gengoai;

import com.gengoai.io.resource.ByteArrayResource;

/**
 * <p> The Copyable interface defines a method for returning a copy of an object. Individual implementations are left
 * to determine if the copy is deep or shallow. However, a preference is for deep copies.This interface acts an
 * alternative to Java's clone method providing a generic return type. To aid possible implementations, a static
 * <code>deepCopy</code> method is provided that uses Java serialization to perform a deep copy of an object.</p>
 *
 * @param <E> The type of object that being copied
 * @author David B. Bracewell
 */
public interface Copyable<E> {

   /**
    * Deep copies an object using serialization.
    *
    * @param <T>    the serializable type parameter
    * @param object the object to copy
    * @return the copied object
    */
   static <T> T deepCopy(T object) {
      try {
         return new ByteArrayResource().writeObject(object).readObject();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * <p> Makes a copy of this object. </p>
    *
    * @return A copy of this object.
    */
   E copy();

}// END OF INTERFACE Copyable
