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

package com.gengoai.reflection;

import com.gengoai.string.Strings;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.gengoai.LogUtils.logFinest;

/**
 * Contains basic information about the read and write methods for a bean class.
 *
 * @author David B. Bracewell
 */
@Log
public final class BeanDescriptor implements Serializable {
   private static final long serialVersionUID = -6445604079340822462L;
   private final Class<?> clazz;
   private final Map<String, Method> readMethods = new ConcurrentHashMap<>();
   private final Map<String, Method> writeMethods = new ConcurrentHashMap<>();

   /**
    * Default Constructor that initializes the descriptor using class information
    *
    * @param clazz The class associated with this descriptor
    */
   BeanDescriptor(@NonNull Class<?> clazz) {
      this.clazz = clazz;
      Reflect.onClass(clazz)
             .getAncestors(false)
             .forEach(r -> setReadWrite(r.getType()));
      setReadWrite(this.clazz);
   }

   /**
    * Constructs an instance of the wrapped class.
    *
    * @return An instance of the wrapped class
    * @throws InstantiationException Something went wrong during instantiation.
    * @throws IllegalAccessException Couldn't access the class.
    */
   public Object createInstance() throws
         InstantiationException,
         IllegalAccessException,
         NoSuchMethodException,
         InvocationTargetException {
      return clazz.getDeclaredConstructor().newInstance();
   }

   /**
    * Constructs an instance of the wrapped class ignoring any errors.
    *
    * @return An instance of the class or <code>null</code> if something went wrong.
    */
   public Object createInstanceQuietly() {
      try {
         return clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
         logFinest(log, e);
         return null;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
         return false;
      }
      final BeanDescriptor other = (BeanDescriptor) obj;
      return Objects.equals(this.clazz, other.clazz);
   }

   /**
    * @return The associated class information.
    */
   public Class<?> getBeanClass() {
      return clazz;
   }

   /**
    * Gets a read method by its name
    *
    * @param methodName The name of the method
    * @return The method with the given name or <code>null</code>
    */
   public Method getReadMethod(String methodName) {
      return readMethods.get(methodName);
   }

   /**
    * @return All of the names of the read methods
    */
   public Set<String> getReadMethodNames() {
      return Collections.unmodifiableSet(readMethods.keySet());
   }

   /**
    * @return All of the read methods
    */
   public Collection<Method> getReadMethods() {
      return Collections.unmodifiableCollection(readMethods.values());
   }

   /**
    * Gets a write method by its name
    *
    * @param methodName The name of the method
    * @return The method with the given name or <code>null</code>
    */
   public Method getWriteMethod(String methodName) {
      return writeMethods.get(methodName);
   }

   /**
    * @return All of the names of the write methods
    */
   public Set<String> getWriteMethodNames() {
      return Collections.unmodifiableSet(writeMethods.keySet());
   }

   /**
    * @return All of the write methods
    */
   public Collection<Method> getWriteMethods() {
      return Collections.unmodifiableCollection(writeMethods.values());
   }

   /**
    * Determines if the descriptor has a read method named with the given string.
    *
    * @param methodName The read method we want to check for
    * @return True if the method exists, false otherwise
    */
   public boolean hasReadMethod(String methodName) {
      return readMethods.containsKey(methodName);
   }

   /**
    * Determines if the descriptor has a write method named with the given string.
    *
    * @param methodName The write method we want to check for
    * @return True if the method exists, false otherwise
    */
   public boolean hasWriteMethod(String methodName) {
      return writeMethods.containsKey(methodName);
   }

   @Override
   public int hashCode() {
      return Objects.hash(clazz);
   }

   /**
    * @return The number of read methods
    */
   public int numberOfReadMethods() {
      return readMethods.size();
   }

   /**
    * @return The number of write methods
    */
   public int numberOfWriteMethods() {
      return writeMethods.size();
   }

   private void setReadWrite(Class<?> clazz) {
      if (clazz == null) {
         return;
      }
      Reflect.onClass(clazz).getMethods().forEach(method -> {
         String name = method.getName();
         if (!name.equals("getClass") && !method.isAnnotationPresent(Ignore.class)) {
            if (name.startsWith("get") || name.startsWith("is")) {
               readMethods.put(transformName(name), method.getElement());
            } else if (name.startsWith("set") || (name.startsWith("add") && method.getParameterCount() == 1)) {
               writeMethods.put(transformName(name), method.getElement());
            }
         }
      });
   }

   private String transformName(String name) {
      int prefixLen = 3;
      if (name.startsWith("is")) {
         prefixLen = 2;
      }
      if (name.length() == prefixLen) {
         return Strings.EMPTY;
      }
      char[] carrry = name.substring(prefixLen).toCharArray();
      carrry[0] = Character.toLowerCase(carrry[0]);
      return new String(carrry);
   }

}// END OF CLASS BeanDescriptor
