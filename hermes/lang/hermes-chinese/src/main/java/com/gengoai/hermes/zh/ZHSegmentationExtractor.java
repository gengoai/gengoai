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

import com.gengoai.apollo.feature.ObservationExtractor;
import com.gengoai.apollo.data.observation.*;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.lexicon.WordList;
import com.gengoai.string.CharMatcher;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gengoai.apollo.data.observation.Variable.binary;

public class ZHSegmentationExtractor implements ObservationExtractor<HString> {
   @NonNull
   private final WordList dictionary;
   private final int windowSize;
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
   private static final Set<String> dates = Set.of("月", "日电", "日", "日晚", "年");
   private static final Set<String> zhCounters = Set.of("个");

   public ZHSegmentationExtractor(WordList dictionary, int windowSize) {
      this.dictionary = dictionary;
      this.windowSize = windowSize;
   }


   private boolean isDigit(String s) {
      if (Strings.isDigit(s)) {
         return true;
      }
      return s.chars().allMatch(z -> zhDigits.contains((char) z));
   }

   private String charType(String s) {
//      return Integer.toString(Character.getType(s.charAt(0)));
      if (isDigit(s)) {
         return "#";
      }
      if (dates.contains(s)) {
         return "D";
      }
      if (zhCounters.contains(s)) {
         return "C";
      }
      if( CharMatcher.Ideographic.matchesAllOf(s) ){
         return "I";
      }
      if (Strings.isAlphaNumeric(s)) {
         return "L";
      }
      if (Strings.isPunctuation(s)) {
         return "P";
      }
      if (Strings.isNullOrBlank(s)) {
         return "W";
      }
      return "X";
   }

   private double isPunct(String c) {
      return Strings.isPunctuation(c)
            ? 1.0
            : 0.0;
   }

   private List<Variable> generate(int[] i, String[] chars, String[] types) {
      return List.of(
            Variable.binary(IntStream.of(i).mapToObj(index -> {
                               if (index <= windowSize) {
                                  return "char[" + (index - windowSize) + "]";
                               }
                               return "char[" + (windowSize - index) + "]";
                            }).collect(Collectors.joining(",")),
                            IntStream.of(i).mapToObj(index -> chars[index]).collect(Collectors.joining(","))
            ),
            Variable.binary(IntStream.of(i).mapToObj(index -> {
                               if (index <= windowSize) {
                                  return "type[-" + (index - windowSize) + "]";
                               }
                               return "type[" + (windowSize - index) + "]";
                            }).collect(Collectors.joining(",")),
                            IntStream.of(i).mapToObj(index -> types[index]).collect(Collectors.joining(","))
            )
      );
   }

   @Override
   public Observation extractObservation(@NonNull HString input) {
      VariableCollectionSequence features = new VariableCollectionSequence();


      final int window = windowSize;
      final int current = window;
      final int rightStart = window + 1;
      final int total = window * 2 + 1;

      for (int index = 0; index < input.length(); index++) {
         VariableCollection iChar = new VariableList();


         String[] chars = new String[total];
         String[] types = new String[total];
         for (int i = 0, offset = index - window; offset <= index + window; offset++, i++) {
            if (offset < 0) {
               chars[i] = "BOS";
               types[i] = "BOS";
            } else if (offset >= input.length()) {
               chars[i] = "EOS";
               types[i] = "EOS";
            } else {
               chars[i] = Character.toString(input.charAt(offset));
               types[i] = charType(chars[i]);
            }
         }

         iChar.add(binary("char[0]", chars[current]));
         iChar.add(binary("type[0]", types[current]));

         for (int i = 0; i < total; i++) {
            if( i == current ){
               continue;
            }
            for( int k = i+1; k <= windowSize; k++){
               iChar.addAll(generate(IntStream.range(i,k).toArray(), chars, types));
            }
         }



//         iChar.add(Variable.binary(Character.toString(input.charAt(index))));
//
//         if (Strings.isPunctuation(Character.toString(input.charAt(index)))) {
//            iChar.add(Variable.binary("punct"));
//         }
//
//         if (CharMatcher.Digit.test(input.charAt(index))) {
//            iChar.add(Variable.binary("class", "number"));
//         } else if (CharMatcher.Letter.test(input.charAt(index))) {
//            iChar.add(Variable.binary("class", "letter"));
//         } else if (CharMatcher.Ideographic.test(input.charAt(index))) {
//            iChar.add(Variable.binary("class", "ideographic"));
//         }
//
//
//         if (index - 2 >= 0) {
//            iChar.add(Variable.binary(Character.toString(input.charAt(index - 2))));
//
//            iChar.add(Variable.binary(input.charAt(index - 2) + ","
//                                            + input.charAt(index - 1)));
//
//            iChar.add(Variable.binary(input.charAt(index - 2) + ","
//                                            + input.charAt(index)));
//
//            iChar.add(Variable.binary(input.charAt(index - 2) + "," +
//                                            input.charAt(index - 1) + ","
//                                            + input.charAt(index)));
//
//
//            if (index + 1 < input.length()) {
//               iChar.add(Variable.binary(input.charAt(index - 2) + ","
//                                               + input.charAt(index - 1) + ","
//                                               + input.charAt(index) + ","
//                                               + input.charAt(index + 1)));
//            }
//
//            if (index + 2 < input.length()) {
//               iChar.add(Variable.binary(input.charAt(index - 2) + ","
//                                               + input.charAt(index - 1) + ","
//                                               + input.charAt(index) + ","
//                                               + input.charAt(index + 1) + ","
//                                               + input.charAt(index + 2)));
//            }
//
//         }
//
//         if (index - 1 >= 0) {
//            iChar.add(Variable.binary(Character.toString(input.charAt(index - 1))));
//            iChar.add(Variable.binary(Character.toString(input.charAt(index - 1)) + "," + input.charAt(index)));
//            if (index + 1 < input.length()) {
//               iChar.add(Variable.binary(Character.toString(input.charAt(index - 1)) + "," + input.charAt(index + 1)));
//               iChar.add(Variable.binary(Character.toString(input.charAt(index - 1)) + "," + input
//                     .charAt(index) + "," + input.charAt(index + 1)));
//            }
//         }
//
//         if (index + 2 < input.length()) {
//            iChar.add(Variable.binary(Character.toString(input.charAt(index + 2))));
//         }
//
//         if (index + 1 < input.length()) {
//            iChar.add(Variable.binary(Character.toString(input.charAt(index + 1))));
//            iChar.add(Variable.binary(Character.toString(input.charAt(index)) + "," + input.charAt(index + 1)));
//         }


//         for (int k = 0, i = index - window; i <= index + window; i++, k++) {
//            if (i < 0 || i >= input.length()) {
//               chars[k] = "*";
//               types[k] = "B";
//            } else {
//               chars[k] = Character.toString(input.charAt(i));
//               types[k] = charType(chars[k]);
//            }
//         }
//
//
//         iChar.add(Variable.binary(chars[window]));
//         iChar.add(Variable.binary(types[window]));
//         for (int i = 0; i < window; i++) {
//
//            iChar.add(Variable.binary(String.join("", Arrays.asList(chars).subList(i, rightStart))));
//            iChar.add(Variable.binary(String.join("", Arrays.asList(types).subList(i, window)) + chars[window]));
//            iChar.add(Variable.binary(String.join("", Arrays.asList(types).subList(i, rightStart))));
//
//            iChar.add(Variable.binary(chars[window] + String
//                  .join("", Arrays.asList(chars).subList(rightStart, i + rightStart + 1))));
//            iChar.add(Variable.binary(chars[window] + String
//                  .join("", Arrays.asList(types).subList(rightStart, i + rightStart + 1))));
//            iChar.add(Variable.binary(types[window] + String
//                  .join("", Arrays.asList(types).subList(rightStart, i + rightStart + 1))));
//
//            iChar.add(Variable.binary(String.join("", Arrays.asList(chars).subList(i, i + rightStart + 1))));
//            iChar.add(Variable.binary(String.join("", Arrays.asList(types).subList(i, i + rightStart + 1))));
//         }


//         int matchStart = -1;
//         int matchLength = 0;
//         for (int i = Math.max(0, index - windowSize); i <= index; i++) {
//            for (int j = Math.min(input.length(), i + windowSize + 1); j > index && (j - i) > matchLength; j--) {
//               String subseq = input.subSequence(i, j).toString();
//               if (dictionary.contains(subseq)) {
//                  matchStart = i;
//                  matchLength = j - i;
//               }
//            }
//            if (matchLength >= windowSize) {
//               break;
//            }
//         }
//         if (matchLength > 0) {
//            if (matchStart + matchLength - 1 == index) {
//               iChar.add(Variable.binary("MATCH_END"));
//            } else {
//               iChar.add(Variable.binary("MATCH_IN"));
//            }
//
//         }
//
         features.add(iChar);
      }

      return features;
   }
}
