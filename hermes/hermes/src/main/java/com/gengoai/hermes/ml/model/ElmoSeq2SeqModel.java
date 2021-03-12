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

package com.gengoai.hermes.ml.model;

import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.math.linalg.Shape;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.IOB;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

public abstract class ElmoSeq2SeqModel extends TFSequenceLabeler {
   public static final String LABEL = "label";
   public static final String TOKENS = "tokens";
   public static final String SEQUENCE_LENGTH = "seq_len";
   private static final long serialVersionUID = 1L;
   private final AnnotationType trainingAnnotationType;


   protected ElmoSeq2SeqModel(@NonNull AnnotationType annotationType,
                              @NonNull AnnotationType trainingAnnotationType) {
      super(
            List.of(
                  TFInputVar.sequence(TOKENS, NoOptEncoder.INSTANCE, -1),
                  TFInputVar.var(SEQUENCE_LENGTH, SEQUENCE_LENGTH, NoOptEncoder.INSTANCE)
            ),
            List.of(
                  TFOutputVar
                        .sequence(LABEL, "label_1/truediv", new IndexEncoder("0", Collections.singletonList("--PAD--")))
            ),
            IOB.decoder(annotationType)
      );
      this.trainingAnnotationType = trainingAnnotationType;
   }


   @Override
   public HStringDataSetGenerator getDataGenerator() {
      return HStringDataSetGenerator.builder(Types.SENTENCE)
                                    .source(TOKENS, h -> {
                                       int tokenLength = h.tokenLength();
                                       String[] tokenStrs = h.tokens()
                                                             .stream()
                                                             .map(Annotation::toString)
                                                             .toArray(String[]::new);
                                       return nd.DSTRING.array(Shape.shape(tokenLength, 1), tokenStrs);
                                    })
                                    .source(SEQUENCE_LENGTH,
                                            h -> nd.DINT32.scalar(h.tokenLength()))
                                    .source(LABEL, IOB.encoder(trainingAnnotationType))
                                    .build();
   }

}//END OF ElmoSeq2SeqModel
