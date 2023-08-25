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
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extractor;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVWriter;
import lombok.Data;
import org.kohsuke.MetaInfServices;

import java.util.List;

/**
 * The type Term extraction processor.
 *
 * @author David B. Bracewell
 */
@MetaInfServices
@Data
public class TermCounts implements Action {
    /**
     * The constant EXTRACTED_TERMS.
     */
    public static final String EXTRACTED_TERMS = "termCounts.extractedTerms";
    private static final long serialVersionUID = 1L;
    private boolean documentFrequencies = false;
    private String extractor = "filter(lemma(@TOKEN), isContentWord)";
    private String id;
    private int minCount = 1;

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
    public String getName() {
        return "TERM_COUNTS";
    }

    @Override
    public String getDescription() {
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
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        saveActionState(context.getActionsFolder());

        Extractor ex = LyreExpression.parse(extractor);
        Counter<String> counts;
        if (documentFrequencies) {
            counts = corpus.documentCount(ex);
        } else {
            counts = corpus.termCount(ex);
        }

        counts = counts.filterByValue(d -> d >= minCount);
        context.property(EXTRACTED_TERMS, counts);

        try (CSVWriter writer = CSV.csv().writer(context.getAnalysisFolder().getChild(getId() + "-termCounts.csv"))) {
            writer.write(List.of("Term", "Count"));
            for (String term : counts.itemsByCount(false)) {
                writer.write(List.of(term, counts.get(term)));
            }
        }

        return onComplete(corpus, context, counts);
    }

}//END OF TermCounts
