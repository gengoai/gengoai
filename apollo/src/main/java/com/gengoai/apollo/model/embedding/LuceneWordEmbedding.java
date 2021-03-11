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
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

public class LuceneWordEmbedding extends WordEmbedding implements SingleSourceTransform {
   private String input = Datum.DEFAULT_INPUT;
   private String output = Datum.DEFAULT_OUTPUT;

   @Override
   public Set<String> getInputs() {
      return Collections.singleton(input);
   }

   @Override
   public Set<String> getOutputs() {
      return Collections.singleton(output);
   }

   @Override
   public LuceneWordEmbedding input(@NonNull String name) {
      this.input = name;
      return this;
   }

   @Override
   public LuceneWordEmbedding output(@NonNull String name) {
      this.output = name;
      return this;
   }

   @Override
   public LuceneWordEmbedding source(@NonNull String name) {
      this.input = name;
      this.output = name;
      return this;
   }
}//END OF LuceneWordEmbedding
