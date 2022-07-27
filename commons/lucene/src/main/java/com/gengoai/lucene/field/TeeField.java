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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gengoai.io.resource.ByteArrayResource;
import com.gengoai.lucene.field.types.NoOptFieldType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TeeField extends Field {
   @JsonProperty
   private final Set<Field> teeFields = new HashSet<>();

   public TeeField(String name,
                   boolean stored,
                   @NonNull Field... tees) {
      super(name, new NoOptFieldType(), stored);
      for (Field tee : tees) {
         teeFields.add(tee.makeChildOf(this));
      }
   }


   public TeeField(String name,
                   @NonNull Field... tees) {
      super(name, new NoOptFieldType());
      teeFields.addAll(Arrays.asList(tees));
   }

   @JsonIgnore
   public Map<String, Analyzer> getAnalyzers() {
      Map<String, Analyzer> analyzerMap = new HashMap<>();
      for (Field teeField : teeFields) {
         analyzerMap.putAll(teeField.getAnalyzers());
      }
      return analyzerMap;
   }

   @Override
   public Optional<Field> getChildField(@NonNull String name) {
      String childName = String.format("%s.%s", getName(), name);
      return teeFields.stream().filter(f -> f.getName().equals(childName)).findFirst();
   }

   @Override
   @SneakyThrows
   public Collection<IndexableField> process(Object value) {
      if (value == null) {
         return Collections.emptyList();
      }
      List<IndexableField> fields = new ArrayList<>();
      if (isStored()) {
         ByteArrayResource bar = new ByteArrayResource();
         bar.writeObject(value);
         fields.add(new StoredField(Fields.getStoredFieldName(getName()), bar.readBytes()));
      }
      teeFields.stream()
               .flatMap(tee -> tee.process(value).stream())
               .forEach(fields::add);
      return fields;
   }

   @Override
   public String toString() {
      return String.format("TeeField{name='%s', type='%s', stored=%s, fields=[%s])",
                           getName(),
                           getType().getClass().getSimpleName(),
                           isStored(),
                           teeFields.stream().map(Field::getName).collect(Collectors.joining(", ")));
   }
}//END OF TeeField
