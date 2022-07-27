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

package com.gengoai.apollo.data;

import com.gengoai.Validation;
import com.gengoai.apollo.data.observation.Variable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a mapping of names (e.g. columns, fields, etc) to {@link ValueType} for conversion.
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor
public class Schema extends HashMap<String, ValueType> {
   private static final long serialVersionUID = 1L;
   @Getter
   @Setter
   private @NonNull ValueType defaultType = ValueType.NUMERIC;


   /**
    * Initializing constructor
    *
    * @param schema map of column names and types defining the schema
    */
   public Schema(@NonNull Map<String, ValueType> schema) {
      super(schema);
   }

   /**
    * Static helper method for creating an empty schema.
    *
    * @return the schema
    */
   public static Schema schema() {
      return new Schema();
   }

   /**
    * Static helper method for creating an empty schema.
    *
    * @param schema map of column names and types defining the schema
    * @return the schema
    */
   public static Schema schema(@NonNull Map<String, ValueType> schema) {
      return new Schema(schema);
   }

   /**
    * Adds a categorical column
    *
    * @param name the name of the column
    * @return the schema
    */
   public Schema categorical(String name) {
      Validation.notNullOrBlank(name, "Name must not be null or blank");
      put(name, ValueType.CATEGORICAL);
      return this;
   }


   /**
    * Defines a column in the schema
    *
    * @param name      the name of the column
    * @param valueType the value type of the column
    * @return the schema
    */
   public Schema column(String name, @NonNull ValueType valueType) {
      Validation.notNullOrBlank(name, "Name must not be null or blank");
      put(name, valueType);
      return this;
   }

   /**
    * Converts  the given name and its value into a Variable.
    *
    * @param name  the name
    * @param value the value
    * @return the variable
    */
   public Variable convert(@NonNull String name, Object value) {
      return getOrDefault(name, defaultType).createVariable(name, value);
   }

   /**
    * Adds a numeric column
    *
    * @param name the name of the column
    * @return the schema
    */
   public Schema numeric(String name) {
      Validation.notNullOrBlank(name, "Name must not be null or blank");
      put(name, ValueType.NUMERIC);
      return this;
   }

}//END OF Schema
