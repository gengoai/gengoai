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

package com.gengoai.sql;

import com.gengoai.Validation;
import com.gengoai.sql.operator.SQLOperable;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates an SQL Function (e.g. count, max, substr, etc.)
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SQLFunction implements SQLOperable {
   private static final long serialVersionUID = 1L;
   String name;
   List<SQLElement> arguments;


   /**
    * Constructs a generic SQLFunction made up of a function name and zero or more arguments.
    *
    * @param name the name of the function
    * @param arg1 the arg 1
    * @param args the arguments of the function
    * @return the SQLFunction
    */
   public static SQLFunction function(String name, @NonNull SQLElement arg1, @NonNull SQLElement... args) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      SQLFunction function = new SQLFunction(name.toUpperCase(), new ArrayList<>());
      function.arguments.add(arg1);
      function.arguments.addAll(Arrays.asList(args));
      return function;
   }

   public static SQLFunction function(String name, @NonNull List<SQLElement> args) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      return new SQLFunction(name.toUpperCase(), new ArrayList<>(args));
   }

   public static SQLFunction function(String name) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      return new SQLFunction(name.toUpperCase(), Collections.emptyList());
   }


}//END OF SQLFunction
