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

package com.gengoai.lucene.field.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.conversion.Converter;
import com.gengoai.lucene.field.Field;
import com.gengoai.lucene.field.Fields;
import com.gengoai.reflection.BeanDescriptor;
import com.gengoai.reflection.BeanDescriptorCache;
import com.gengoai.reflection.BeanMap;
import lombok.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexableField;

import java.util.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class BeanFieldType extends FieldType {
   @JsonProperty
   private final boolean storeBeanProperties;
   private final Map<String, FieldType> fieldTypeMap = new HashMap<>();

   @JsonCreator
   public BeanFieldType(@JsonProperty("beanClass") @NonNull Class<?> valueClass,
                        @JsonProperty("storeBeanProperties") boolean storeBeanProperties) {
      super(valueClass);
      this.storeBeanProperties = storeBeanProperties;
      BeanDescriptor beanDescriptor = BeanDescriptorCache.getInstance().get(getValueClass());
      for (String readMethodName : beanDescriptor.getReadMethodNames()) {
         fieldTypeMap.put(readMethodName, Fields.typeOf(beanDescriptor.getReadMethod(readMethodName).getReturnType()));
      }
   }

   public List<Field> createFields() {
      List<Field> fields = new ArrayList<>();
      fieldTypeMap.forEach((name,type) -> fields.add(new Field(name,type,storeBeanProperties)));
      return fields;
   }

   @JsonIgnore
   public Map<String, Analyzer> getAnalyzers() {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      fieldTypeMap.forEach((field, type) -> analyzerMap.put(field, type.getAnalyzer()));
      return analyzerMap;
   }

   public Field getField(@NonNull String parentName, @NonNull String name) {
      if (fieldTypeMap.containsKey(name)) {
         return new Field(String.format("%s.%s", parentName, name), fieldTypeMap.get(name));
      }
      return null;
   }

   @Override
   @JsonProperty("beanClass")
   public Class<?> getValueClass() {
      return super.getValueClass();
   }

   @Override
   public String toString() {
      return String.format("%s(valueClass='%s', storeBeanProperties=%s)",
                           getClass().getSimpleName(),
                           getValueClass(),
                           storeBeanProperties);
   }

   @Override
   @SneakyThrows
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      List<IndexableField> indexableFields = new ArrayList<>(Fields.BLOB.process(value, outField, store));
      value = Converter.convert(value, getValueClass());
      if (value == null) {
         return Collections.emptyList();
      }
      BeanMap beanMap = new BeanMap(value);
      for (String field : beanMap.keySet()) {
         indexableFields.addAll(Fields.toIndexableField(beanMap.get(field),
                                                        String.format("%s.%s", outField, field),
                                                        storeBeanProperties));
      }
      return indexableFields;
   }

   private void setBeanClass(Class<?> valueClass) {
      super.setValueClass(valueClass);
   }
}//END OF BeanFieldType
