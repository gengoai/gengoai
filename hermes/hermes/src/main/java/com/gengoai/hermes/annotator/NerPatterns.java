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

package com.gengoai.hermes.annotator;

import com.gengoai.LogUtils;
import com.gengoai.Validation;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.EntityType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.io.resource.Resource;
import com.gengoai.parsing.ParseException;
import com.gengoai.string.StringResolver;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple3;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
@Log
public final class NerPatterns implements Serializable {
   private static final long serialVersionUID = 1L;
   @NonNull
   private final List<Tuple3<EntityType, TokenRegex, Double>> entityPatterns;


   protected NerPatterns(List<Tuple3<EntityType, TokenRegex, Double>> entityPatterns) {
      this.entityPatterns = entityPatterns;
   }

   public static NerPatterns load(@NonNull Resource patternFile) throws IOException, ParseException {
      Map<String, String> wordLists = new HashMap<>();
      int STATE = -1; //-1 START, 0 WORD_LIST, 1 PATTERNS
      boolean isLemma = false;
      boolean caseSensitive = false;
      String heading = null;
      double defaultConfidence = 0.6;
      double confidence = 0d;
      List<String> buffer = new ArrayList<>();
      Pattern lexiconHeader = Pattern.compile("\\[([\\w_]+)(\\|(\\w+))?]");
      Pattern patternHeader = Pattern.compile("\\[(.*?)(?:\\|(\\d+\\.\\d+))?]");
      List<Tuple3<EntityType, TokenRegex, Double>> entityPatterns = new ArrayList<>();
      for (String s : patternFile.readLines()) {
         String line = s.strip();
         if (Strings.isNullOrBlank(line)) {
            continue;
         }

         if (line.replaceAll("\\s+", "").startsWith("==DEFAULT_CONFIDENCE=")) {
            defaultConfidence = Double
                  .parseDouble(line.replaceAll("\\s+", "").replaceFirst("==DEFAULT_CONFIDENCE=", ""));
         } else if (line.replaceAll("\\s+", "").equals("=LEXICONS")) {
            Validation.checkArgument(STATE == -1, "Invalid LEXICON section");
            STATE = 0;
         } else if (line.replaceAll("\\s+", "").equals("=PATTERNS")) {
            STATE = 1;
            if (buffer.size() > 0) {
               wordLists.put(heading, "(" + String.join("|", buffer) + ")");
               buffer.clear();
            }
         } else if (STATE == 0) {
            Matcher matcher = lexiconHeader.matcher(line);
            if (matcher.find()) {
               if (buffer.size() > 0) {
                  wordLists.put(heading, "(" + String.join("|", buffer) + ")");
                  buffer.clear();
               }
               heading = matcher.group(1);
               if (matcher.group(2) != null) {
                  if (matcher.group(2).equals("lemma")) {
                     isLemma = true;
                  } else if (matcher.group(2).equals("caseSensitive")) {
                     caseSensitive = true;
                  }
               }
            } else if (Strings.isNotNullOrBlank(heading)) {
               final String pattern;
               if (isLemma) {
                  pattern = "<%s>";
               } else if (caseSensitive) {
                  pattern = "\"%s\"";
               } else {
                  pattern = "'%s'";
               }
               buffer.add(Arrays.stream(line.split("\\s+"))
                                .map(word -> String.format(pattern, word))
                                .collect(Collectors.joining(" ", "(", ")")));
            }
         } else if (STATE == 1) {
            Matcher matcher = patternHeader.matcher(line);
            if (matcher.find()) {
               heading = matcher.group(1);
               if (matcher.group(2) != null) {
                  confidence = Double.parseDouble(matcher.group(2));
               } else {
                  confidence = defaultConfidence;
               }
            } else if (Strings.isNotNullOrBlank(heading)) {
               entityPatterns
                     .add($(EntityType.valueOf(heading), TokenRegex
                           .compile(StringResolver.resolve(line, wordLists)), confidence));
            }
         }

      }
      return new NerPatterns(entityPatterns);
   }


   public void process(@NonNull Document document) {
      for (Annotation sentence : document.sentences()) {
         for (Tuple3<EntityType, TokenRegex, Double> e : entityPatterns) {
            TokenMatcher match = e.v2.matcher(sentence);
            while (match.find()) {
               LogUtils.logFinest(log, "Found entity: span={0}, type={1}, confidence={2}, pattern={3}",
                                  match.group(),
                                  e.getV1(),
                                  e.getV3(),
                                  e.getV2().pattern());
               document.annotationBuilder(Types.RULE_BASED_ENTITY)
                       .bounds(match.group())
                       .attribute(Types.ENTITY_TYPE, e.getV1())
                       .attribute(Types.CONFIDENCE, e.v3)
                       .attribute(Types.attribute("PATTERN"), e.v2.pattern())
                       .createAttached();
            }
         }
      }
   }

}//END OF NerPatterns
