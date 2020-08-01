/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai;

import com.gengoai.reflection.TypeUtils;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Defines a parameter and its type for use with in a {@link ParamMap}
 *
 * @param <T> the type of the parameter
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public final class ParameterDef<T> implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * The Param name.
    */
   public final String name;
   /**
    * The Param Type.
    */
   public final Class<T> type;


   private ParameterDef(String name, @NonNull Class<T> type) {
      this.name = Validation.notNullOrBlank(name);
      this.type = type;
   }

   /**
    * Creates a boolean param
    *
    * @param name the name of the param
    * @return the param
    */
   public static ParameterDef<Boolean> boolParam(String name) {
      return new ParameterDef<>(name, Boolean.class);
   }

   /**
    * Creates a Double param param.
    *
    * @param name the name of the param
    * @return the param
    */
   public static ParameterDef<Double> doubleParam(String name) {
      return new ParameterDef<>(name, Double.class);
   }

   /**
    * Creates a Float param param.
    *
    * @param name the name of the param
    * @return the param
    */
   public static ParameterDef<Float> floatParam(String name) {
      return new ParameterDef<>(name, Float.class);
   }

   /**
    * Creates a Int param param.
    *
    * @param name the name of the param
    * @return the param
    */
   public static ParameterDef<Integer> intParam(String name) {
      return new ParameterDef<>(name, Integer.class);
   }

   /**
    * Creates a Long param param.
    *
    * @param name the name of the param
    * @return the param
    */
   public static ParameterDef<Long> longParam(String name) {
      return new ParameterDef<>(name, Long.class);
   }

   /**
    * Param parameter def.
    *
    * @param <T>  the type parameter
    * @param name the name
    * @param type the type
    * @return the parameter def
    */
   public static <T> ParameterDef<T> param(String name, @NonNull Type type) {
      return new ParameterDef<>(name, TypeUtils.asClass(type));
   }

   /**
    * Param parameter def.
    *
    * @param <T>  the type parameter
    * @param name the name
    * @param type the type
    * @return the parameter def
    */
   public static <T> ParameterDef<T> param(String name, @NonNull Class<T> type) {
      return new ParameterDef<>(name, type);
   }

   /**
    * Creates a String param param.
    *
    * @param name the name of the param
    * @return the param
    */
   public static ParameterDef<String> strParam(String name) {
      return new ParameterDef<>(name, String.class);
   }

   /**
    * Check type.
    *
    * @param type the type
    */
   void checkType(Class<?> type) {
      if (!this.type.isAssignableFrom(type)) {
         throw new IllegalArgumentException(
            "Invalid type: " + type.getSimpleName() + ", expecting " + this.type.getSimpleName()
         );
      }
   }

   /**
    * Check value.
    *
    * @param value the value
    */
   void checkValue(Object value) {
      if (Number.class.isAssignableFrom(type) && value instanceof Number) {
         return;
      }
      if (!type.isInstance(value)) {
         throw new IllegalArgumentException(
            "Invalid value: " + value + ", expecting " + type.getSimpleName()
         );
      }
   }

}//END OF ParameterDef
