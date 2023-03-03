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

package com.gengoai.hermes.extraction.keyword;

import com.gengoai.hermes.extraction.lyre.LyreExpression;
import lombok.NonNull;

public enum KeywordExtractors {
   Rake {
      @Override
      public KeywordExtractor create() {
         return new RakeKeywordExtractor();
      }

      @Override
      public KeywordExtractor create(@NonNull LyreExpression lyreExpression) {
         return new RakeKeywordExtractor(lyreExpression);
      }
   },
   TermFrequency {
      @Override
      public KeywordExtractor create() {
         return new TermKeywordExtractor();
      }

      @Override
      public KeywordExtractor create(@NonNull LyreExpression lyreExpression) {
         return new TermKeywordExtractor(lyreExpression);
      }
   },
   TfIdf {
      @Override
      public KeywordExtractor create() {
         return new TFIDFKeywordExtractor();
      }

      @Override
      public KeywordExtractor create(@NonNull LyreExpression lyreExpression) {
         return new TFIDFKeywordExtractor(lyreExpression);
      }
   },
   NPClustering {
      @Override
      public KeywordExtractor create() {
         return new NPClusteringKeywordExtractor();
      }

      @Override
      public KeywordExtractor create(@NonNull LyreExpression lyreExpression) {
         return new NPClusteringKeywordExtractor();
      }
   },
   TextRank {
      @Override
      public KeywordExtractor create() {
         return new TextRank();
      }

      @Override
      public KeywordExtractor create(@NonNull LyreExpression lyreExpression) {
         return new TextRank();
      }
   };


   public abstract KeywordExtractor create();

   public abstract KeywordExtractor create(@NonNull LyreExpression lyreExpression);

   public KeywordExtractor create(@NonNull String lyreExpression) {
      return create(LyreExpression.parse(lyreExpression));
   }

}
