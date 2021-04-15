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

package com.gengoai.reflection;

import com.gengoai.conversion.Cast;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * Wraps a Constructor allowing easy access to parameters and the ability to create new instances of an object.
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = true)
public final class RConstructor extends RExecutable<Constructor<?>, RConstructor> {
   private static final long serialVersionUID = 1L;
   private final Constructor<?> constructor;

   RConstructor(Reflect owner, Constructor<?> constructor) {
      super(owner);
      this.constructor = constructor;
   }

   /**
    * Create a new instance of the object calling the constructor that with the given arguments
    *
    * @param <T>  the type parameter for the object being created
    * @param args the constructor arguments
    * @return the created object
    * @throws ReflectionException Something went wrong creating the object
    */
   public <T> T create(Object... args) throws ReflectionException {
      return Cast.as(process(constructor -> {
         if (args == null || args.length == 0) {
            return Cast.as(constructor.newInstance());
         }
         return Cast.as(constructor.newInstance(convertParameters(args)));
      }));
   }

   /**
    * Create a new instance of the object calling the constructor that with the given arguments and wrapping the
    * constructed object as a Reflect
    *
    * @param args the constructor arguments
    * @return the created object wrapped as a Reflect
    * @throws ReflectionException Something went wrong creating the object
    */
   public Reflect createReflective(Object... args) throws ReflectionException {
      return Reflect.onObject(create(args)).setIsPrivileged(isPrivileged());
   }

   @Override
   public Constructor<?> getElement() {
      return constructor;
   }

   @Override
   public Type getType() {
      return constructor.getAnnotatedReturnType().getType();
   }

   @Override
   public String toString() {
      return constructor.toString();
   }

}//END OF ReflectedConstructor
