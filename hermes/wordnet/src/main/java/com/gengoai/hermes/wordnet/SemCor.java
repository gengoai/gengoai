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

package com.gengoai.hermes.wordnet;

import com.gengoai.Language;
import com.gengoai.config.Config;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.en.ENWordSenseAnnotator;
import com.gengoai.hermes.format.*;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import com.gengoai.tuple.Tuple4;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class SemCor extends WholeFileTextFormat {
   final static Pattern docIdPattern = Pattern.compile("<context filename=(\\S+)");
   final static Pattern sentencePattern = Pattern
         .compile("<s\\s*[^>]+>(.*?)</s>", Pattern.MULTILINE | Pattern.DOTALL);
   final static Pattern wordPattern = Pattern.compile("<(?:wf|punc)[^>]*>(.*?)</(?:wf|punc)>");
   final static Pattern cmd = Pattern.compile("cmd=(\\S+)");
   final static Pattern lemma = Pattern.compile("lemma=(\\S+)");
   final static Pattern wnsn = Pattern.compile("wnsn=(\\d+)");
   final static Pattern posPattern = Pattern.compile("pos=(\\S+)");

   public static void main(String[] args) throws Exception {
      Config.initializeTest();
      Config.setProperty(Corpus.REPORT_LEVEL, Level.INFO.getName());
      Config.setProperty(Corpus.REPORT_INTERVAL, 5);
//      DocumentCollection docs = DocumentCollection
//            .create("semcor::///data/corpora/en/WordSense/semcor3.0/brown1/tagfiles/")
//            .cache()
//            .annotate(Types.PHRASE_CHUNK, Types.ENTITY, Types.LEMMA);
//      docs.export("conll::/home/ik/temp/;fields=INDEX,WORD,POS,CHUNK,ENTITY,IGNORE,CATEGORY,SENSE");
      DocumentCollection docs = DocumentCollection
            .create("conll::/home/ik/temp/documents/part-000;fields=INDEX,WORD,POS,CHUNK,ENTITY,SUPER_SENSE,IGNORE,IGNORE,IGNORE")
            .cache();
      System.out.println(docs.size());
      docs.forEach(d -> {
         for (Annotation annotation : d.annotations(Types.MWE)) {
            System.out.println(annotation.toSGML());
         }
      });
   }

   @Override
   public DocFormatParameters getParameters() {
      return new DocFormatParameters();
   }

   @Override
   protected Stream<Document> readSingleFile(String content) {
      Matcher m = docIdPattern.matcher(content);
      String id = null;
      if (m.find()) {
         id = m.group(1);
      }
      m = sentencePattern.matcher(content);
      List<String> tokens = new ArrayList<>();
      List<Tuple2<Integer, Integer>> sentences = new ArrayList<>();
      List<PartOfSpeech> pos = new ArrayList<>();
      List<Tuple4<Integer, Integer, String, Integer>> senses = new ArrayList<>();
      while (m.find()) {
         var wfm = wordPattern.matcher(m.group(1));
         int sstart = tokens.size();
         int start = tokens.size();
         while (wfm.find()) {
            String wf = wfm.group(0);
            String lemmaValue = Strings.firstMatch(lemma, wf, 1);
            String wnsnValue = Strings.firstMatch(wnsn, wf, 1);
            String posValue = Strings.firstMatch(posPattern, wf, 1);
            List<String> lt = new ArrayList<>();
            for (String s : wfm.group(1).split("_")) {
               lt.add(POSCorrection.word(s, posValue));
            }
            int end = start + lt.size() - 1;
            tokens.addAll(lt);
            if (Strings.isNotNullOrBlank(lemmaValue) &&
                  Strings.isNotNullOrBlank(wnsnValue) &&
                  Strings.isNotNullOrBlank(posValue)) {

               senses.add($(start, end, lemmaValue.replaceAll("_", " "), Math.max(1, Integer.parseInt(wnsnValue))));
               pos.add(PartOfSpeech.valueOf(posValue));
            }
            start = tokens.size();
         }
         sentences.add($(sstart, tokens.size() - 1));
      }

      Document document = DocumentFactory.getInstance().fromTokens(tokens, Language.ENGLISH);
      document.setId(id);
      int index = 0;
      for (Tuple2<Integer, Integer> sentence : sentences) {
         document.annotationBuilder(Types.SENTENCE)
                 .start(document.tokenAt(sentence.v1).start())
                 .end(document.tokenAt(sentence.v2).end())
                 .attribute(Types.INDEX, index)
                 .createAttached();
         index++;
      }

      for (int i = 0; i < senses.size(); i++) {
         PartOfSpeech posV = pos.get(i);
         Tuple4<Integer, Integer, String, Integer> sense = senses.get(i);
         Sense s = WordNet.getInstance()
                          .getSense(sense.v3, posV, sense.v4, Language.ENGLISH)
                          .orElse(null);
         if (s == null) {
            continue;
         }
         document.annotationBuilder(Types.WORD_SENSE)
                 .start(document.tokenAt(sense.v1).start())
                 .end(document.tokenAt(sense.v2).end())
                 .attribute(ENWordSenseAnnotator.SENSE, s)
                 .createAttached();
      }

      document.setCompleted(Types.SENTENCE, "PROVIDED");
      document.setCompleted(Types.TOKEN, "PROVIDED");
      document.setCompleted(Types.WORD_SENSE, "PROVIDED");

      return Stream.of(document);
   }

   @Override
   public void write(Document document, Resource outputResource) throws IOException {

   }

   @Override
   public void write(DocumentCollection documentCollection, Resource outputResource) throws IOException {

   }

   /**
    * The type Provider.
    */
   @MetaInfServices
   public static class Provider implements DocFormatProvider {

      @Override
      public DocFormat create(DocFormatParameters parameters) {
         return new SemCor();
      }

      @Override
      public String getName() {
         return "SEMCOR";
      }

      @Override
      public boolean isWriteable() {
         return false;
      }
   }

}//END OF SemCor
