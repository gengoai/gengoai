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

import com.gengoai.collection.Iterables;
import com.gengoai.hermes.*;
import com.gengoai.hermes.en.ENWordSenseAnnotator;
import com.gengoai.hermes.format.CoNLLColumnProcessor;
import com.gengoai.hermes.format.CoNLLRow;
import com.gengoai.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
@MetaInfServices
public class WordNetProcessor implements CoNLLColumnProcessor {
   @Override
   public String getFieldName() {
      return "SENSE";
   }

   @Override
   public void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      documentRows
            .stream()
            .filter(row -> row.hasOther("SENSE"))
            .forEach(row -> document.annotation(row.getAnnotationID())
                                    .ifNotEmpty(a -> {
                                       Set<BasicCategories> cats = Types.CATEGORY.decode(row.getOther("SENSE"));
                                       a.put(Types.CATEGORY, cats);
                                    }));
   }

   @Override
   public String processOutput(HString document, Annotation token, int index) {
      Annotation ws = Iterables.getFirst(token.annotations(Types.WORD_SENSE), null);
      if (ws != null) {
         List<Sense> senses = ws.attribute(ENWordSenseAnnotator.SENSE);
         if (senses.size() > 0) {
            Sense s = Iterables.getFirst(senses, null);
            if (ws.firstToken() == token) {
               return "B-" + s.getSynset().getLexicographerFile().name();
            }
            return "I-" + s.getSynset().getLexicographerFile().name();
         }
      }
      return "O";
   }
}//END OF CategoryProcessor
