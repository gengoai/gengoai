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

package com.gengoai.hermes.zh;

import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.en.ENTokenizer;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.hermes.morphology.Tokenizer;
import com.gengoai.parsing.Parser;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZHTokenAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;
   private final JiebaSegmenter segmenter = new JiebaSegmenter();
   private static final Set<String> dates = Set.of("月", "日电", "日", "日晚", "年");


   private int expandDate(int index, List<SegToken> tokens) {
      boolean needDigit = true;
      int dateCount = 0;

      for (int i = index; i < tokens.size(); i++) {
         String word = tokens.get(i).word;

         if (needDigit) {

            needDigit = false;
            if (!Strings.isDigit(word)) {
               return dateCount - 1;
            }

            dateCount++;

         } else if (dates.contains(word)) {

            needDigit = true;
            dateCount++;

         } else {
            return dateCount - 1;
         }

      }

      return dateCount - 1;
   }

   @Override
   protected void annotateImpl(Document document) {
      ENTokenizer enTokenizer = new ENTokenizer();
      List<Tokenizer.Token> altTokens = Streams.asStream(enTokenizer.tokenize(document.toString()))
                                               .filter(t -> t.type == TokenType.EMAIL ||
                                                     t.type == TokenType.EMOTICON ||
                                                     t.type == TokenType.URL)
                                               .collect(Collectors.toList());
      int altTokenIndex = 0;
      List<SegToken> tokens = segmenter.process(document.toString(), JiebaSegmenter.SegMode.SEARCH);
      for (int i = 0; i < tokens.size(); i++) {
         SegToken token = tokens.get(i);
         SegToken next = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;
         String word = token.word;
         String nextWord = (next == null) ? Strings.EMPTY : next.word;
         int start = token.startOffset;
         int end = token.endOffset;
         TokenType tokenType = TokenType.CHINESE_JAPANESE;

         if (Strings.isNullOrBlank(word)) {
            continue;
         }

         if (Strings.isPunctuation(word)) {
            tokenType = TokenType.PUNCTUATION;
         } else if (Strings.isDigit(word)) {
            int length = expandDate(i, tokens);

            if (length <= 0) {
               tokenType = TokenType.NUMBER;
            } else {
               end = tokens.get(i + length).endOffset;
               i += length;
               tokenType = TokenType.TIME;
            }

         }
         if (altTokenIndex < altTokens.size()) {
            if (altTokens.get(altTokenIndex).charStartIndex == start) {
               Tokenizer.Token at = altTokens.get(altTokenIndex);
               altTokenIndex++;
               end = at.charEndIndex;
               tokenType = at.type;
               while (i + 1 < tokens.size() && tokens.get(i + 1).startOffset < end) {
                  i++;
               }
            } else {
               while (altTokenIndex < altTokens.size() && altTokens.get(altTokenIndex).charStartIndex < start) {
                  altTokenIndex++;
               }
            }
         }
         document.annotationBuilder(Types.TOKEN)
                 .start(start)
                 .end(end)
                 .attribute(Types.TOKEN_TYPE, tokenType)
                 .createAttached();
      }
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Set.of(Types.TOKEN, Types.LEMMA);
   }
}//END OF ZHTokenAnnotator
