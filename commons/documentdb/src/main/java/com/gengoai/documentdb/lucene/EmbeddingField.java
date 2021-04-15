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

package com.gengoai.documentdb.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class EmbeddingField extends Field {
   public static final FieldType TYPE = new FieldType();

   static {
      TYPE.setStored(false);
      TYPE.setOmitNorms(true);
      TYPE.setTokenized(true);
      TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
   }

   public EmbeddingField(String name, float... vector) {
      super(name, f2s(vector), TYPE);
   }

   static String f2s(float[] v) {
      StringBuilder ndStr = new StringBuilder();
      for (float value : v) {
         if (ndStr.length() > 0) {
            ndStr.append(' ');
         }
         ndStr.append(value);
      }
      return ndStr.toString();
   }
}
