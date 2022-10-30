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

import java.time.OffsetDateTime;
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
public class OffsetDateTimeFieldType extends FieldType {

   public OffsetDateTimeFieldType() {
      super(OffsetDateTime.class);
   }

   @Override
   public Query newRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) {
      OffsetDateTime lowDate;
      OffsetDateTime hiDate;
      try {
         lowDate = parse(part1, OffsetDateTime.MIN);
         hiDate = parse(part2, OffsetDateTime.MAX);
      } catch (TypeConversionException e) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' and ''{1}'' into LocalDate values", part1, part2);
         return null;
      }
      long low = lowDate.toEpochSecond();
      if (!startInclusive) {
         low += 1;
      }
      long hi = hiDate.toEpochSecond();
      if (!endInclusive) {
         hi -= 1;
      }
      return LongPoint.newRangeQuery(field, low, hi);
   }

   @Override
   public Query newTermQuery(@NonNull Term term) {
      OffsetDateTime value;
      try {
         value = Converter.convert(term.text(), OffsetDateTime.class);
      } catch (TypeConversionException e) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' into a LocalDate", term.text());
         return null;
      }
      return LongPoint.newExactQuery(term.field(), value.toEpochSecond());
   }

   protected OffsetDateTime parse(String part, OffsetDateTime defaultValue) throws TypeConversionException {
      if (part == null || part.equals("*")) {
         return defaultValue;
      } else {
         return Converter.convert(part, OffsetDateTime.class);
      }
   }

   @Override
   @SneakyThrows
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      long ts = Converter.convert(value, OffsetDateTime.class).toEpochSecond();
      LongPoint point = new LongPoint(outField, ts);
      if (store) {
         return List.of(point, new StoredField(Fields.getStoredFieldName(outField), ts));
      }
      return Collections.singleton(point);
   }
}//END OF OffsetDateTimeFieldType
