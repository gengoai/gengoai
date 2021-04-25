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

package com.gengoai.lucene.processor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.collection.Iterables;
import lombok.Getter;
import lombok.NonNull;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

import java.io.Serializable;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class FieldProcessor implements Serializable {
   @Getter
   private final String fieldName;

   protected FieldProcessor(String fieldName) {
      this.fieldName = fieldName;
   }

   @JsonIgnore
   public abstract Analyzer getAnalyzer();

   public <T> void multiValued(@NonNull Document document, @NonNull T[] values) {
      for (T value : values) {
         singleValue(document, value);
      }
   }

   public void multiValued(@NonNull Document document, @NonNull Object values) {
      for (Object value : Iterables.asIterable(values)) {
         singleValue(document, value);
      }
   }

   public void multiValued(@NonNull Document document, @NonNull Iterable<?> values) {
      for (Object value : values) {
         singleValue(document, value);
      }
   }

   public abstract void singleValue(@NonNull Document document, @NonNull Object value);

}//END OF FieldProcessor