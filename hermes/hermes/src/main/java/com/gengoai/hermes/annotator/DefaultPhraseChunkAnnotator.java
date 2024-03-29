/*
 * (c) 2005 David B. Bracewell
 *
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
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.model.IOBTagger;

import java.util.Collections;
import java.util.Set;

/**
 * Default Phrase Chunk annotator that use an IOBTagger.
 *
 * @author David B. Bracewell
 */
public class DefaultPhraseChunkAnnotator extends SentenceLevelAnnotator {
   private static final long serialVersionUID = 1L;
   private static final Cache<Language, IOBTagger> cache = ResourceType.MODEL.createCache("Annotation.PHRASE_CHUNK",
                                                                                          "phrase_chunk");

   @Override
   protected void annotate(Annotation sentence) {
      cache.get(sentence.getLanguage()).apply(sentence);
   }

   @Override
   protected Set<AnnotatableType> furtherRequires() {
      return Collections.singleton(Types.PART_OF_SPEECH);
   }

   @Override
   public String getProvider(Language language) {
      return "IOBTagger v" + cache.get(language).getVersion();
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.PHRASE_CHUNK);
   }

}//END OF DefaultPhraseChunkAnnotator
