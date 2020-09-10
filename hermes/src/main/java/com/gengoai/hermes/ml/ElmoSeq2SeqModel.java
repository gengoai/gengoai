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

package com.gengoai.hermes.ml;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.TensorUtils;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.collection.Maps;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.Types;
import lombok.NonNull;
import org.tensorflow.Tensor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gengoai.tuple.Tuples.$;

public abstract class ElmoSeq2SeqModel extends TensorFlowSequenceLabeler {
   public static final String LABEL = "label";
   public static final String TOKENS = "tokens";
   public static final String SEQUENCE_LENGTH = "seq_len";
   private static final long serialVersionUID = 1L;
   private final AnnotationType trainingAnnotationType;


   protected ElmoSeq2SeqModel(@NonNull AnnotationType annotationType,
                              @NonNull AnnotationType trainingAnnotationType) {
      super(Set.of(TOKENS),
//            Maps.linkedHashMapOf($(LABEL, "label/truediv")),
            Maps.linkedHashMapOf($(LABEL, "label_1/truediv")),
            Collections.emptyMap(),
            IOBValidator.INSTANCE,
            IOB.decoder(annotationType));
      this.trainingAnnotationType = trainingAnnotationType;
   }


   @Override
   protected Map<String, Tensor<?>> createTensors(DataSet batch) {
      return TensorUtils.sequence2StringTensor(batch, TOKENS, TOKENS, SEQUENCE_LENGTH);
   }

   @Override
   protected Transformer createTransformer() {
      IndexingVectorizer labelVectorizer = new IndexingVectorizer(encoders.get(LABEL)).source(LABEL);
      return new Transformer(List.of(labelVectorizer));
   }

   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .tokenSequence(TOKENS, h -> Variable.binary(h.toString()))
                                    .source(LABEL, IOB.encoder(trainingAnnotationType))
                                    .build();
   }

}//END OF ElmoSeq2SeqModel
