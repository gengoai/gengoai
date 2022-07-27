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

import com.gengoai.apollo.math.measure.Similarity;
import com.gengoai.collection.Lists;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.summarization.Summarizer;
import com.gengoai.hermes.extraction.summarization.TextRankSummarizer;
import com.gengoai.hermes.similarity.ExtractorBasedSimilarity;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.ActionDescription;
import com.gengoai.hermes.workflow.Context;
import lombok.Data;
import org.kohsuke.MetaInfServices;

/**
 * @author David B. Bracewell
 */
@Data
public class Summarize implements Action {
   private static final long serialVersionUID = 1L;
   private Summarizer summarizer = new TextRankSummarizer(
         new ExtractorBasedSimilarity(Similarity.Cosine)
   );


   @Override
   public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
      summarizer.fit(corpus);
      return corpus.update("KeywordExtraction", doc -> {
         Extraction summary = summarizer.extract(doc);
         doc.put(Types.SUMMARY, Lists.asArrayList(summary.string()));
      });
   }

   @MetaInfServices
   public static class SummarizeDescription implements ActionDescription {
      @Override
      public String description() {
         return "Extracts keywords from documents storing them both on the document using the 'KEYWORD' attribute and on the context using 'KEYWORDS'. " +
               "Note that keywords are only kept on the context if the property 'keepGlobalCounts' is true (it is 'false' by default). " +
               "Additionally, you can specify the maximum number of keywords per document by setting the parameter 'n'. " +
               "The keyword extraction algorithm can be set via JSON using either 'algorithm' or 'extractor' as follows:" +
               "\n\nVia Workflow Json:\n" +
               "--------------------------------------\n" +
               "{\n" +
               "   \"@type\"=\"" + KeywordExtraction.class.getName() + "\",\n" +
               "   \"algorithm\"=\"tfidf\"|\"tf\"|\"rake\"|\"np\"|\"text-rank\"\n," +
               "   \"extactor\"={EXTRACTOR DEFINITION}\n," +
               "   \"n\"=NUMBER\n," +
               "   \"keepGlobalCounts\"=true|false\n" +
               "}";
      }

      @Override
      public String name() {
         return Summarizer.class.getName();
      }
   }
}//END OF Summarize
