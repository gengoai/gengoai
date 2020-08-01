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

package com.gengoai.apollo.ml.feature;

import com.gengoai.collection.Iterables;
import com.gengoai.collection.Lists;
import com.gengoai.conversion.Cast;
import com.gengoai.parsing.Lexer;
import com.gengoai.parsing.ParserToken;
import com.gengoai.parsing.TokenDef;
import com.gengoai.parsing.TokenStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.string.Re.*;

/**
 * @author David B. Bracewell
 */
public enum ContextPatternParser implements TokenDef {
   STRICT {
      @Override
      public List<List<FeatureGetter>> generate(ParserToken token, TokenStream tokenStream) {
         return Collections.emptyList();
      }

      @Override
      public String getPattern() {
         return "~";
      }
   },
   PIPE {
      @Override
      public List<List<FeatureGetter>> generate(ParserToken token, TokenStream tokenStream) {
         return Collections.emptyList();
      }

      @Override
      public String getPattern() {
         return "\\|";
      }
   },
   NGRAM {
      @Override
      public String getPattern() {
         return re(q("<"),
                   namedGroup("", oneOrMore(DIGIT)), //0
                   zeroOrOne("\\s*,\\s*",
                             namedGroup("", oneOrMore(DIGIT)) //1
                            ),
                   q(">"));
      }

      @Override
      public List<List<FeatureGetter>> generate(ParserToken token, TokenStream tokenStream) {
         List<List<FeatureGetter>> getters = new ArrayList<>();

         int nMin = Integer.parseInt(token.getVariable(0));
         int nMax = token.getVariable(1) != null
                    ? Integer.parseInt(token.getVariable(1))
                    : nMin;

         ParserToken nt = tokenStream.consume();
         List<List<FeatureGetter>> subFeatures = ((ContextPatternParser) nt.getType()).generate(nt, tokenStream);
         for(int i = 0; i <= subFeatures.size(); i++) {
            for(int j = i + nMin; j <= i + nMax && j <= subFeatures.size(); j++) {
               getters.add(Lists.asArrayList(Iterables.flatten(subFeatures.subList(i, j))));
            }
         }
         return getters;
      }
   },
   PREFIX {
      @Override
      public List<List<FeatureGetter>> generate(ParserToken token, TokenStream tokenStream) {
         String[] prefixes = (token.getVariable(0) != null
                              ? token.getVariable(0)
                              : token.getVariable(1))
               .split("\\s*,\\s*");
         int low = Integer.parseInt(token.getVariable(2));

         if(token.getVariable(3) == null) {
            return List.of(Stream.of(prefixes)
                                 .map(p -> new FeatureGetter(low, p))
                                 .collect(Collectors.toList()));
         }

         String op = token.getVariable(3);
         int high = Integer.parseInt(token.getVariable(4));
         final List<List<FeatureGetter>> getters = new ArrayList<>();
         if(op.equals(",")) {
            for(int i = low; i <= high; i++) {
               final int offset = i;
               getters.add(Stream.of(prefixes)
                                 .map(p -> new FeatureGetter(offset, p))
                                 .collect(Collectors.toList()));
            }
         } else {
            List<FeatureGetter> l = new ArrayList<>();
            for(int i = low; i <= high; i++) {
               final int offset = i;
               l.addAll(Stream.of(prefixes)
                              .map(p -> new FeatureGetter(offset, p))
                              .collect(Collectors.toList()));
            }
            getters.add(l);
         }
         return getters;
      }

      @Override
      public String getPattern() {
         return re(
               or(namedGroup("", greedyOneOrMore("\\w")), //0
                  re(q("("), namedGroup("", oneOrMore(notChars(q(")")))), q(")"))), //1
               q("["),
               namedGroup("", zeroOrOne(chars("-+")), oneOrMore(DIGIT)), //2
               zeroOrOne("\\s*",
                         namedGroup("", or(q(".."), ",")), //3
                         "\\s*",
                         namedGroup("", zeroOrOne(chars("-+")), oneOrMore(DIGIT)) //4
                        ),
               q("]"));
      }
   };

   private static final Lexer lexer = Lexer.create(ContextPatternParser.values());

   public static <T> List<ContextFeaturizer<T>> parse(String pattern) {
      TokenStream ts = lexer.lex(pattern);
      List<List<FeatureGetter>> extractors = Collections.emptyList();

      AtomicBoolean isStrict = new AtomicBoolean(false);
      while(ts.hasNext()) {
         ParserToken token = ts.consume();
         if(token.getType() == STRICT) {
            isStrict.set(true);
            continue;
         }
         List<List<FeatureGetter>> getters = ((ContextPatternParser) token.getType()).generate(token, ts);
         if(ts.hasNext()) {
            ts.consume(PIPE);
         }

         if(extractors.isEmpty()) {
            extractors = getters;
         } else {
            List<List<FeatureGetter>> out = new ArrayList<>();
            for(List<FeatureGetter> extractor : extractors) {
               for(List<FeatureGetter> getter : getters) {
                  out.add(new ArrayList<>(extractor));
                  out.get(out.size() - 1).addAll(getter);
               }
            }
            extractors = out;
         }

      }
      return Cast.cast(extractors.stream()
                                 .map(l -> new ContextFeaturizerImpl<>(isStrict.get(), l))
                                 .collect(Collectors.toList()));
   }

   protected abstract List<List<FeatureGetter>> generate(ParserToken token, TokenStream tokenStream);

}
