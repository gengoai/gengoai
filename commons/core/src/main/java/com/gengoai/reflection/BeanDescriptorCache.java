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

import com.gengoai.cache.Cache;

/**
 * <p>A cache of {@link BeanDescriptor} to speed up performance of using {@link BeanMap}s. The cache parameters can be
 * specified using the cache spec with property <code>com.gengoai.reflection.BeanDescriptorCache.spec</code> by default
 * it will have a maximum size of 100 and entries will expire after 10 minutes of write</p>
 *
 * @author David B. Bracewell
 */
public class BeanDescriptorCache {
   private static final BeanDescriptorCache INSTANCE = new BeanDescriptorCache();
   private final Cache<Class<?>, BeanDescriptor> cache;

   private BeanDescriptorCache() {
      cache = Cache.create(1000, BeanDescriptor::new);
   }

   /**
    * @return An instance of the {@link BeanDescriptorCache}.
    */
   public static BeanDescriptorCache getInstance() {
      return INSTANCE;
   }

   /**
    * Gets a {@link BeanDescriptor} for a <code>Class</code>
    *
    * @param clazz The class for which a {@link BeanDescriptor} is to be created.
    * @return A {@link BeanDescriptor} for the given class
    */
   public BeanDescriptor get(Class<?> clazz) {
      return cache.get(clazz);
   }

}// END OF CLASS BeanDescriptorCache
