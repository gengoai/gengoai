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

import com.gengoai.LogUtils;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.lucene.field.Fields;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.kohsuke.MetaInfServices;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Log
@EqualsAndHashCode(callSuper = true)
@MetaInfServices
public class LongFieldType extends FieldType {
   private static final long serialVersionUID = 1L;

   public LongFieldType() {
      super(Long.class);
   }

   @Override
   public Query newRangeQuery(@NonNull String field, @NonNull String part1, @NonNull String part2, boolean startInclusive, boolean endInclusive) {
      try {
         Long low = parse(part1, true);
         Long hi = parse(part2, false);
         if (!startInclusive) {
            low += 1;
         }
         if (!endInclusive) {
            hi += -1;
         }
         return LongPoint.newRangeQuery(field, low, hi);
      } catch (TypeConversionException e) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' and ''{1}'' into double values", part1, part2);
         return super.newRangeQuery(field, part1, part2, startInclusive, endInclusive);
      }
   }

   @Override
   public Query newTermQuery(@NonNull Term term) {
      Long value;
      try {
         value = Converter.convert(term.text(), Long.class);
      } catch (TypeConversionException e) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' into a long", term.text());
         return super.newTermQuery(term);
      }
      return LongPoint.newExactQuery(term.field(), value);
   }

   protected Long parse(String str, boolean isLower) throws TypeConversionException {
      if (str == null || str.equals("*")) {
         return isLower
               ? Long.MIN_VALUE
               : Long.MAX_VALUE;
      }
      return Converter.convert(str, Long.class);
   }

   @Override
   @SneakyThrows
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      Long number = Converter.convert(value, Long.class);
      LongPoint point = new LongPoint(outField, number);
      if (store) {
         return List.of(point, new StoredField(Fields.getStoredFieldName(outField), number));
      }
      return Collections.singleton(point);
   }
}//END OF LongeFieldType
