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

import com.gengoai.collection.IteratorSet;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static com.gengoai.LogUtils.logFinest;
import static com.gengoai.reflection.RMethod.reflectOn;
import static com.gengoai.tuple.Tuples.$;

/**
 * <p> A Map based interface for accessing the properties of a bean object. </p> <p> Property information is cached
 * using the {@link BeanDescriptorCache}. </p>
 *
 * @author David B. Bracewell
 */
@Log
public class BeanMap extends AbstractMap<String, Object> {
   private final Object bean;
   private final BeanDescriptor beanDescriptor;

   /**
    * Constructs a bean map for a given bean
    *
    * @param bean The bean
    */
   public BeanMap(@NonNull Object bean) {
      this.bean = bean;
      this.beanDescriptor = BeanDescriptorCache.getInstance().get(bean.getClass());
   }

   @Override
   public boolean containsKey(Object arg0) {
      return beanDescriptor.hasReadMethod(arg0.toString());
   }

   @Override
   public boolean containsValue(Object arg0) {
      return values().contains(arg0);
   }

   @Override
   public Set<Entry<String, Object>> entrySet() {
      return new IteratorSet<>(() -> new Iterator<Entry<String, Object>>() {
         Iterator<String> getters = beanDescriptor.getReadMethodNames().iterator();

         @Override
         public boolean hasNext() {
            return getters.hasNext();
         }

         @Override
         public Entry<String, Object> next() {
            String name = getters.next();
            return $(name, get(name));
         }
      });
   }

   @Override
   public Object get(Object arg0) {
      Method m = beanDescriptor.getReadMethod(arg0.toString());
      if(m != null) {
         try {
            return m.invoke(bean);
         } catch(Exception e) {
            logFinest(log, e);
         }
      }
      return null;
   }

   /**
    * @return The bean in the bean map
    */
   public Object getBean() {
      return bean;
   }

   /**
    * @return The names of the setter methods
    */
   public Set<String> getSetters() {
      return beanDescriptor.getWriteMethodNames();
   }

   /**
    * Gets the type of the parameter on the setter method.
    *
    * @param key The setter method
    * @return A <code>Class</code> representing the parameter type of the setter method
    */
   public Type getType(String key) {
      if(beanDescriptor.hasReadMethod(key)) {
         return beanDescriptor.getReadMethod(key).getGenericReturnType();
      } else if(beanDescriptor.hasWriteMethod(key)) {
         Type[] paramTypes = beanDescriptor.getWriteMethod(key).getGenericParameterTypes();
         if(paramTypes.length > 0) {
            return paramTypes[0];
         }
      }
      return null;
   }

   @Override
   public boolean isEmpty() {
      return beanDescriptor.numberOfReadMethods() == 0;
   }

   @Override
   public Set<String> keySet() {
      return beanDescriptor.getReadMethodNames();
   }

   @Override
   public Object put(String arg0, Object arg1) {
      if(beanDescriptor.hasWriteMethod(arg0)) {
         try {
            return reflectOn(bean, beanDescriptor.getWriteMethod(arg0)).invoke(arg1);
         } catch(ReflectionException e) {
            throw new RuntimeException(e);
         }
      } else {
         logFinest(log, "{0} is not a setter on {1}.", arg0, bean.getClass());
      }
      return null;
   }

   @Override
   public int size() {
      return beanDescriptor.numberOfReadMethods();
   }

   @Override
   public Collection<Object> values() {
      return new IteratorSet<>(() -> new Iterator<Object>() {
         Iterator<String> getters = beanDescriptor.getReadMethodNames().iterator();

         @Override
         public boolean hasNext() {
            return getters.hasNext();
         }

         @Override
         public Object next() {
            return get(getters.next());
         }
      });
   }

}// END OF CLASS BeanMap
