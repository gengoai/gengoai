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

package com.gengoai.apollo.ml.model;

import com.gengoai.Validation;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.Sequence;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.util.Map;

public final class TensorUtils {


   private TensorUtils() {
      throw new IllegalAccessError();
   }


   public static Map<String, Tensor<?>> sequence2StringTensor(@NonNull DataSet batch,
                                                              @NonNull String tokenObservation,
                                                              @NonNull String tokenOutputName) {
      return sequence2StringTensor(batch, tokenObservation, tokenOutputName, null);
   }



   public static Map<String, Tensor<?>> sequence2StringTensor(@NonNull DataSet batch,
                                                              @NonNull String tokenObservation,
                                                              @NonNull String tokenOutputName,
                                                              String sequenceLengthOutputName) {
      Validation.notNullOrBlank(tokenObservation);
      Validation.notNullOrBlank(tokenOutputName);
      byte[][][] tokens = new byte[(int) batch.size()][][];
      int[] sequenceLength = new int[(int) batch.size()];
      int maxSentenceLength = (int) batch.stream()
                                         .mapToDouble(d -> d.get(tokenObservation).asSequence().size())
                                         .max().orElse(0);
      int si = 0;
      for (Datum datum : batch) {
         byte[][] example = new byte[maxSentenceLength][];
         Sequence<?> vs = datum.get(tokenObservation).asSequence();
         for (int i = 0; i < vs.size(); i++) {
            example[i] = vs.get(i).asVariable().getSuffix().getBytes();
         }
         for (int i = vs.size(); i < maxSentenceLength; i++) {
            example[i] = Strings.EMPTY.getBytes();
         }
         sequenceLength[si] = vs.size();
         tokens[si] = example;
         si++;
      }
      if (Strings.isNullOrBlank(sequenceLengthOutputName)) {
         return Map.of(tokenOutputName, Tensor.create(tokens));
      }
      return Map.of(tokenOutputName, Tensor.create(tokens),
                    sequenceLengthOutputName, Tensor.create(sequenceLength));
   }


}//END OF TensorUtils
