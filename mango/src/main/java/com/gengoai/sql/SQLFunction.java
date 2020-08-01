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
import com.gengoai.string.StringResolver;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates an SQL Function (e.g. count, max, substr, etc.)
 */
@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SQLFunction implements SQLFormattable, SQLOperable {
   private static final long serialVersionUID = 1L;
   String name;
   @NonNull Map<String, SQLElement> args;

   /**
    * Constructs a binary function
    *
    * @param name the name of the function
    * @param arg1 the first argument
    * @param arg2 the second argument
    * @return the sql function
    */
   public static SQLFunction binaryFunction(String name, @NonNull SQLElement arg1, @NonNull SQLElement arg2) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      return new SQLFunction(name.toUpperCase(), Map.of("arg1", arg1, "arg2", arg2));
   }

   /**
    * Constructs a n-ary function taking zero or more arguments
    *
    * @param name the name of the function
    * @param args the arguments
    * @return the sql function
    */
   public static SQLFunction nAryFunction(String name, @NonNull SQLElement... args) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      Map<String, SQLElement> m = new HashMap<>();
      for(int i = 0; i < args.length; i++) {
         m.put("arg" + (i + 1), args[i]);
      }
      return new SQLFunction(name.toUpperCase(), m);
   }

   /**
    * Constructs a n-ary function taking at least one argument.
    *
    * @param name the name of the function
    * @param arg1 the first argument
    * @param args the other arguments
    * @return the sql function
    */
   public static SQLFunction nAryFunction(String name, @NonNull SQLElement arg1, @NonNull SQLElement... args) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      Map<String, SQLElement> m = new HashMap<>();
      m.put("arg1", arg1);
      for(int i = 0; i < args.length; i++) {
         m.put("arg" + (i + 2), args[i]);
      }
      return new SQLFunction(name.toUpperCase(), m);
   }

   /**
    * Constructs a ternary function
    *
    * @param name the name of the function
    * @param arg1 the first argument
    * @param arg2 the second argument
    * @param arg3 the third argument
    * @return the sql function
    */
   public static SQLFunction ternaryFunction(String name,
                                             @NonNull SQLElement arg1,
                                             @NonNull SQLElement arg2,
                                             @NonNull SQLElement arg3) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      return new SQLFunction(name.toUpperCase(), Map.of("arg1", arg1, "arg2", arg2, "arg3", arg3));
   }

   /**
    * Constructs an unary function
    *
    * @param name the name of the function
    * @param arg  the argument
    * @return the sql function
    */
   public static SQLFunction unaryFunction(String name, @NonNull SQLElement arg) {
      Validation.notNullOrBlank(name, "Must specify a function name");
      return new SQLFunction(name.toUpperCase(), Map.of("arg1", arg));
   }

   @Override
   public String toSQL(@NonNull SQLDialect dialect) {
      String template = dialect.getFunctionTemplate(name, args.size());
      Map<String, String> map = new HashMap<>();
      for(int i = 0; i < args.size(); i++) {
         String key = "arg" + (i + 1);
         map.put(key, dialect.toSQL(args.get(key)));
      }
      return StringResolver.resolve(template, map);
   }

}//END OF SqlFunction
