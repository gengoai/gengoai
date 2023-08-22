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

package com.gengoai.apollo.model.embedding;

import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>
 * An {@link Encoder} in which the encoded index has an {@link NDArray} associated with it for embedding the encoded
 * String into a vector. Each vector store can have an "unknown key" which when defined will be used in place of keys
 * that are not found in the alphabet. Additionally, a set of "special keys" are defined that will always be added to
 * the vector store as an array of all zero values.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface KeyedVectorStore extends Serializable {

    /**
     * The dimension of the vectors in the store
     *
     * @return the dimension of the vectors in the store
     */
    int dimension();

    /**
     * @return the array of special tokens
     */
    String[] getSpecialKeys();

    /**
     * The key representing that is used when other keys are not able to be encoded.
     *
     * @return the unknown key
     */
    String getUnknownKey();

    /**
     * Gets the vector for the key. When the key is not in the store, it will backoff to the unknown key or an NDArray of
     * all zero values.
     *
     * @param key the key
     * @return the vector
     */
    NumericNDArray getVector(@NonNull String key);

    /**
     * Gets a stream over the vectors in the store.
     *
     * @return the stream of vectors.
     */
    Stream<NumericNDArray> stream();

    /**
     * Updates the vector at the given index.
     *
     * @param word   the word associated with the vector
     * @param vector the new vector
     */
    void updateVector(String word, @NonNull NumericNDArray vector);

    int size();

    Set<String> getAlphabet();

    default void writeWord2VecFormat(@NonNull Resource output) throws IOException {
        try (Writer writer = output.writer()) {
            writer.write(Integer.toString(size()));
            writer.write(" ");
            writer.write(Integer.toString(dimension()));
            writer.write("\n");
            for (String word : getAlphabet()) {
                writer.write(word.replace(' ', '_'));
                writer.write(" ");
                writer.write(VSTextUtils.vectorToLine(getVector(word)));
                writer.write("\n");
            }
        }
    }


}//END OF KeyedVectorStore
