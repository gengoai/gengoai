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
import com.gengoai.apollo.ml.observation.VariableNameSpace;
import com.gengoai.string.Strings;
import lombok.NonNull;

/**
 *<p>
 *    Defines a methodology for converting a column (field, feature) and its colu
 *</p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface ColumnConverter {

   /**
    * Binary valued variable column converter.
    *
    * @param includeColumnName the include column name
    * @return the column converter
    */
   static ColumnConverter binaryValuedVariable(boolean includeColumnName) {
      return (c, v) -> {
         if(includeColumnName) {
            return Variable.binary(c, v);
         }
         return Variable.binary(v);
      };
   }

   /**
    * Real valued variable column converter.
    *
    * @param nameSpace the name space
    * @return the column converter
    */
   static ColumnConverter realValuedVariable(@NonNull VariableNameSpace nameSpace) {
      return (c, v) -> {
         if(nameSpace == VariableNameSpace.Prefix) {
            return Variable.real(c, Strings.EMPTY, Double.parseDouble(v));
         }
         return Variable.real(c, Double.parseDouble(v));
      };
   }

   /**
    * Convert variable.
    *
    * @param columnName  the column name
    * @param columnValue the column value
    * @return the variable
    */
   Variable convert(@NonNull String columnName, @NonNull String columnValue);

}//END OF ColumnConverter
