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

package com.gengoai.apollo.ml.encoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.transform.vectorizer.Vectorizer;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Set;

/**
 * <p>
 * An encoder represents a maps variable names to indexes. They are typically used in conjunction with a {@link
 * Vectorizer} in order to convert {@link Observation}s into a vector ({@link com.gengoai.apollo.math.linalg.NDArray})
 * representation.
 * </p>
 * <p>
 * Encoders need to learn its mapping during training through calls to the {@link #fit(MStream)} method which processes
 * a stream of Observation learning to how encode its name space. However, an encoder can be <code>fixed</code> meaning
 * that the mapping vocabulary is static, i.e. known beforehand. This is the case when using pre-trained embeddings,
 * like Glove and Word2Vec.
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Encoder extends Serializable {

   /**
    * Decodes the value (index) into its associated variable name
    *
    * @param index the encoded index
    * @return the decoded variable name or null if the index is invalid
    */
   String decode(double index);

   /**
    * Encodes the given variable name into an integer index.
    *
    * @param variableName the variable name
    * @return the index of the variable or -1 if its out-of-vocabulary
    */
   int encode(String variableName);

   /**
    * Learns the mapping between variables in the observations to indexes.
    *
    * @param observations the stream of observations to learn a mapping over
    */
   void fit(@NonNull MStream<Observation> observations);

   /**
    * Gets all known encodable strings, i.e. the alphabet
    *
    * @return the alphabet
    */
   @JsonIgnore
   Set<String> getAlphabet();

   /**
    * Retruns true when this encoder has a fixed, i.e. static and non-changing, vocabulary.
    *
    * @return True if the encoder has a fixed vocabulary
    */
   @JsonIgnore
   boolean isFixed();

   /**
    * The largest index assigned by this encoder. For example, an {@link IndexEncoder} that has an alphabet of
    * <code>A, B, C, D</code> will return a size of 4 and a {@link HashingEncoder} with 10 set for the number of
    * features will return 10.
    *
    * @return the largest index assigned by this encoder
    */
   int size();

}//END OF Encoder
