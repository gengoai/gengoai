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

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.topic.MalletLDA;
import com.gengoai.apollo.model.topic.Topic;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import lombok.Data;
import org.kohsuke.MetaInfServices;

import java.util.ArrayList;
import java.util.List;

@Data
@MetaInfServices
public class TopicModelling implements Action {
    public static final String TOPIC_MODELLING_MODEL = "topic.model";
    public static final String TOPIC_MODELLING_TOPIC_INFO = "topic.topicInfo";
    private String extractor = "filter(lemma(@TOKEN), isContentWord)";
    private int numTopics = 50;
    private String id;

    @Override
    public String getName() {
        return "TOPIC";
    }

    @Override
    public String getDescription() {
        return "Builds a topic model over the corpus";
    }

    @Override
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        saveActionState(context.getActionsFolder());

        final Resource saveLocation = context.getActionsFolder().getChild(getId());
        HStringDataSetGenerator generator = HStringDataSetGenerator.builder()
                                                                   .tokenSequence(Datum.DEFAULT_INPUT, LyreExpression.parse(extractor))
                                                                   .build();
        MalletLDA lda = new MalletLDA(p -> {
            p.K.set(numTopics);
            p.combineOutputs.set(true);
            p.verbose.set(true);
        });

        DataSet dataset = corpus.asDataSet(generator);
        lda.estimate(dataset);

        ModelIO.save(lda, saveLocation);

        corpus.update("TopicModelling", doc -> {
            Datum datum = generator.apply(doc);
            datum = lda.transform(datum);
            doc.put(Types.TOPIC_DISTRIBUTION, datum.getDefaultOutput().asNumericNDArray());
        });

        List<List<String>> topics = new ArrayList<>();
        for (int i = 0; i < lda.getNumberOfTopics(); i++) {
            Topic topic = lda.getTopic(i);
            topics.add(topic.getFeatureDistribution().topN(10).itemsByCount(false));
        }
        context.property(TOPIC_MODELLING_TOPIC_INFO, topics);
        context.property(TOPIC_MODELLING_MODEL, lda);
        context.getAnalysisFolder().getChild(getId() + "-topics.json").write(Json.dumpsPretty(topics));
        return corpus;
    }
}
