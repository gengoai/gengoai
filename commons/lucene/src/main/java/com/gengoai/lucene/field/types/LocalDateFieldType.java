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

import java.time.LocalDate;
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
public class LocalDateFieldType extends FieldType {

   public LocalDateFieldType() {
      super(LocalDate.class);
   }

   @Override
   public Query newRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) {
      LocalDate lowDate;
      LocalDate hiDate;
      try {
         lowDate = parse(part1, LocalDate.MIN);
         hiDate = parse(part2, LocalDate.MAX);
      } catch (TypeConversionException e) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' and ''{1}'' into LocalDate values", part1, part2);
         return super.newRangeQuery(field, part1, part2, startInclusive, endInclusive);
      }
      long low = lowDate.toEpochDay();
      if (!startInclusive) {
         low += 1;
      }
      long hi = hiDate.toEpochDay();
      if (!endInclusive) {
         hi -= 1;
      }
      return LongPoint.newRangeQuery(field, low, hi);
   }

   @Override
   public Query newTermQuery(@NonNull Term term) {
      LocalDate value;
      try {
         value = Converter.convert(term.text(), LocalDate.class);
      } catch (TypeConversionException e) {
         LogUtils.logSevere(log, "Unable to convert ''{0}'' into a LocalDate", term.text());
         return super.newTermQuery(term);
      }
      return LongPoint.newExactQuery(term.field(), value.toEpochDay());
   }

   protected LocalDate parse(String part, LocalDate defaultValue) throws TypeConversionException {
      if (part == null || part.equals("*")) {
         return defaultValue;
      } else {
         return Converter.convert(part, LocalDate.class);
      }
   }

   @Override
   @SneakyThrows
   protected Collection<IndexableField> processImpl(Object value, String outField, boolean store) {
      LocalDate localDate = Converter.convert(value, LocalDate.class);
      long ts = localDate.toEpochDay();
      LongPoint point = new LongPoint(outField, ts);
      if (store) {
         return List.of(point, new StoredField(Fields.getStoredFieldName(outField), localDate.toString()));
      }
      return Collections.singleton(point);
   }
}//END OF LocalDateFieldType
