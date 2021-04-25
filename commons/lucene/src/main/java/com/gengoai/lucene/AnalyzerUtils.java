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

import com.gengoai.Language;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.bn.BengaliAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lt.LithuanianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public final class AnalyzerUtils {

   private AnalyzerUtils() {
      throw new IllegalAccessError();
   }

   @SneakyThrows
   public static Counter<String> analyze(@NonNull Analyzer analyzer, @NonNull String text) {
      return analyze(analyzer, Resources.fromString(text));
   }

   public static Counter<String> analyze(@NonNull Analyzer analyzer, @NonNull Resource text) throws IOException {
      Counter<String> c = Counters.newCounter();
      try (Reader reader = text.reader()) {
         TokenStream tokenStream = analyzer.tokenStream(null, reader);
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
         return c;
      }
   }

   public static Analyzer analyzerForLanguage(@NonNull Language language) {
      return analyzerForLanguage(language, true);
   }

   public static Analyzer analyzerForLanguage(@NonNull Language language, boolean removeStopWords) {
      switch (language) {
         case ARABIC:
            return removeStopWords
                  ? new ArabicAnalyzer()
                  : new ArabicAnalyzer(CharArraySet.EMPTY_SET);
         case ARMENIAN:
            return removeStopWords
                  ? new ArmenianAnalyzer()
                  : new ArmenianAnalyzer(CharArraySet.EMPTY_SET);
         case BASQUE:
            return removeStopWords
                  ? new BasqueAnalyzer()
                  : new BasqueAnalyzer(CharArraySet.EMPTY_SET);
         case BENGALI:
            return removeStopWords
                  ? new BengaliAnalyzer()
                  : new BengaliAnalyzer(CharArraySet.EMPTY_SET);
         case BULGARIAN:
            return removeStopWords
                  ? new BulgarianAnalyzer()
                  : new BulgarianAnalyzer(CharArraySet.EMPTY_SET);
         case CATALAN:
            return removeStopWords
                  ? new CatalanAnalyzer()
                  : new CatalanAnalyzer(CharArraySet.EMPTY_SET);
         case CZECH:
            return removeStopWords
                  ? new CzechAnalyzer()
                  : new CzechAnalyzer(CharArraySet.EMPTY_SET);
         case DANISH:
            return removeStopWords
                  ? new DanishAnalyzer()
                  : new DanishAnalyzer(CharArraySet.EMPTY_SET);
         case ENGLISH:
            return removeStopWords
                  ? new EnglishAnalyzer()
                  : new EnglishAnalyzer(CharArraySet.EMPTY_SET);
         case FINNISH:
            return removeStopWords
                  ? new FinnishAnalyzer()
                  : new FinnishAnalyzer(CharArraySet.EMPTY_SET);
         case FRENCH:
            return removeStopWords
                  ? new FrenchAnalyzer()
                  : new FrenchAnalyzer(CharArraySet.EMPTY_SET);
         case GALICIAN:
            return removeStopWords
                  ? new GalicianAnalyzer()
                  : new GalicianAnalyzer(CharArraySet.EMPTY_SET);
         case GERMAN:
            return removeStopWords
                  ? new GermanAnalyzer()
                  : new GermanAnalyzer(CharArraySet.EMPTY_SET);
         case GREEK:
            return removeStopWords
                  ? new GreekAnalyzer()
                  : new GreekAnalyzer(CharArraySet.EMPTY_SET);
         case HINDI:
            return removeStopWords
                  ? new HindiAnalyzer()
                  : new HindiAnalyzer(CharArraySet.EMPTY_SET);
         case HUNGARIAN:
            return removeStopWords
                  ? new HungarianAnalyzer()
                  : new HungarianAnalyzer(CharArraySet.EMPTY_SET);
         case INDONESIAN:
            return removeStopWords
                  ? new IndonesianAnalyzer()
                  : new IndonesianAnalyzer(CharArraySet.EMPTY_SET);
         case IRISH:
            return removeStopWords
                  ? new IrishAnalyzer()
                  : new IrishAnalyzer(CharArraySet.EMPTY_SET);
         case ITALIAN:
            return removeStopWords
                  ? new ItalianAnalyzer()
                  : new ItalianAnalyzer(CharArraySet.EMPTY_SET);
         case LATVIAN:
            return removeStopWords
                  ? new LatvianAnalyzer()
                  : new LatvianAnalyzer(CharArraySet.EMPTY_SET);
         case LITHUANIAN:
            return removeStopWords
                  ? new LithuanianAnalyzer()
                  : new LithuanianAnalyzer(CharArraySet.EMPTY_SET);
         case NORWEGIAN:
            return removeStopWords
                  ? new NorwegianAnalyzer()
                  : new NorwegianAnalyzer(CharArraySet.EMPTY_SET);
         case PERSIAN:
            return removeStopWords
                  ? new PersianAnalyzer()
                  : new PersianAnalyzer(CharArraySet.EMPTY_SET);
         case PORTUGUESE:
            return removeStopWords
                  ? new PortugueseAnalyzer()
                  : new PortugueseAnalyzer(CharArraySet.EMPTY_SET);
         case ROMANIAN:
            return removeStopWords
                  ? new RomanianAnalyzer()
                  : new RomanianAnalyzer(CharArraySet.EMPTY_SET);
         case RUSSIAN:
            return removeStopWords
                  ? new RussianAnalyzer()
                  : new RussianAnalyzer(CharArraySet.EMPTY_SET);
         case KURDISH:
            return removeStopWords
                  ? new SoraniAnalyzer()
                  : new SoraniAnalyzer(CharArraySet.EMPTY_SET);
         case SPANISH:
            return removeStopWords
                  ? new SpanishAnalyzer()
                  : new SpanishAnalyzer(CharArraySet.EMPTY_SET);
         case SWEDISH:
            return removeStopWords
                  ? new SwedishAnalyzer()
                  : new SwedishAnalyzer(CharArraySet.EMPTY_SET);
         case THAI:
            return removeStopWords
                  ? new ThaiAnalyzer()
                  : new ThaiAnalyzer(CharArraySet.EMPTY_SET);
         case TURKISH:
            return removeStopWords
                  ? new TurkishAnalyzer()
                  : new TurkishAnalyzer(CharArraySet.EMPTY_SET);
         case CHINESE:
         case JAPANESE:
         case KOREAN:
            return removeStopWords
                  ? new CJKAnalyzer()
                  : new CJKAnalyzer(CharArraySet.EMPTY_SET);
         default:
            return new StandardAnalyzer();
      }
   }

   public static List<String> tokenize(@NonNull Analyzer analyzer, @NonNull Resource text) throws IOException {
      List<String> c = new ArrayList<>();
      try (Reader reader = text.reader()) {
         TokenStream tokenStream = analyzer.tokenStream(null, reader);
         CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
         tokenStream.reset();
         while (tokenStream.incrementToken()) {
            String token = cattr.toString();
            if (token.length() == 0) {
               continue;
            }
            c.add(cattr.toString());
         }
         tokenStream.end();
         tokenStream.close();
         return c;
      }
   }

   @SneakyThrows
   public static List<String> tokenize(@NonNull Analyzer analyzer, @NonNull String text) {
      return tokenize(analyzer, Resources.fromString(text));
   }

}//END OF AnalyzerUtils
