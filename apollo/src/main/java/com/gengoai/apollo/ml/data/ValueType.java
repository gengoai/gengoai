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
import com.gengoai.math.Math2;
import com.gengoai.string.Strings;
import lombok.NonNull;

/**
 * Defines a value type and methodology for constructing a Variable from it and a given name.
 *
 * @author David B. Bracewell
 */
public enum ValueType {
   /**
    * Numeric variables where if the given value is null or cannot be parsed will result in <code>NaN</code>.
    */
   NUMERIC {
      @Override
      public Variable createVariable(@NonNull String name, Object value) {
         double v;
         if(value == null) {
            v = Double.NaN;
         } else if(value instanceof Number) {
            v = ((Number) value).doubleValue();
         } else {
            Double temp = Math2.tryParseDouble(value.toString());
            v = temp == null
                ? Double.NaN
                : temp;
         }
         return Variable.real(name, v);
      }
   },
   /**
    * Categorical Values where nulls will result in an empty string.
    */
   CATEGORICAL {
      @Override
      public Variable createVariable(@NonNull String name, Object value) {
         if(value == null) {
            return Variable.binary(name, Strings.EMPTY);
         }
         return Variable.binary(name, value.toString());
      }
   };

   /**
    * Creates a variable for the given value and associated name.
    *
    * @param name  the name
    * @param value the value
    * @return the variable
    */
   public abstract Variable createVariable(@NonNull String name, Object value);

}//END OF ColumnDataType
