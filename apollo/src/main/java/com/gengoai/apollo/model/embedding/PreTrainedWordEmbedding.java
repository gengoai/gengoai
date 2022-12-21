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

import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.transform.SingleSourceTransform;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A pre-trained {@link WordEmbedding} such as the available Word2Vec and Glove Embeddings.
 * </p>
 *
 * @author David B. Bracewell
 */
public class PreTrainedWordEmbedding extends WordEmbedding implements SingleSourceTransform {
   private String input = Datum.DEFAULT_INPUT;
   private String output = Datum.DEFAULT_OUTPUT;

   /**
    * Loads a pre-trained embedding in Word2Vec text format from the given resource.
    *
    * @param resource    the resource to read from
    * @param unknownWord the string to use to represent an unknown word.
    * @return the PreTrainedWordEmbedding
    * @throws IOException Something went wrong reading the file
    */
   public static PreTrainedWordEmbedding readWord2VecTextFormat(Resource resource,
                                                                String unknownWord) throws IOException {
      PreTrainedWordEmbedding e = new PreTrainedWordEmbedding();
      e.vectorStore = new InMemoryVectorStore(VSTextUtils.determineDimension(resource), unknownWord, null);
      List<String> lines = resource.readLines();
      lines = lines.subList(1, lines.size());
      lines.parallelStream().forEach(line -> {
         if (Strings.isNotNullOrBlank(line) && !line.startsWith("#")) {
            NumericNDArray v = VSTextUtils.convertLineToVector(line, e.dimension());
            synchronized (e) {
               e.vectorStore.updateVector(v.getLabel(), v);
            }
         }
      });
      return e;
   }

   /**
    * Loads a pre-trained embedding in Word2Vec text format from the given resource. Will check for an unknown word
    * specified on the first line followed by a hash, e.g. <code>#UNKNOWN</code>
    *
    * @param resource the resource to read from
    * @return the PreTrainedWordEmbedding
    * @throws IOException Something went wrong reading the file
    */
   public static PreTrainedWordEmbedding readWord2VecTextFormat(@NonNull Resource resource) throws IOException {
      return readWord2VecTextFormat(resource, VSTextUtils.determineUnknownWord(resource));
   }

   @Override
   public Set<String> getInputs() {
      return Collections.singleton(input);
   }

   @Override
   public Set<String> getOutputs() {
      return Collections.singleton(output);
   }

   @Override
   public PreTrainedWordEmbedding input(@NonNull String name) {
      this.input = name;
      return this;
   }

   @Override
   public PreTrainedWordEmbedding output(@NonNull String name) {
      this.output = name;
      return this;
   }

   @Override
   public PreTrainedWordEmbedding source(@NonNull String name) {
      this.input = name;
      this.output = name;
      return this;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      datum.put(this.output, transform(datum.get(this.input)));
      return datum;
   }

}//END OF PreTrainedWordEmbedding
