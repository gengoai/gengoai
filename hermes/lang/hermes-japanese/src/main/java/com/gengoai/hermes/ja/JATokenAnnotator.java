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

import com.atilika.kuromoji.TokenizerBase;
import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.gengoai.Language;
import com.gengoai.config.Config;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.morphology.PartOfSpeech;

import java.util.Map;
import java.util.Set;

public class JATokenAnnotator extends Annotator {
    private static final long serialVersionUID = 1L;

    private final Tokenizer tokenizer = new Tokenizer.Builder().mode(TokenizerBase.Mode.NORMAL).build();


    @Override
    protected void annotateImpl(Document document) {
        int sentenceStart = 0;
        int sentenceIndex = 0;

        for (Token token : tokenizer.tokenize(document.toString())) {

            if (token.getSurface().strip().length() == 0) {
                continue;
            }

            int charStart = token.getPosition();
            int charEnd = charStart + token.getSurface().length();
            var hToken = document.createAnnotation(Types.TOKEN,
                    charStart,
                    charEnd,
                    Map.of());


            if (token.getSurface().equals("。") ||
                    token.getSurface().equals("？") ||
                    token.getSurface().equals("?") ||
                    token.getSurface().equals("!") ||
                    token.getSurface().equals(".") ||
                    token.getSurface().equals("！")) {
                document.createAnnotation(Types.SENTENCE,
                        sentenceStart,
                        token.getPosition() + 1,
                        Map.of(Types.INDEX, sentenceIndex));
                sentenceStart = token.getPosition() + 1;
                sentenceIndex += 1;
            }

            if (token.getBaseForm().equals("*")) {
                hToken.put(Types.LEMMA, token.getSurface());
            } else {
                hToken.put(Types.LEMMA, token.getBaseForm());
            }

            String pos = token.getPartOfSpeechLevel1();
            if (!token.getPartOfSpeechLevel2().equals("*")) {
                pos += token.getPartOfSpeechLevel2();
            }
            hToken.put(Types.PART_OF_SPEECH, PartOfSpeech.valueOf(pos));
        }

        if (sentenceStart < document.length() && document.substring(sentenceStart, document.length()).tokenLength() > 0) {
            document.createAnnotation(Types.SENTENCE,
                    sentenceStart,
                    document.length(),
                    Map.of(Types.INDEX, sentenceIndex));
        }

    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.TOKEN, Types.LEMMA, Types.PART_OF_SPEECH, Types.SENTENCE);
    }

    public static void main(String[] args) throws Exception {
        Config.initialize("Sandbox", args);
        Document doc = Document.create("気象庁によりますと、山口県では、活発な雨雲が連なる「線状降水帯」が発生し、非常に激しい雨が降り続いています。気象庁は、災害の危険度が急激に高まっているとして、緊急の情報を出し、厳重な警戒を呼びかけています。"
                , Language.JAPANESE);
        doc.annotate(Types.TOKEN, Types.PHRASE_CHUNK);
        for (Annotation sentence : doc.sentences()) {
            System.out.println(sentence.toPOSString());
            for (Annotation pc : sentence.annotations(Types.PHRASE_CHUNK)) {
                System.out.println(pc + " : " + pc.pos());
            }
            System.out.println();
        }
    }


}
