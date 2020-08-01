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

package com.gengoai.json;

import com.gengoai.cache.Cache;
import com.gengoai.cache.LRUCache;
import com.gengoai.conversion.Cast;
import com.gengoai.reflection.Reflect;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * @author David B. Bracewell
 */
public class TypeInfoAdapter<T> extends TypeAdapter<T> {
   public static final TypeAdapterFactory FACTORY = new TypeInfoAdapterFactory();

   private final TypeAdapterFactory adapterFactory;
   private final TypeToken<T> typeToken;
   private final Gson gson;

   private TypeInfoAdapter(TypeAdapterFactory adapterFactory, TypeToken<T> typeToken, Gson gson) {
      this.adapterFactory = adapterFactory;
      this.typeToken = typeToken;
      this.gson = gson;
   }

   @Override
   public T read(JsonReader jsonReader) throws IOException {
//      JsonObject obj = gson.fromJson(jsonReader, JsonObject.class);
//      try {
//         Class<?> clazz = Reflect.getClassForName(obj.get(Json.CLASS_NAME_PROPERTY).getAsString());
//         JsonElement derObject = obj;
//         if(obj.has(Json.VALUE_PROPERTY)) {
//            derObject = obj.get(Json.VALUE_PROPERTY);
//         }
//         if(clazz != typeToken.getRawType()) {
//            return Cast.as(gson.getAdapter(clazz).fromJsonTree(derObject));
//         }
//         return gson.getDelegateAdapter(adapterFactory, typeToken).fromJsonTree(derObject);
//      } catch(Exception e) {
//         throw new IOException(e);
//      }
      return null;
   }

   @Override
   public void write(JsonWriter jsonWriter, Object o) throws IOException {
//      if(o == null) {
      //         jsonWriter.nullValue();
      //         return;
      //      }
      //      JsonObject object = new JsonObject();
      //      object.addProperty(Json.CLASS_NAME_PROPERTY, o.getClass().getName());
      //      JsonElement e = gson.getAdapter(o.getClass()).toJsonTree(Cast.as(o));
      //      if(e.isJsonObject()) {
      //         for(Map.Entry<String, JsonElement> entry : e.getAsJsonObject().entrySet()) {
      //            object.add(entry.getKey(), entry.getValue());
      //         }
      //      } else {
      //         object.add(Json.VALUE_PROPERTY, e);
      //      }
      //      gson.toJson(object, jsonWriter);
   }

   private static class TypeInfoAdapterFactory implements TypeAdapterFactory {
      private final Cache<TypeToken<?>, TypeAdapter<?>> cache = new LRUCache<>(10_000);

      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
         if(Reflect.onClass(typeToken.getRawType()).getAnnotation(TypeInfo.class) != null) {
            return Cast.as(cache.get(typeToken, () -> new TypeInfoAdapter<>(this, typeToken, gson)));
         }
         return null;
      }
   }
}//END OF TypeInfoAdapter
