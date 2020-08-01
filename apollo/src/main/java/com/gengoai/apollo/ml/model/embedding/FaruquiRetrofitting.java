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

package com.gengoai.apollo.ml.model.embedding;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.collection.Sets;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.io.resource.Resource;
import com.gengoai.math.Math2;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Implementation of <b>Retrofitting Word Vectors to Semantic Lexicons</b> by Faruqui et al.</p>
 *
 * @author David B. Bracewell
 */
public class FaruquiRetrofitting implements Retrofitting {
   private static final long serialVersionUID = 1L;
   private final int iterations;
   private final Multimap<String, String> lexicon = new HashSetMultimap<>();

   /**
    * Instantiates a new Faruqui retrofitting with a maximum of 25 iterations.
    */
   public FaruquiRetrofitting() {
      this(25);
   }

   /**
    * Instantiates a new Faruqui retrofitting.
    *
    * @param iterations the iterations
    */
   public FaruquiRetrofitting(int iterations) {
      this.iterations = iterations;
   }

   @Override
   public WordEmbedding apply(@NonNull WordEmbedding origVectors) {
      Set<String> sourceVocab = new HashSet<>(origVectors.getAlphabet());
      Set<String> sharedVocab = Sets.intersection(sourceVocab, lexicon.keySet());
      Map<String, NDArray> unitNormedVectors = new HashMap<>();
      Map<String, NDArray> retrofittedVectors = new HashMap<>();

      //Unit Normalize the vectors
      sourceVocab.forEach(w -> {
         NDArray v = origVectors.embed(w).unitize();
         retrofittedVectors.put(w, v);
         unitNormedVectors.put(w, v.copy());
      });

      for(int i = 0; i < iterations; i++) {
         sharedVocab.forEach(retrofitTerm -> {
            Set<String> similarTerms = Sets.intersection(lexicon.get(retrofitTerm), sourceVocab);
            if(similarTerms.size() > 0) {
               //Get the original unit normalized vector for the term we are retrofitting
               NDArray newTermVector = unitNormedVectors.get(retrofitTerm)
                                                        .mul(similarTerms.size());

               //Sum the vectors of the similar terms using the retrofitted vectors
               //from last iteration
               similarTerms.forEach(similarTerm -> newTermVector.addi(retrofittedVectors.get(similarTerm)));

               //Normalize and update
               double div = 2.0 * similarTerms.size();//v.magnitude() + 1e-6;
               newTermVector.divi((float) div);
               retrofittedVectors.put(retrofitTerm, newTermVector);
            }
         });
      }

      PreTrainedWordEmbedding out = new PreTrainedWordEmbedding();
      out.vectorStore = new InMemoryVectorStore(origVectors.dimension());
      for(String key : origVectors.getAlphabet()) {
         int i = out.vectorStore.addOrGetIndex(key);
         if(retrofittedVectors.containsKey(key)) {
            out.vectorStore.updateVector(i, retrofittedVectors.get(key).unitize());
         } else {
            out.vectorStore.updateVector(i, origVectors.embed(key).unitize());
         }
      }
      return out;
   }

   private void loadLexicon(Resource resource, Multimap<String, String> lexicon) throws IOException {
      resource.forEach(line -> {
         String[] parts = line.toLowerCase().trim().split("\\s+");
         String word = norm(parts[0]);
         for(int i = 1; i < parts.length; i++) {
            lexicon.put(word, norm(parts[i]));
         }
      });
   }

   private String norm(String string) {
      if(Math2.tryParseDouble(string) != null) {
         return "---num---";
      } else if(Strings.isPunctuation(string)) {
         return "---punc---";
      }
      return string.toLowerCase().replace('_', ' ');
   }

   /**
    * Sets lexicon.
    *
    * @param resource the resource
    * @throws IOException the io exception
    */
   public void setLexicon(Resource resource) throws IOException {
      lexicon.clear();
      loadLexicon(resource, lexicon);
   }

}//END OF FaruquiRetrofitting
