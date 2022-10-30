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

package com.gengoai.lucene;

import com.gengoai.lucene.field.Fields;
import lombok.NonNull;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
class SchemaAwareQueryParser extends QueryParser {
   private final IndexConfig config;

   public SchemaAwareQueryParser(@NonNull String defaultField, @NonNull IndexConfig config) {
      super(defaultField, config.getAnalyzer());
      this.config = config;
   }

   @Override
   public Query newRangeQuery(String field,
                              String part1,
                              String part2,
                              boolean startInclusive,
                              boolean endInclusive) {
      return Fields
            .findField(field, config.getFields())
            .map(f -> f.getType().newRangeQuery(field, part1, part2, startInclusive, endInclusive))
            .orElseGet(() -> super.newRangeQuery(field, part1, part2, startInclusive, endInclusive));
   }

   @Override
   public Query newTermQuery(Term term) {
      return Fields
            .findField(field, config.getFields())
            .map(f -> f.getType().newTermQuery(term))
            .orElseGet(() -> super.newTermQuery(term));
   }
}//END OF SchemaAwareQueryParser
