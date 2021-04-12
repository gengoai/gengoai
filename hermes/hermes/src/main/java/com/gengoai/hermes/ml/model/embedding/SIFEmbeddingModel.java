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

package com.gengoai.hermes.ml.model.embedding;

import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.math.linalg.decompose.SingularValueDecomposition;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.embedding.WordEmbedding;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.HStringMLModel;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class SIFEmbeddingModel implements HStringMLModel {
   @NonNull
   private final WordEmbedding embedding;
   @NonNull
   private final Map<String, Double> wordCounts;
   @NonNull
   private final double alpha;
   private final double defaultTermCount;

   public SIFEmbeddingModel(WordEmbedding embedding, Map<String, Double> wordCounts, double alpha, double defaultTermCount) {
      this.embedding = embedding;
      this.wordCounts = wordCounts;
      this.alpha = alpha;
      this.defaultTermCount = defaultTermCount;
   }

   @Override
   public HString apply(@NonNull HString hString) {
      List<NumericNDArray> e = sif(hString);
      List<Annotation> sentences = hString.sentences();
      for (int i = 0; i < e.size(); i++) {
         sentences.get(i).put(Types.EMBEDDING, e.get(i));
      }
      return hString;
   }

   @Override
   public Model delegate() {
      return this;
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      throw new UnsupportedOperationException();
   }

   @Override
   public String getVersion() {
      return "SIFEmbedding";
   }

   @Override
   public void setVersion(String version) {
      throw new UnsupportedOperationException();
   }

   protected List<NumericNDArray> sif(HString h) {
      if( h.sentences().size() == 1){
         return List.of(weightedAvg(h.sentence()));
      }
      NumericNDArray m = nd.vstack(h.sentences().stream().map(this::weightedAvg).collect(toList()));
      SingularValueDecomposition svd = new SingularValueDecomposition(1);
      NumericNDArray[] uSVT = svd.decompose(m);
      NumericNDArray pc = uSVT[0].mmul(uSVT[1]).mmul(uSVT[2]);
      NumericNDArray v = m.sub(nd.dot(m, pc.T()).mmul(pc));
      List<NumericNDArray> r = new ArrayList<>();
      for (int i = 0; i < v.rows(); i++) {
         r.add(v.getAxis(Shape.ROW, i));
      }
      return r;
   }

   protected NumericNDArray weightedAvg(HString sentence) {
      NumericNDArray wordEmbeddings = nd.DFLOAT32.zeros(sentence.tokenLength(), embedding.dimension());
      NumericNDArray w = nd.DFLOAT32.zeros(1, sentence.tokenLength());
      int nonZero = 0;
      for (int i = 0; i < sentence.tokenLength(); i++) {
         if (!sentence.tokenAt(i).hasAttribute(Types.EMBEDDING)) {
            sentence.tokenAt(i).put(Types.EMBEDDING, embedding.embed(sentence.tokenAt(i).toLowerCase()));
         }
         NumericNDArray e = sentence.tokenAt(i).attribute(Types.EMBEDDING);
         wordEmbeddings.setAxis(Shape.ROW, i, e);
         w.set(i, wordCounts.getOrDefault(sentence.tokenAt(i).toLowerCase(),  alpha * (alpha  + defaultTermCount)));
         if (w.getDouble(i) != 0) {
            nonZero++;
         }
      }
      var wa = w.mmul(wordEmbeddings).divi(nonZero);
      return wa;
   }

}//END OF SIFEmbeddingModel
