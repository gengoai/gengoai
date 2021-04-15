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

import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import com.gengoai.documentdb.FieldType;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class LuceneDocumentDBQueryParser extends QueryParser {
   private final Map<String, FieldType> indices;

   public LuceneDocumentDBQueryParser(String primaryKey, Map<String, FieldType> indices) {
      super(primaryKey, LuceneDocumentUtils.createAnalyzer(indices));
      this.indices = indices;
   }

   @Override
   public Query newRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) {
      FieldType fieldType = indices.get(field);
      try {
         if (fieldType != null) {
            switch (fieldType) {
               case Long:
                  return LongPoint.newRangeQuery(field,
                                                 Long.parseLong(part1),
                                                 Long.parseLong(part2));
               case Integer:
                  return IntPoint.newRangeQuery(field,
                                                Integer.parseInt(part1),
                                                Integer.parseInt(part2));
               case Double:
                  return DoublePoint.newRangeQuery(field,
                                                   Double.parseDouble(part1),
                                                   Double.parseDouble(part2));
               case Float:
                  return FloatPoint.newRangeQuery(field,
                                                  Float.parseFloat(part1),
                                                  Float.parseFloat(part2));
               case Date:
                  return LongPoint.newRangeQuery(field,
                                                 Converter.convert(part1, LocalDate.class).toEpochDay(),
                                                 Converter.convert(part2, LocalDate.class).toEpochDay());
               case Timestamp:
                  return LongPoint.newRangeQuery(field,
                                                 Converter.convert(part1, LocalDateTime.class)
                                                          .toEpochSecond(ZoneOffset.UTC),
                                                 Converter.convert(part2, LocalDateTime.class)
                                                          .toEpochSecond(ZoneOffset.UTC));
            }
         }
      } catch (TypeConversionException e) {
         throw new RuntimeException(e);
      }
      return super.newRangeQuery(field, part1, part2, startInclusive, endInclusive);
   }


   @Override
   public Query newTermQuery(Term term) {
      FieldType fieldType = indices.get(term.field());
      if (fieldType != null) {
         switch (fieldType) {
            case Long:
               return LongPoint.newExactQuery(field,
                                              Long.parseLong(term.text()));
            case Integer:
               return IntPoint.newExactQuery(field,
                                             Integer.parseInt(term.text()));
            case Double:
               return DoublePoint.newExactQuery(field,
                                                Double.parseDouble(term.text()));
            case Float:
               return FloatPoint.newExactQuery(field,
                                               Float.parseFloat(term.text()));
            case Date:
               Date date = Converter.convertSilently(term.text(), Date.class);
               return LongPoint.newExactQuery(field, date.getTime());
         }
      }

      return super.newTermQuery(term);
   }
}
