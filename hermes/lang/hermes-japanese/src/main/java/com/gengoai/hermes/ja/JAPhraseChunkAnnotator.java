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

package com.gengoai.hermes.ja;

import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.PennTreeBank;
import com.gengoai.parsing.ParseException;

import java.util.Map;
import java.util.Set;

public class JAPhraseChunkAnnotator extends Annotator {
    final TokenRegex tre;

    public JAPhraseChunkAnnotator() {
        try {
            this.tre = TokenRegex.compile(
                    "( (?<NP> (#ADJECTIVE | #NUMERAL)* #NOUN+ (　( #助詞連体化 | #助詞格助詞 |　'な') (#ADJECTIVE | #NUMERAL)* #NOUN)*　#NUMERAL*) ) | " +
                            " (?<VP> #名詞サ変接続* #VERB #AUXILIARY*)"
            );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void annotateImpl(Document document) {
        TokenMatcher matcher = tre.matcher(document);
        while (matcher.find()) {
            PartOfSpeech pos = matcher.group("NP").size() > 0 ? PennTreeBank.NP : PennTreeBank.VP;
            document.createAnnotation(Types.PHRASE_CHUNK,
                    matcher.group().start(),
                    matcher.group().end(),
                    Map.of(Types.PART_OF_SPEECH, pos));
        }
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.PHRASE_CHUNK);
    }

}
