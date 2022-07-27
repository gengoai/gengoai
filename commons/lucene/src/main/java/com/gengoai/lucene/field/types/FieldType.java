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

package com.gengoai.lucene.field.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.Copyable;
import com.gengoai.conversion.Cast;
import lombok.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode
public abstract class FieldType implements Serializable, Copyable<FieldType> {
   @Getter
   @Setter(AccessLevel.PROTECTED)
   @JsonIgnore
   private Class<?> valueClass;

   /**
    * Instantiates a new Field type.
    */
   protected FieldType() {
      this.valueClass = Object.class;
   }

   /**
    * Instantiates a new Field type.
    *
    * @param valueClass the value class
    */
   protected FieldType(@NonNull Class<?> valueClass) {
      this.valueClass = valueClass;
   }


   @JsonIgnore
   public Analyzer getAnalyzer() {
      return new KeywordAnalyzer();
   }

   /**
    * New range query query.
    *
    * @param field          the field
    * @param part1          the part 1
    * @param part2          the part 2
    * @param startInclusive the start inclusive
    * @param endInclusive   the end inclusive
    * @return the query
    */
   public Query newRangeQuery(@NonNull String field, @NonNull String part1, @NonNull String part2, boolean startInclusive, boolean endInclusive) {
      return new TermRangeQuery(field, new BytesRef(part1.getBytes()), new BytesRef(part2.getBytes()), startInclusive, endInclusive);
   }

   /**
    * New term query query.
    *
    * @param term the term
    * @return the query
    */
   public Query newTermQuery(@NonNull Term term) {
      return new TermQuery(term);
   }

   @Override
   public FieldType copy() {
      return Copyable.deepCopy(this);
   }

   /**
    * Process collection.
    *
    * @param value    the value
    * @param outField the out field
    * @param store    the store
    * @return the collection
    */
   public Collection<IndexableField> process(Object value, String outField, boolean store) {
      if (value == null) {
         return Collections.emptyList();
      }
      List<IndexableField> fields = new ArrayList<>();

      if (value.getClass().isArray()) {
         for (int i = 0; i < Array.getLength(value); i++) {
            fields.addAll(process(Array.get(value, i), outField, store));
         }
      } else if (value instanceof Iterable) {
         Iterable<?> iterable = Cast.as(value);
         for (Object o : iterable) {
            fields.addAll(process(o, outField, store));
         }
      } else if (value instanceof Iterator) {
         Iterator<?> iterator = Cast.as(value);
         while (iterator.hasNext()) {
            fields.addAll(process(iterator.next(), outField, store));
         }
      } else {
         fields.addAll(processImpl(value, outField, store));
      }

      return fields;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "{valueClass='" + valueClass.getName() + "'}";
   }

   /**
    * Process collection.
    *
    * @param value    the value
    * @param outField the out field
    * @param store    the store
    * @return the collection
    */
   protected abstract Collection<IndexableField> processImpl(Object value, String outField, boolean store);
}//END OF FieldType