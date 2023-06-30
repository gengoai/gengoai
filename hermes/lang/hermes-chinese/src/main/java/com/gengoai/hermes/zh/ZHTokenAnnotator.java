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

import com.gengoai.Language;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.en.ENTokenizer;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.hermes.morphology.Tokenizer;
import com.gengoai.stream.Streams;
import com.gengoai.string.Re;
import com.gengoai.string.Strings;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ZHTokenAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;
   private final JiebaSegmenter segmenter = new JiebaSegmenter();
   private static final Set<String> dates = Set.of("月", "日电", "日", "日晚", "年");
   private static final Set<String> zhCounters = Set.of("个");
   private static final Set<Character> zhDigits = Set.of('壹',
                                                         '一',
                                                         '1',
                                                         '贰',
                                                         '二',
                                                         '2',
                                                         '叁',
                                                         '三',
                                                         '3',
                                                         '肆',
                                                         '四',
                                                         '4',
                                                         '伍',
                                                         '五',
                                                         '5',
                                                         '陆',
                                                         '六',
                                                         '6',
                                                         '柒',
                                                         '七',
                                                         '7',
                                                         '捌',
                                                         '八',
                                                         '8',
                                                         '玖',
                                                         '久',
                                                         '9',
                                                         '零',
                                                         '〇',
                                                         '0',
                                                         '拾',
                                                         '十',
                                                         '佰',
                                                         '百',
                                                         '仟',
                                                         '千',
                                                         '万',
                                                         '亿',
                                                         '兆');

   private int expandDate(int index, List<SegToken> tokens) {
      boolean needDigit = true;
      int dateCount = 0;

      for (int i = index; i < tokens.size(); i++) {
         String word = tokens.get(i).word;

         if (needDigit) {

            needDigit = false;
            if (!isDigit(word)) {
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

   private boolean isDigit(String s) {
      if (Strings.isDigit(s)) {
         return true;
      }
      if (s.chars().allMatch(z -> zhDigits.contains((char) z))) {
         return true;
      }
      return isCount(s);
   }

   private boolean isCount(String s) {
      return s.length() > 1 && isDigit(s.substring(0, s.length() - 1)) && isCounter(s.substring(s.length() - 1, s
            .length()));
   }

   private boolean isCounter(String s) {
      return zhCounters.contains(s);
   }

   @Override
   protected void annotateImpl(Document document) {
      ENTokenizer enTokenizer = new ENTokenizer();
      List<Tokenizer.Token> altTokens = Streams.asStream(enTokenizer.tokenize(document.toString()))
                                               .filter(t -> t.type == TokenType.EMAIL ||
                                                     t.type == TokenType.EMOTICON ||
                                                     t.type == TokenType.URL)
                                               .collect(Collectors.toList());

      Pattern pattern = Pattern
            .compile(Re.oneOrMore(Re.chars(Re.NUMBER, "壹", "一", "1", "贰", "二", "2", "叁", "三", "3", "肆", "四", "4", "伍", "五", "5", "陆", "六", "6", "柒", "七", "7", "捌", "八", "8", "玖", "久", "9", "零",
                                           "〇",
                                           "0",
                                           "拾",
                                           "十",
                                           "佰",
                                           "百",
                                           "仟",
                                           "千",
                                           "万",
                                           "亿",
                                           "兆")));
      Matcher m = pattern.matcher(document.toString());
      while (m.find()) {
//         altTokens.add(new Tokenizer.Token(m.group(), TokenType.NUMBER, m.start(),m.end(), 0));
      }
      Collections.sort(altTokens, Comparator.comparing(t -> t.charStartIndex));

      int altTokenIndex = 0;
      List<SegToken> tokens = segmenter.process(document.toString(), JiebaSegmenter.SegMode.SEARCH);

      //Step 1: Collapse Digits
      List<SegToken> temp = new ArrayList<>();
      for (int i = 0; i < tokens.size(); i++) {
         SegToken token = tokens.get(i);
         int j = i;
         while (j < tokens.size()) {
            if (!isDigit(tokens.get(j).word)) {
               break;
            }
            j++;
         }

         if (j > i) {
            int start = tokens.get(i).startOffset;
            int end = tokens.get(j - 1).endOffset;
            String word = document.subSequence(start, end).toString();
            i = j - 1;
            temp.add(new SegToken(word, start, end));
         } else {
            temp.add(token);
         }
      }
      tokens = temp;

      for (int i = 0; i < tokens.size(); i++) {
         SegToken token = tokens.get(i);
         SegToken next = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;
         String word = token.word;
         int start = token.startOffset;
         int end = token.endOffset;
         TokenType tokenType = TokenType.CHINESE_JAPANESE;

//         System.out.println(word + ": " + isDigit(word));

         if (Strings.isNullOrBlank(word)) {
            continue;
         }

         if (Strings.isPunctuation(word)) {
            tokenType = TokenType.PUNCTUATION;
         } else if (isDigit(word)) {
            int length = expandDate(i, tokens);

            if (length <= 0) {
               tokenType = isCounter(word)
                     ? TokenType.QUANTITY
                     : TokenType.NUMBER;
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


   public static void main(String[] args) {
      Document doc = Document.create("被取消的还有国务卿蓬佩奥（Mike Pompeo）的欧洲之行。:)");
      doc.setLanguage(Language.CHINESE);
      doc.annotate(Types.TOKEN, Types.TRANSLITERATION);
      for (Annotation token : doc.tokens()) {
         System.out.println(token + " (" + token.attribute(Types.TRANSLITERATION) + ") " + token.pos());
      }
   }

}//END OF ZHTokenAnnotator
