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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.reflection.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author David B. Bracewell
 */
public final class Mixins {

   private Mixins() {
      throw new IllegalAccessError();
   }

   @JsonDeserialize(as = TypeUtils.ParameterizedTypeImpl.class)
   public static abstract class ParameterizedTypeMixin {

   }

   @JsonDeserialize(using = TypeDeserializer.class)
   public static class TypeMixin {

   }

   private static class TypeDeserializer extends JsonDeserializer<Type> {

      @Override
      public Type deserialize(JsonParser p, DeserializationContext ctxt) throws IOException,
                                                                                JsonProcessingException {
         try {
            JsonEntry n = new JsonEntry(p.getCodec().readTree(p));
            if(n.isObject()) {
               return TypeUtils.parameterizedType(n.getProperty("rawType").as(Type.class),
                                                  n.getProperty("parameters")
                                                   .asArray(Type.class)
                                                   .toArray(new Type[1]));
            }
            return Class.forName(n.asString());
         } catch(Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

}//END OF Mixins
