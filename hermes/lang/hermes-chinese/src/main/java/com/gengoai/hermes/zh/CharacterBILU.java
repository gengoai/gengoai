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
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.util.List;

public class CharacterBILU {

   public final static ObservationExtractor<HString> encoder = s -> {
      VariableSequence label = new VariableSequence();
      int end = 0;
      for (int i = 0; i < s.length(); i++) {
         List<Annotation> tokens = s.substring(i, i + 1).tokens();
         if (tokens.size() > 0) {
            Annotation token = tokens.get(0);
            String pos = token.pos().name();
            if (token.length() == 1) {
               label.add(Variable.binary("U-" + pos));
            } else if (token.start() - s.start() == i) {
               label.add(Variable.binary("B-" + pos));
            } else if (token.end() - s.start() - 1 == i) {
               label.add(Variable.binary("L-" + pos));
            } else {
               label.add(Variable.binary("I-" + pos));
            }
         } else {
            label.add(Variable.binary("O"));
         }
      }

      return label;
   };

   private CharacterBILU() {
      throw new IllegalAccessError();
   }

   private static void checkToken(Document document, int start, int end, String tag) {
      if (end - start > 0 && Strings.isNotNullOrBlank(document.substring(start, end))) {
         document.annotationBuilder(Types.TOKEN)
                 .start(start)
                 .end(end)
                 .attribute(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(tag))
                 .attribute(Types.LEMMA, document.substring(start, end).toString())
                 .createAttached();
      }
   }

   public static void decode(@NonNull Document document, VariableSequence vs) {
      int start = 0;
      String last = "O";
      for (int end = 0; end < vs.size(); end++) {
         String lbl = vs.get(end).getName();
         int ii = lbl.indexOf('-');
         String tag = ii >= 0 ? lbl.substring(ii + 1) : lbl;
         String enc = ii >= 0 ? lbl.substring(0, ii) : lbl;
         if (tag.equals("O")) {
            checkToken(document, start, end, last);
            start = end + 1;
         } else if (enc.equals("L")) {
            checkToken(document, start, end + 1, last);
            start = end + 1;
         } else if (enc.equals("B")) {
            checkToken(document, start, end, last);
            start = end;
            last = tag;
         } else if (enc.equals("U")) {
            checkToken(document, start, end, last);
            start = end;
            last = tag;
            checkToken(document, start, end + 1, last);
            start = end + 1;
         } else if (end + 1 == vs.size()) {
            checkToken(document, start, end + 1, last);
         }
      }
   }


}
