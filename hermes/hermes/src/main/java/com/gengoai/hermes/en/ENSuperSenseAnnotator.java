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

package com.gengoai.hermes.en;

import com.gengoai.Language;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.collection.Sets;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.annotator.Annotator;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class ENSuperSenseAnnotator extends Annotator {
   private final SuperSenseTagger tagger;

   public ENSuperSenseAnnotator() {
      try {
         tagger = ModelIO.load(ResourceType.MODEL.locate("supersense", Language.ENGLISH).orElseThrow());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   protected void annotateImpl(Document document) {
      tagger.apply(document);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.SUPER_SENSE);
   }

   @Override
   public Set<AnnotatableType> requires() {
      return Sets.hashSetOf(Types.TOKEN, Types.SENTENCE, Types.PART_OF_SPEECH, Types.PHRASE_CHUNK);
   }
}
