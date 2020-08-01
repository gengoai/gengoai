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

import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.observation.Classification;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import lombok.NonNull;

/**
 * <p>
 * Enumeration of the varying types of outputs from Models. The label type defines how to decode an NDArray using an
 * Encoder.
 * </p>
 *
 * @author David B. Bracewell
 */
public enum LabelType {
   /**
    * Binary (true / false) classification.
    */
   BinaryClassification {
      @Override
      public Observation transform(Encoder encoder, @NonNull Observation observation) {
         return transformToClassification(encoder, observation);
      }

      @Override
      public Class<? extends Observation> getObservationClass() {
         return Classification.class;
      }
   },
   /**
    * Multi-class Classification
    */
   MultiClassClassification {
      @Override
      public Observation transform(Encoder encoder, @NonNull Observation observation) {
         return transformToClassification(encoder, observation);
      }

      @Override
      public Class<? extends Observation> getObservationClass() {
         return Classification.class;
      }
   },
   /**
    * Sequence of variables
    */
   Sequence {
      @Override
      public Observation transform(Encoder encoder, @NonNull Observation observation) {
         if(observation.isSequence()) {
            return observation;
         } else if(observation.isNDArray()) {
            VariableSequence sequence = new VariableSequence();
            com.gengoai.apollo.math.linalg.NDArray ndArray = observation.asNDArray();
            for(int i = 0; i < ndArray.rows(); i++) {
               if(ndArray.columns() == 1) {
                  sequence.add(Variable.binary(encoder.decode(ndArray.get(0))));
               } else {
                  long argmax = ndArray.argmax();
                  sequence.add(Variable.real(encoder.decode(argmax), ndArray.get(argmax)));
               }
            }
            return sequence;
         }
         throw new IllegalArgumentException("Expecting Sequence or NDArray, but found: " + observation.getClass());
      }

      @Override
      public Class<? extends Observation> getObservationClass() {
         return com.gengoai.apollo.ml.observation.Sequence.class;
      }
   },
   /**
    * NDArray output (topics, clustering, embedding, regression)
    */
   NDArray {
      @Override
      public Observation transform(Encoder encoder, @NonNull Observation observation) {
         if(observation.isNDArray()) {
            return observation;
         }
         throw new IllegalArgumentException("Expecting NDArray, but found: " + observation.getClass());
      }

      @Override
      public Class<? extends Observation> getObservationClass() {
         return com.gengoai.apollo.math.linalg.NDArray.class;
      }
   };

   /**
    * Determines the type of classification based on the number of labels.
    *
    * @param numLabels the number of labels
    * @return the label type
    */
   public static LabelType classificationType(int numLabels) {
      return numLabels > 2
             ? LabelType.MultiClassClassification
             : LabelType.BinaryClassification;
   }

   private static Observation transformToClassification(Encoder encoder, Observation observation) {
      if(observation.isClassification()) {
         return observation;
      } else if(observation.isNDArray()) {
         return new Classification(observation.asNDArray(), encoder);
      }
      throw new IllegalArgumentException("Expecting Classification or NDArray, but found: " + observation.getClass());
   }

   /**
    * Gets class information for the label Observation
    *
    * @return the observation class
    */
   public abstract Class<? extends Observation> getObservationClass();

   /**
    * Transforms (decodes) the given {@link Observation} with the given {@link Encoder}
    *
    * @param encoder     the encoder
    * @param observation the observation
    * @return the observation
    */
   public abstract Observation transform(Encoder encoder, @NonNull Observation observation);

}//END OF LabelType
