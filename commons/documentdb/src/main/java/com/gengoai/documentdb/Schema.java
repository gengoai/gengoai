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

package com.gengoai.documentdb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public final class Schema {
   @JsonProperty
   private final Map<String, Field> fieldMap = new LinkedHashMap<>();
   @JsonProperty
   @Getter
   private final String primaryKey;

   private Schema(Field primaryKey) {
      if (primaryKey == null) {
         this.primaryKey = null;
      } else {
         this.primaryKey = primaryKey.getName();
         fieldMap.put(primaryKey.getName(), primaryKey);
      }
   }

   public static Schema schema() {
      return new Schema(null);
   }

   public static Schema withPrimaryKey(@NonNull Field primaryKey) {
      return new Schema(primaryKey);
   }

   public Schema addField(@NonNull Field field) {
      this.fieldMap.put(field.getName(), field);
      return this;
   }

   public Set<String> fieldNames() {
      return Collections.unmodifiableSet(fieldMap.keySet());
   }

   public Collection<Field> fields() {
      return Collections.unmodifiableCollection(fieldMap.values());
   }

   @JsonIgnore
   public Field getField(String name) {
      return fieldMap.get(name);
   }

}//END OF Schema
