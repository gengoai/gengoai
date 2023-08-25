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
import com.gengoai.apollo.math.measure.ContingencyTable;
import com.gengoai.apollo.math.measure.ContingencyTableCalculator;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.collection.counter.MultiCounters;
import com.gengoai.hermes.AttributeType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.io.CSV;
import com.gengoai.io.CSVWriter;
import com.gengoai.stream.Streams;
import lombok.Data;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.stream.Stream;

@Data
@MetaInfServices
public class AttributeAssociationCalc implements Action {
    private String id;
    private String extractor = "filter(lemma(@TOKEN), isContentWord)";
    private String attribute = Types.LABEL.toString();
    private boolean distinct = false;
    private double minCount = 10;
    private String calculator = "G_SCORE";

    @Override
    public String getName() {
        return "ASSOCIATION";
    }

    @Override
    public String getDescription() {
        return "Calculates the association between two sets of terms.";
    }

    private ContingencyTableCalculator getCalculator() {
        switch (calculator.toUpperCase()) {
            case "G_SCORE":
                return Association.G_SQUARE;
            case "X2":
                return Association.CHI_SQUARE;
            case "MIKOLOV":
                return Association.Mikolov;
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
        final Counter<String> attributeCounter = Counters.newConcurrentCounter();
        final Counter<String> termCounter = Counters.newConcurrentCounter();
        final MultiCounter<String, String> cooccurrence = MultiCounters.newConcurrentMultiCounter();
        final AttributeType<String> attributeType = AttributeType.valueOf(attribute);
        final LyreExpression lyreExpression = LyreExpression.parse(extractor);

        corpus.parallelStream()
              .forEach(d -> {
                  String LABEL = d.attribute(attributeType);
                  attributeCounter.increment(LABEL);
                  Stream<String> stream = Streams.asStream(lyreExpression.extract(d).string());
                  if (distinct) {
                      stream = stream.distinct();
                  }
                  stream.forEach(term -> {
                      cooccurrence.increment(LABEL, term);
                      termCounter.increment(term);
                  });
              });

        final double totalItems = attributeCounter.sum();
        final double totalTerms = cooccurrence.sum();
        final ContingencyTableCalculator calculator = getCalculator();
        try (CSVWriter writer = CSV.csv().writer(context.getAnalysisFolder().getChild(getId() + "-association.csv"))) {
            writer.write(List.of("Attribute", "Term", calculator, "P-Value"));
            for (String attr : cooccurrence.firstKeys()) {
                Counter<String> terms = cooccurrence.get(attr);
                for (String term : terms.itemsByCount(false)) {
                    if (terms.get(term) < minCount) {
                        break;
                    }
                    double termCount = termCounter.get(term);
                    ContingencyTable table = ContingencyTable.create2X2(terms.get(term),
                                                                        totalItems,
                                                                        termCount,
                                                                        totalTerms);
                    double score = calculator.calculate(table);
                    double p = calculator.pValue(table);
                    writer.write(List.of(
                            attr,
                            term,
                            score,
                            p));
                }
            }
        }

        return corpus;
    }
}//END OF AssociationCalc
