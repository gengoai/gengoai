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

package com.gengoai.lucene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.lucene.field.Field;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class FieldSet implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<String, Field> fields = new HashMap<>();


   @JsonCreator
   public FieldSet(@NonNull Collection<Field> fields) {
      fields.forEach(f -> this.fields.put(f.getName(), f));
   }

   @JsonValue
   public Collection<Field> fields() {
      return Collections.unmodifiableCollection(fields.values());
   }

   public Optional<Field> get(@NonNull String name) {
      if (fields.containsKey(name)) {
         return Optional.of(fields.get(name));
      }

      String[] parts = name.split("\\.");
      if (parts.length > 1) {
         for (int i = 1; i < parts.length - 1; i++) {
            String parentField = Arrays.stream(parts, 0, i + 1).collect(Collectors.joining("."));
            String childField = Arrays.stream(parts, i + 1, parts.length).collect(Collectors.joining("."));
            if (fields.containsKey(parentField)) {
               return fields.get(parentField).getChildField(childField);
            }
         }
      }
      return Optional.empty();
   }
}//END OF FieldSet
