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

package com.gengoai.lucene;

import lombok.NonNull;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;

import java.io.IOException;

/**
 * <p>Enumeration of methods for calculating the count of a term in a Lucene Index</p>
 *
 * @author David B. Bracewell
 */
public enum FrequencyType {
   /**
    * Calculates the total times the term appears in the index
    */
   TOTAL_TERM_FREQUENCY {
      @Override
      public long getFrequency(@NonNull TermsEnum termsEnum) throws IOException {
         return termsEnum.totalTermFreq();
      }

      @Override
      public long getFrequency(@NonNull Term term, @NonNull IndexReader indexReader) throws IOException {
         return indexReader.totalTermFreq(term);
      }
   },
   /**
    * Calculates the number of documents in which the term appears
    */
   DOCUMENT_FREQUENCY {
      @Override
      public long getFrequency(@NonNull TermsEnum termsEnum) throws IOException {
         return termsEnum.docFreq();
      }

      @Override
      public long getFrequency(@NonNull Term term, @NonNull IndexReader indexReader) throws IOException {
         return indexReader.docFreq(term);
      }
   };


   /**
    * Gets the frequency for a TermsEnum.
    *
    * @param termsEnum the terms enum
    * @return the frequency
    * @throws IOException Something went wrong reading from the index
    */
   public abstract long getFrequency(@NonNull TermsEnum termsEnum) throws IOException;

   /**
    * Gets the frequency for a Term using an IndexReader.
    *
    * @param term        the term
    * @param indexReader the index reader
    * @return the frequency
    * @throws IOException Something went wrong reading from the index
    */
   public abstract long getFrequency(@NonNull Term term, @NonNull IndexReader indexReader) throws IOException;

}//END OF FrequencyType
