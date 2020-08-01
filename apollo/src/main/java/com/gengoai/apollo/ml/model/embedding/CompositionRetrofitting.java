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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * <p>
 * Retrofits a vector store by composing the vectors with the same term and optionally neighbors in a secondary
 * background vector store. Mathematically it is defined as:
 * </p>
 * <pre>
 *    e_i = e_i + b_i + sum_[j=0 to neighborSize](b_n_j * neighborWeight if sim(b_n_j,e_i) >= neighborThreshold)
 * </pre>
 *
 * @author David B. Bracewell
 */
public class CompositionRetrofitting implements Retrofitting {
   private static final long serialVersionUID = 1L;
   private final WordEmbedding background;
   @Getter
   @Setter
   private int neighborSize = 0;
   @Getter
   @Setter
   private double neighborThreshold = 0.5;
   @Getter
   @Setter
   private double neighborWeight = 0;

   /**
    * Instantiates a new Composition retrofitting.
    *
    * @param background the background WordEmbedding
    */
   public CompositionRetrofitting(@NonNull WordEmbedding background) {
      this.background = background;
   }

   @Override
   public WordEmbedding apply(@NonNull WordEmbedding embedding) {
      PreTrainedWordEmbedding out = new PreTrainedWordEmbedding();
      out.vectorStore = new InMemoryVectorStore(embedding.dimension(),
                                                embedding.vectorStore.getUnknownKey(),
                                                embedding.vectorStore.getSpecialKeys());
      for(String key : embedding.getAlphabet()) {
         int index = out.vectorStore.addOrGetIndex(key);
         NDArray tv = embedding.embed(key).copy();
         if(background.getAlphabet().contains(key)) {
            tv.addi(background.embed(key));
            if(neighborSize > 0 && neighborWeight > 0) {
               background.query(VSQuery.termQuery(key)
                                       .limit(neighborSize)
                                       .threshold(neighborThreshold))
                         .forEach(n -> tv.addi(n.mul((float) neighborWeight)));
            }
         }
         out.vectorStore.updateVector(index, tv);
      }
      return out;
   }

}// END OF CompositionRetrofitting
