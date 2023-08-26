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

import com.gengoai.apollo.math.measure.Association;
import com.gengoai.apollo.math.measure.ContingencyTableCalculator;
import com.gengoai.collection.counter.Counter;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.NGramExtractor;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVWriter;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple;
import lombok.Data;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.List;

@Data
@MetaInfServices
public class SignificantBigrams implements Action {
    private String id;
    private String trim = "isStopWord";
    private String filter = "hasStopWord";
    private boolean lemma = true;
    private int minCount = 10;
    private double minScore = 0;
    private String calculator = "G_SCORE";

    @Override
    public String getName() {
        return "BIGRAMS";
    }

    @Override
    public String getDescription() {
        return "Generate significant bigrams";
    }


    private ContingencyTableCalculator getTableCalculator() {
        switch (calculator.toUpperCase()) {
            case "G_SCORE":
                return Association.G_SQUARE;
            case "X2":
                return Association.CHI_SQUARE;
            case "MIKOLOV":
                return Association.MIKOLOV;
            case "NPMI":
                return Association.NPMI;
            case "PMI":
                return Association.PMI;
            case "PPMI":
                return Association.PPMI;
            case "MI":
                return Association.MI;
        }
        throw new IllegalArgumentException("Unknown Association: '" + calculator + "'");
    }

    @Override
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        saveActionState(context.getActionsFolder());
        NGramExtractor.Builder nGramExtractor = NGramExtractor.bigrams();
        if (Strings.isNotNullOrBlank(trim)) {
            nGramExtractor = nGramExtractor.trim(trim);
        }
        if (Strings.isNotNullOrBlank(filter)) {
            nGramExtractor = nGramExtractor.filter(filter);
        }
        if (lemma) {
            nGramExtractor = nGramExtractor.toLemma();
        }
        Counter<Tuple> cntr = corpus.significantBigrams(nGramExtractor.build(),
                                                        minCount,
                                                        minScore,
                                                        getTableCalculator());
        try (CSVWriter writer = CSV.csv().writer(context.getAnalysisFolder().getChild(getId() + "-bigrams.csv"))) {
            writer.write(List.of("Bigram", calculator));
            cntr.forEach((tuple, value) -> {
                String h1 = tuple.get(0);
                String h2 = tuple.get(1);
                try {
                    writer.write(List.of(h1 + "__" + h2, value));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return corpus;
    }
}
