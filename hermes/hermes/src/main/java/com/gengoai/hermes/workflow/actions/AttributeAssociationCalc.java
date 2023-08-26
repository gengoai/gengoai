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
    private String metric = "G_SCORE";
    private String extractor = "filter(lemma(@TOKEN), isContentWord)";
    private String attribute = Types.LABEL.toString();
    private boolean distinct = false;
    private double minCount = 10;

    @Override
    public String getName() {
        return "ASSOCIATION";
    }

    @Override
    public String getDescription() {
        return "Calculates the association between two sets of terms.";
    }


    @Override
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        saveActionState(context.getActionsFolder());

        final Counter<String> attributeCounter = Counters.newConcurrentCounter();
        final Counter<String> termCounter = Counters.newConcurrentCounter();
        final MultiCounter<String, String> cooccurrence = MultiCounters.newConcurrentMultiCounter();
        final AttributeType<?> attributeType = AttributeType.valueOf(attribute);
        final LyreExpression lyreExpression = LyreExpression.parse(extractor);

        corpus.parallelStream()
              .forEach(d -> {
                  String LABEL = d.attribute(attributeType).toString();
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
        final ContingencyTableCalculator tableCalculator = Association.valueOf(metric.toUpperCase());
        try (CSVWriter writer = CSV.csv().writer(context.getAnalysisFolder().getChild(getId() + "-association.csv"))) {
            writer.write(List.of(attribute,
                                 "Term",
                                 "cooccurrence",
                                 "Total Term Count",
                                 "Total Attribute Count",
                                 metric,
                                 "P-Value"));
            for (String attr : cooccurrence.firstKeys()) {
                Counter<String> attrTerms = cooccurrence.get(attr);
                double attrCount = attributeCounter.get(attr);

                for (String term : attrTerms.itemsByCount(false)) {
                    if (attrTerms.get(term) < minCount) {
                        break;
                    }
                    double n11 = cooccurrence.get(attr, term);
                    double termCount = termCounter.get(term);
                    ContingencyTable table = ContingencyTable.create2X2(n11,
                                                                        attrCount,
                                                                        termCount,
                                                                        totalTerms);
                    double score = tableCalculator.calculate(table);
                    double p = -1;
                    try {
                        p = tableCalculator.pValue(table);
                    } catch (UnsupportedOperationException e) {
                        //ignore as not all calculators support p-value
                    }
                    writer.write(List.of(
                            attr,
                            term,
                            n11,
                            termCount,
                            attrCount,
                            score,
                            p));
                }
            }
        }

        return corpus;
    }
}//END OF AssociationCalc
