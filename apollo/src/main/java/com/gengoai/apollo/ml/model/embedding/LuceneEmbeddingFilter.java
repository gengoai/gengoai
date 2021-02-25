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

package com.gengoai.apollo.ml.model.embedding;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;

import static com.gengoai.apollo.ml.model.embedding.LuceneEmbeddingAnalyzer.SKIP;


public class LuceneEmbeddingFilter extends TokenFilter {

   private static final String POSITION_PREFIX = "P";

   private final CharTermAttribute positionAttribute = addAttribute(CharTermAttribute.class);
   private final int quantizationLevel;
   private final LinkedList<String> fs = new LinkedList<>();
   private int tokenCount = 0;

   LuceneEmbeddingFilter(TokenStream tokenStream, int quantizationLevel){
      super(tokenStream);
      this.quantizationLevel = quantizationLevel;
   }


   @Override
   public boolean incrementToken() throws IOException {
      if( !fs.isEmpty() ){
         positionAttribute.setEmpty();
         positionAttribute.append(fs.removeFirst());
         return true;
      }
      if(input.incrementToken()){
         tokenCount++;
         String token = new String(positionAttribute.buffer(),0, positionAttribute.length());
         String fw = POSITION_PREFIX + tokenCount;
         double value = Double.parseDouble(token);
         int qv = (int) (Math.abs(value) * quantizationLevel);
         if( value < 0 ){
            fw = "N" + tokenCount;
         }
         for (int i = 0; i < qv; i++) {
            fs.add(fw);
         }
         positionAttribute.setEmpty();
         if( qv > 0 ){
            positionAttribute.append(fw);
         } else {
            positionAttribute.append(SKIP);
         }
         return true;
      }
      return false;
   }

   @Override
   public void reset() throws IOException {
      super.reset();
      this.fs.clear();
      this.tokenCount = 0;
   }
}
