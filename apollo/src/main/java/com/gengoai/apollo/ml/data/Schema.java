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

package com.gengoai.apollo.ml.data;

import com.gengoai.apollo.ml.observation.Variable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;

/**
 * Defines a mapping of names (e.g. columns, fields, etc) to {@link ValueType} for conversion.
 *
 * @author David B. Bracewell
 */
public class Schema extends HashMap<String, ValueType> {
   private static final long serialVersionUID = 1L;
   @Getter
   @Setter
   private @NonNull ValueType defaultType = ValueType.NUMERIC;

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

}//END OF Schema
