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

package com.gengoai.hermes.tools;

import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.SQLiteDataSet;
import com.gengoai.apollo.data.transform.Transformer;
import com.gengoai.apollo.data.transform.vectorizer.EmbeddingVectorizer;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.en.ENResources;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.io.Resources;

import java.util.concurrent.atomic.AtomicLong;

import static com.gengoai.apollo.feature.Featurizer.valueFeaturizer;

public class CreateWikiCorpus extends HermesCLI {
    public static final String TOKENS = "words";

    @Override
    protected void programLogic() throws Exception {
        Corpus wiki = Corpus.open("/shared/OneDrive/wikipedia");
        HStringDataSetGenerator generator = HStringDataSetGenerator.builder(Types.SENTENCE)
                                                                   .tokenSequence(TOKENS, valueFeaturizer(HString::toString))
                                                                   .build();
        EmbeddingVectorizer vectorizer = new EmbeddingVectorizer(ENResources.gloveLargeEmbeddings());
        vectorizer.source(TOKENS);
        Transformer transformer = new Transformer(
                vectorizer
        );

        Resources.from("/home/ik/prj/gengoai/python/tensorflow/data/wiki.db").delete();
        AtomicLong counter = new AtomicLong();
        SQLiteDataSet dataSet = new SQLiteDataSet(
                Resources.from("/home/ik/prj/gengoai/python/tensorflow/data/wiki.db"),
                wiki.stream().flatMap(d -> {
                    return d.sentences().stream().map(a -> {
                        Datum datum = generator.apply(a);
                        datum = transformer.transform(datum);
                        System.out.println(counter.incrementAndGet());
                        return datum;
                    });
                }).limit(500_000).javaStream()
        );
        dataSet.probe();
    }

    public static void main(String[] args) {
        new CreateWikiCorpus().run(args);
    }

}
