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

package com.gengoai.hermes.workflow.actions;

import com.gengoai.collection.counter.Counter;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.hermes.extraction.TermExtractor;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.ActionDescription;
import com.gengoai.hermes.workflow.Context;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.kohsuke.MetaInfServices;

import java.io.Serial;

/**
 * The type Term extraction processor.
 *
 * @author David B. Bracewell
 */
public class TermCounts implements Action {
   /**
    * The constant EXTRACTED_TERMS.
    */
   public static final String EXTRACTED_TERMS = "EXTRACTED_TERMS";
   @Serial
   private static final long serialVersionUID = 1L;
   @Getter
   @Setter
   private boolean documentFrequencies = false;
   @Getter
   private Extractor extractor = TermExtractor.builder().build();


   public static Counter<String> getTermCounts(@NonNull Context context) {
      return Cast.as(context.get(EXTRACTED_TERMS));
   }

   /**
    * On complete corpus.
    *
    * @param corpus  the corpus
    * @param context the context
    * @param counts  the counts
    * @return the corpus
    */
   protected DocumentCollection onComplete(DocumentCollection corpus, Context context, Counter<String> counts) {
      return corpus;
   }

   @Override
   public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
      Counter<String> counts;
      if (documentFrequencies) {
         counts = corpus.documentCount(extractor);
      } else {
         counts = corpus.termCount(extractor);
      }
      context.property(EXTRACTED_TERMS, counts);
      return onComplete(corpus, context, counts);
   }

   /**
    * Sets extractor.
    *
    * @param extractor the extractor
    */
   public void setExtractor(@NonNull Extractor extractor) {
      this.extractor = extractor;
   }

   @Override
   public String toString() {
      return "TermExtractionProcessor{" +
            "extractor=" + extractor +
            ", documentFrequencies=" + documentFrequencies +
            '}';
   }

   @MetaInfServices
   public static class TermCountsDescription implements ActionDescription {
      @Override
      public String description() {
         return "Calculates the term or document frequencies over corpus. " +
               "The extracted terms are stored on the context using the property 'EXTRACTED_TERMS'." +
               "The parameters of the action are defined in the Workflow Json as follows:\n" +
               "{\n" +
               "   \"@type\"=\"" + TermCounts.class.getName() + "\",\n" +
               "   \"extractor\"={EXTRACTOR DEFINITION},\n" +
               "   \"documentFrequencies\"=true|false\n" +
               "}\n" +
               "where you may specify either 'extractor' or 'lyrePattern'. If neither are specified a default TermExtractor is used.";
      }

      @Override
      public String name() {
         return TermCounts.class.getName();
      }

   }
}//END OF TermCounts
