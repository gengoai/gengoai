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

package com.gengoai.apollo.model.tensorflow;

import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.apollo.model.embedding.MLVectorStore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collections;

import static com.gengoai.apollo.encoder.IndexEncoder.indexEncoder;

/**
 * <P>A TFVar for input variables.</P>
 *
 * @author David B. Bracewell
 */
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TFInputVar extends TFVar {
    private static final long serialVersionUID = 1L;

    protected TFInputVar(@NonNull String name,
                         @NonNull String servingName,
                         @NonNull Encoder encoder,
                         @NonNull int... shape) {
        super(name, servingName, encoder, shape);
    }


    /**
     * <p>Constructs a TFInputVar for a sequence where the serving name is the same as the observation name with the
     * given shape. The input var will create a new {@link IndexEncoder} with an unknown word of <code>--UNKNOWN</code>
     * and a special word <code>--PAD--</code> for padding. Note that <code>-1</code> in shape means that it will expand
     * to the longest in a given batch.</p>
     *
     * @param name  the name of the variable
     * @param shape the shape of the variable
     * @return the TFInputVar
     */
    public static TFInputVar sequence(String name, int... shape) {
        return new TFInputVar(name, name, indexEncoder("--UNKNOWN--", Collections.singletonList("--PAD--")), shape);
    }

    public static TFInputVar sequence(String name, String servingName, int... shape) {
        return new TFInputVar(name, servingName, indexEncoder("--UNKNOWN--", Collections.singletonList("--PAD--")), shape);
    }

    public static TFOneHotInputVar oneHotEncoding(String name, int... shape) {
        return new TFOneHotInputVar(name, shape);
    }

    public static TFOneHotInputVar oneHotEncoding(String name, String servingName, int... shape) {
        return new TFOneHotInputVar(name, servingName, shape);
    }

    public static TFEmbeddingInputVar embedding(String name, MLVectorStore vectors) {
        return new TFEmbeddingInputVar(name, name, vectors);
    }

    public static TFEmbeddingInputVar embedding(String name, String servingName, MLVectorStore vectors) {
        return new TFEmbeddingInputVar(name, servingName, vectors);
    }

    /**
     * <p>Constructs a TFInputVar for a sequence where the serving name is the same as the observation name with the
     * given shape and encoder. Note that <code>-1</code> in shape means that it will expand to the longest in a given
     * batch.</p>
     *
     * @param name    the name of the variable
     * @param encoder the encoder to use
     * @param shape   the shape of the variable
     * @return the TFInputVar
     */
    public static TFInputVar sequence(String name, Encoder encoder, int... shape) {
        return new TFInputVar(name, name, encoder, shape);
    }

    /**
     * <p>Constructs a TFInputVar for a sequence where the serving name is the same as the observation name with the
     * given encoder and assumes variable length sequences. </p>
     *
     * @param name    the name of the variable
     * @param encoder the encoder to use
     * @return the TFInputVar
     */
    public static TFInputVar sequence(String name, Encoder encoder) {
        return new TFInputVar(name, name, encoder, -1);
    }

    public static TFInputVar sequence(String name, String servingName, Encoder encoder) {
        return new TFInputVar(name, servingName, encoder, -1);
    }

    /**
     * <p>Constructs a TFInputVar for a sequence where the serving name is the same as the observation name. The input
     * var will create a new {@link IndexEncoder} with an unknown word of <code>--UNKNOWN</code> and a special word
     * <code>--PAD--</code> for padding and assumes variable length sequences. </p>
     *
     * @param name the name of the variable
     * @return the TFInputVar
     */
    public static TFInputVar sequence(String name) {
        return new TFInputVar(name, name, indexEncoder("--UNKNOWN--", Collections.singletonList("--PAD--")), -1);
    }

    /**
     * <p>Static creation of TFInputVar.</p>
     *
     * @param name        the name
     * @param servingName the serving name
     * @param encoder     the encoder
     * @param shape       the shape
     * @return the tf input var
     */
    public static TFInputVar var(String name, String servingName, Encoder encoder, int... shape) {
        return new TFInputVar(name, servingName, encoder, shape);
    }


}//END OF TFInputVar
