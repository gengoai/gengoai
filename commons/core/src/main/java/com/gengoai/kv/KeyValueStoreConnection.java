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

package com.gengoai.kv;

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import com.gengoai.specification.*;
import lombok.Data;

import java.io.Serializable;

/**
 * The type Kv store spec.
 *
 * @author David B. Bracewell
 */
@Data
public final class KeyValueStoreConnection implements Specifiable, Serializable {
   private static final long serialVersionUID = 1L;
   @QueryParameter
   private boolean compressed = true;
   @SubProtocol(0)
   private String namespace;
   @Path
   private String path;
   @QueryParameter
   private boolean readOnly = false;
   @Protocol
   private String type;
   @QueryParameter
   private boolean navigable = false;

   /**
    * Parse kv store spec.
    *
    * @param specification the specification
    * @return the kv store spec
    */
   public static KeyValueStoreConnection parse(String specification) {
      KeyValueStoreConnection spec = Specification.parse(specification, KeyValueStoreConnection.class);
      Validation.notNullOrBlank(spec.type, "Key-Value store type must not be blank or null");
      Validation.notNullOrBlank(spec.namespace, "Key-Value store namespace must not be blank or null");
      return spec;
   }

   /**
    * Connect t.
    *
    * @param <K> the type parameter
    * @param <V> the type parameter
    * @param <T> the type parameter
    * @return the t
    */
   public <K, V, T extends KeyValueStore<K, V>> T connect() {
      switch(type.toLowerCase()) {
         case "mem":
            if(navigable) {
               return Cast.as(new InMemoryNavigableKeyValueStore<K, V>(namespace, readOnly));
            }
            return Cast.as(new InMemoryKeyValueStore<>(namespace, readOnly));
         case "disk":
            Validation.notNullOrBlank(path, "Key-Value store path must not be blank or null");
            return Cast.as(new MapDBKeyValueStore<>(Resources.from(path),
                  namespace,
                  compressed,
                  readOnly));
         default:
            throw new IllegalArgumentException("Invalid key-value type: " + type);
      }
   }

   @Override
   public String getSchema() {
      return "kv";
   }


   @Override
   public String toString() {
      return toSpecification();
   }

}//END OF KeyValueStoreConnection
