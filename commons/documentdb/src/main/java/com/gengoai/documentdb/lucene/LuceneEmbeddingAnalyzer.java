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

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;

import java.io.IOException;
import java.io.StringReader;

public class LuceneEmbeddingAnalyzer extends Analyzer {
   public static final int DEFAULT_QUANTIZATION_LEVEL = 80;
   public static final String SKIP = "_";

   private final int quantizationLevel;
   private final CharArraySet set = new CharArraySet(1, false);

   public LuceneEmbeddingAnalyzer() {
      this(DEFAULT_QUANTIZATION_LEVEL);
   }

   public LuceneEmbeddingAnalyzer(int quantizationLevel) {
      this.quantizationLevel = quantizationLevel;
      this.set.add(SKIP);
   }



   public Counter<String> analyze(String s) {
      Counter<String> c = Counters.newCounter();
      try {
         TokenStream tokenStream = tokenStream(null, new StringReader(s));
         CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);


         tokenStream.reset();
         while (tokenStream.incrementToken()) {
            String token = cattr.toString();
            if (token.length() == 0) {
               continue;
            }
            c.increment(cattr.toString());
         }
         tokenStream.end();
         tokenStream.close();

      } catch (IOException e) {
         e.printStackTrace();
      }

      return c;
   }

   @Override
   protected TokenStreamComponents createComponents(String fieldName) {
      var tokenizer = new DelimiterTokenizer();
      TokenFilter filter = new LuceneEmbeddingFilter(tokenizer, quantizationLevel);
      filter = new StopFilter(filter, set);
      return new TokenStreamComponents(tokenizer, filter);
   }


   private static class DelimiterTokenizer extends CharTokenizer {
      @Override
      protected boolean isTokenChar(int c) {
         char current = Character.toChars(c)[0];
         return current != ',' && !Character.isWhitespace(c);
      }
   }
}
