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
import com.gengoai.apollo.model.embedding.KeyedVectorStore;
import com.gengoai.hermes.ResourceType;
import com.gengoai.hermes.lexicon.WordList;

/**
 * Common Resources for English
 */
public final class ENResources {

   public static final String GLOVE_LARGE = "glove.840b.300d";
   public static final String GLOVE_SMALL_50 = "glove.6b.50d";
   public static final String GLOVE_SMALL_100 = "glove.6b.100d";
   public static final String GLOVE_SMALL_300 = "glove.6b.300d";
   public static final String GLOVE_SMALL_ALPHABET = "glove";
   public static final String GLOVE_LARGE_ALPHABET = "glove_large";


   private ENResources() {
      throw new IllegalAccessError();
   }

   public static KeyedVectorStore gloveLargeEmbeddings() {
      return ResourceType.EMBEDDINGS.load(GLOVE_LARGE, Language.ENGLISH);
   }

   public static WordList gloveLargeLexicon() {
      return ResourceType.WORD_LIST.load(GLOVE_LARGE_ALPHABET, Language.ENGLISH);
   }

   public static WordList gloveSmallLexicon() {
      return ResourceType.WORD_LIST.load(GLOVE_SMALL_ALPHABET, Language.ENGLISH);
   }

   public static KeyedVectorStore gloveSmallEmbeddings(int dimension) {
      switch (dimension) {
         case 50:
            return ResourceType.EMBEDDINGS.load(GLOVE_SMALL_50, Language.ENGLISH);
         case 100:
            return ResourceType.EMBEDDINGS.load(GLOVE_SMALL_100, Language.ENGLISH);
         case 300:
            return ResourceType.EMBEDDINGS.load(GLOVE_SMALL_300, Language.ENGLISH);
      }
      throw new IllegalArgumentException("Invalid dimension of " + dimension);
   }

}//END OF ENResources
