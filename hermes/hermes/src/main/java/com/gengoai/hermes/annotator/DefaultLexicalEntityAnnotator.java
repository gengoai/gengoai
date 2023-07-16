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

import com.gengoai.Language;
import com.gengoai.cache.Cache;
import com.gengoai.hermes.*;
import com.gengoai.hermes.lexicon.Lexicon;

import java.util.Map;
import java.util.Set;

public class DefaultLexicalEntityAnnotator extends Annotator {
    private static final Cache<Language, Lexicon> cache = ResourceType.LEXICON.createCache("Annotation.LEXICAL_ENTITY",
            "entities_whitelist");
    private static final long serialVersionUID = 1L;

    @Override
    protected void annotateImpl(Document document) {
        Lexicon lexicon = cache.get(document.getLanguage());
        for (Annotation sentence : document.sentences()) {
            for (HString match : lexicon.extract(sentence)) {
                EntityType type = EntityType.valueOf(match.attribute(Types.MATCHED_TAG));
                double confidence = 0; //LET ML OVERRIDE
                document.createAnnotation(Types.LEXICAL_ENTITY,
                        match.start(),
                        match.end(),
                        Map.of(Types.ENTITY_TYPE, type,
                                Types.CONFIDENCE, confidence));
            }
        }

    }


    @Override
    public Set<AnnotatableType> requires() {
        return Set.of(Types.TOKEN, Types.SENTENCE);
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.LEXICAL_ENTITY);
    }
}
