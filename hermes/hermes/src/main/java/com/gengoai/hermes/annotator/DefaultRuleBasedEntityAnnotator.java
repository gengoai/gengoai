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
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.Types;

import java.util.Collections;
import java.util.Set;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class DefaultRuleBasedEntityAnnotator extends Annotator {
   private static final long serialVersionUID = 1L;
   private static final Cache<Language, NerPatterns> languagePatterns = ResourceType.NER_PATTERNS
         .createCache("Annotation.RULE_BASED_ENTITY",
                      "ner");

   @Override
   public Set<AnnotatableType> requires() {
      return Collections.singleton(Types.SENTENCE);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.RULE_BASED_ENTITY);
   }

   @Override
   protected void annotateImpl(Document document) {
      NerPatterns patterns = languagePatterns.get(document.getLanguage());
      patterns.process(document);
   }

}//END OF DefaultRuleBasedEntityAnnotator
