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

package com.gengoai.lucene.field;

import com.gengoai.collection.counter.Counter;
import com.gengoai.lucene.field.types.LuceneEmbeddingAnalyzer;
import lombok.NonNull;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.Map;

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
      super(name, encode(vector), TYPE);
   }

   public EmbeddingField(String name, String encodedVector) {
      super(name, encodedVector, TYPE);
   }


   public static String encode(@NonNull float... v) {
      StringBuilder ndStr = new StringBuilder();
      for (float value : v) {
         if (ndStr.length() > 0) {
            ndStr.append(' ');
         }
         ndStr.append(value);
      }
      return ndStr.toString();
   }

   public static Query query(@NonNull String fieldName, @NonNull float... vector) {
      Counter<String> tf = ((LuceneEmbeddingAnalyzer) Fields.EMBEDDING.getAnalyzer()).analyze(EmbeddingField.encode(vector));
      BooleanQuery.Builder q = new BooleanQuery.Builder();
      for (Map.Entry<String, Double> e : tf.entries()) {
         var tq = new BoostQuery(new TermQuery(
               new Term(fieldName, e.getKey())),
                                 e.getValue().floatValue()
         );
         var bc = new BooleanClause(tq, BooleanClause.Occur.SHOULD);
         q.add(bc);
      }
      return q.build();
   }
}
