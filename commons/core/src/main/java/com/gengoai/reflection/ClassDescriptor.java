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

import com.gengoai.collection.Iterators;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.Multimap;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static com.gengoai.collection.Lists.linkedListOf;
import static com.gengoai.collection.Sets.hashSetOf;

/**
 * Contains basic information about the methods, fields and constructors for a class.
 *
 * @author David B. Bracewell
 */
public final class ClassDescriptor implements Serializable {
   private static final long serialVersionUID = 1L;
   private final LinkedList<Reflect> ancestors = new LinkedList<>();
   private final Class<?> clazz;
   private final Set<Constructor<?>> constructors = new HashSet<>();
   private final Map<String, Field> fields = new HashMap<>();
   private final Multimap<String, Method> methods = new HashSetMultimap<>();
   private Method singletonMethod;

   /**
    * Instantiates a new Class descriptor.
    *
    * @param clazz the clazz
    */
   public ClassDescriptor(Class<?> clazz) {
      this.clazz = clazz;
      for(Method method : clazz.getMethods()) {
         if(method.getDeclaringClass().equals(clazz) || !methods.containsKey(method.getName())) {
            methods.put(method.getName(), method);
         }
         if(isSingletonMethod(method)) {
            singletonMethod = method;
         }
      }
      for(Method method : clazz.getDeclaredMethods()) {
         if(method.getDeclaringClass().equals(clazz) || !methods.containsKey(method.getName())) {
            methods.put(method.getName(), method);
         }
         if(isSingletonMethod(method)) {
            singletonMethod = method;
         }
      }

      Class<?> tClass = clazz;
      while(tClass != null && tClass != Object.class) {
         for(Field field : tClass.getFields()) {
            if(field.getDeclaringClass().equals(clazz) || !fields.containsKey(field.getName())) {
               fields.put(field.getName(), field);
            }
         }
         for(Field field : tClass.getDeclaredFields()) {
            if(field.getDeclaringClass().equals(clazz) || !fields.containsKey(field.getName())) {
               fields.put(field.getName(), field);
            }
         }
         tClass = tClass.getSuperclass();
      }

      Collections.addAll(constructors, clazz.getConstructors());
      Collections.addAll(constructors, clazz.getConstructors());

      final Queue<Class<?>> queue = linkedListOf(clazz);
      final Set<Class<?>> seen = hashSetOf();
      while(queue.size() > 0) {
         Class<?> nextClazz = queue.remove();
         if(nextClazz != clazz) {
            ancestors.addLast(Reflect.onClass(nextClazz));
         }

         if(seen.contains(nextClazz)) {
            continue;
         }
         seen.add(nextClazz);

         if(nextClazz.getSuperclass() != null && !seen.contains(nextClazz.getSuperclass())) {
            queue.add(clazz.getSuperclass());
            seen.add(clazz.getSuperclass());
         }
         for(Class<?> iface : nextClazz.getInterfaces()) {
            if(!seen.contains(iface)) {
               queue.add(iface);
               seen.add(iface);
            }
         }
      }
   }

   private static boolean isSingletonMethod(Method method) {
      return Modifier.isStatic(method.getModifiers())
            && method.getParameterCount() == 0
            && (method.getName().equals("getSingleton")
            || method.getName().equals("getInstance")
            || method.getName().equals("createInstance"));
   }

   @Override
   public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof ClassDescriptor)) return false;
      ClassDescriptor that = (ClassDescriptor) o;
      return Objects.equals(clazz, that.clazz);
   }

   public Iterator<Reflect> getAncestors(boolean reversed) {
      if(reversed) {
         return Iterators.unmodifiableIterator(ancestors.descendingIterator());
      }
      return Iterators.unmodifiableIterator(ancestors.iterator());
   }

   /**
    * Gets clazz.
    *
    * @return the clazz
    */
   public Class<?> getClazz() {
      return clazz;
   }

   /**
    * Gets constructors.
    *
    * @param privileged the privileged
    * @return the constructors
    */
   public Stream<Constructor<?>> getConstructors(boolean privileged) {
      Stream<Constructor<?>> stream = constructors.stream();
      if(!privileged) {
         stream = stream.filter(m -> Modifier.isPublic(m.getModifiers()));
      }
      return stream;
   }

   /**
    * Gets field.
    *
    * @param name       the name
    * @param privileged the privileged
    * @return the field
    */
   public Field getField(String name, boolean privileged) {
      Field f = fields.get(name);
      if(f != null) {
         return (privileged || Modifier.isPublic(f.getModifiers()))
                ? f
                : null;
      }
      return null;
   }

   /**
    * Gets fields.
    *
    * @param privileged the privileged
    * @return the fields
    */
   public Stream<Field> getFields(boolean privileged) {
      Stream<Field> stream = fields.values().stream();
      if(!privileged) {
         stream = stream.filter(m -> Modifier.isPublic(m.getModifiers()));
      }
      return stream;
   }

   /**
    * Gets methods.
    *
    * @param privileged the privileged
    * @return the methods
    */
   public Stream<Method> getMethods(boolean privileged) {
      Stream<Method> stream = methods.values().stream();
      if(!privileged) {
         stream = stream.filter(m -> Modifier.isPublic(m.getModifiers()));
      }
      return stream;
   }

   /**
    * Gets methods.
    *
    * @param privileged the privileged
    * @return the methods
    */
   public Stream<Method> getMethods(String[] names, boolean privileged) {
      Stream<Method> stream = null;
      for(String name : names) {
         if(stream == null) {
            stream = getMethods(name, privileged);
         } else {
            stream = Stream.concat(stream, getMethods(name, privileged));
         }
      }
      return stream;
   }

   /**
    * Gets methods.
    *
    * @param name       the name
    * @param privileged the privileged
    * @return the methods
    */
   public Stream<Method> getMethods(String name, boolean privileged) {
      Stream<Method> stream = methods.get(name).stream();
      if(!privileged) {
         stream = stream.filter(m -> Modifier.isPublic(m.getModifiers()));
      }
      return stream;
   }

   public Method getSingletonMethod() {
      return singletonMethod;
   }

   @Override
   public int hashCode() {
      return Objects.hash(clazz);
   }
}//END OF ClassDescriptor
