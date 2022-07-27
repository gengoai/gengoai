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

import com.gengoai.conversion.Cast;
import com.gengoai.conversion.Converter;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.TypeUtils;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DBDocument extends HashMap<String, Object> {
   private static final long serialVersionUID = 1L;

   public DBDocument(@NonNull Map<String, ?> m) {
      super(m);
   }

   public static DBDocument from(Object obj) {
      if (obj == null) {
         return new DBDocument();
      }
      JsonEntry e = JsonEntry.from(obj);
      if (e.isObject()) {
         return new DBDocument(e.asMap());
      }
      return new DBDocument(Map.of("data", e));
   }

   public <T> T asType(@NonNull Type type){
      try {
         return Json.parse(Json.dumps(this), type);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public <T> T getAs(String name, @NonNull Type type) {
      Object o = get(name);
      if (o == null) {
         return null;
      }
      if (o instanceof JsonEntry) {
         return Cast.<JsonEntry>as(o).as(type);
      }
      return Converter.convertSilently(o, type);
   }

   public <T> List<T> getAsList(String name, @NonNull Type type) {
      Object o = get(name);
      if (o == null) {
         return null;
      }
      if (o instanceof JsonEntry) {
         JsonEntry je = Cast.as(o);
         if (je.isNull()) {
            return null;
         }
         if (je.isArray()) {
            return je.asArray(type);
         }
         o = je.get();
      }
      return Converter.convertSilently(o, TypeUtils.parameterizedType(List.class, type));
   }

   public <T> Set<T> getAsSet(String name, @NonNull Type type) {
      Object o = get(name);
      if (o == null) {
         return null;
      }
      if (o instanceof JsonEntry) {
         JsonEntry je = Cast.as(o);
         if (je.isNull()) {
            return null;
         }
         if (je.isArray()) {
            return new HashSet<>(je.asArray(type));
         }
         o = je.get();
      }
      return Converter.convertSilently(o, TypeUtils.parameterizedType(Set.class, type));
   }

   public <V> Map<String, V> getAsMap(String name, @NonNull Class<V> valueType) {
      Object o = get(name);
      if (o == null) {
         return null;
      }
      if (o instanceof JsonEntry) {
         JsonEntry je = Cast.as(o);
         if (je.isNull()) {
            return null;
         }
         if (je.isObject()) {
            return je.asMap(valueType);
         }
         o = je.get();
      }
      return Converter.convertSilently(o, TypeUtils.parameterizedType(Map.class, String.class, valueType));
   }

   public String getAsString(String name) {
      Object o = get(name);
      if (o == null) {
         return null;
      }
      if (o instanceof CharSequence) {
         return o.toString();
      }
      if (o instanceof JsonEntry) {
         JsonEntry je = Cast.as(o);
         if (je.isNull()) {
            return null;
         }
         if (je.isString()) {
            return je.asString();
         }
         o = je.get();
      }
      return Converter.convertSilently(o, String.class);
   }

}//END OF DBDocument
