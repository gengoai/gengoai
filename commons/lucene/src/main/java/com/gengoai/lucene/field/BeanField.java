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

package com.gengoai.lucene.field;

import com.gengoai.conversion.Cast;
import com.gengoai.lucene.field.types.BeanFieldType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.lucene.analysis.Analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BeanField extends Field {
   private static final long serialVersionUID = 1L;

   public BeanField(String name,
                    boolean stored,
                    @NonNull Class<?> beanClass,
                    boolean storeBeanProperties) {
      super(name, new BeanFieldType(beanClass, storeBeanProperties), stored);
   }

   public BeanField(String name,
                    @NonNull Class<?> beanClass,
                    boolean storeBeanProperties) {
      this(name, false, beanClass, storeBeanProperties);
   }

   @Override
   public Map<String, Analyzer> getAnalyzers() {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      BeanFieldType type = Cast.as(getType());
      type.getAnalyzers()
          .forEach((field, analyzer) -> {
             String cname = String.format("%s.%s", getName(), field);
             analyzerMap.put(cname, analyzer);
          });
      return analyzerMap;
   }

   @Override
   public Optional<Field> getChildField(@NonNull String name) {
      BeanFieldType type = Cast.as(getType());
      return Optional.ofNullable(type.getField(getName(), name));
   }

   @Override
   public String toString() {
      return String.format("%s(name='%s', stored=%s, type=%s)",
                           getClass().getSimpleName(),
                           getName(),
                           isStored(),
                           getType());
   }
}//END OF BeanField
