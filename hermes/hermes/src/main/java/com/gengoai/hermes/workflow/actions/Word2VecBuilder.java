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
import com.gengoai.apollo.data.DataSetType;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.transform.MinCountFilter;
import com.gengoai.apollo.data.transform.Transform;
import com.gengoai.apollo.model.embedding.PreTrainedWordEmbedding;
import com.gengoai.apollo.model.embedding.Word2Vec;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.hermes.workflow.State;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import lombok.Data;
import org.kohsuke.MetaInfServices;

import java.io.IOException;

@Data
@MetaInfServices
public class Word2VecBuilder implements Action {
    public static final String WORD2VEC_SAVE_LOCATION = "word2vec.saveLocation";
    public static final String WORD2VEC_EMBEDDING = "word2vec.embedding";
    private int dimension = 100;
    private int minCount = 10;
    private String extractor = "filter(lemma(@TOKEN), isContentWord)";
    private String saveLocation = null;

    @Override
    public String getName() {
        return "WORD2VEC";
    }

    @Override
    public String getDescription() {
        return "Builds Word2Vec Embeddings for a corpus";
    }


    @Override
    public State loadPreviousState(DocumentCollection corpus, Context context) {
        if (context.getString(WORD2VEC_SAVE_LOCATION) != null) {
            Resource r = Resources.from(context.getString(WORD2VEC_SAVE_LOCATION));
            if (r.exists()) {
                try {
                    PreTrainedWordEmbedding embedding = PreTrainedWordEmbedding.readWord2VecTextFormat(r);
                    context.property(WORD2VEC_EMBEDDING, embedding);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return State.LOADED;
            }
        }
        return State.NOT_LOADED;
    }

    @Override
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        Word2Vec word2Vec = new Word2Vec(p -> {
            p.dimension.set(dimension);
            p.unknownWord.set("--UNKNOWN--");
            p.specialWords.set(new String[]{"--PAD--"});
        });
        LyreExpression lyreExpression = LyreExpression.parse(extractor);
        DataSet dataSet = corpus.asDataSet(HStringDataSetGenerator.builder()
                                                                  .dataSetType(DataSetType.Distributed)
                                                                  .tokenSequence(Datum.DEFAULT_INPUT, lyreExpression)
                                                                  .build());
        Transform transform = new MinCountFilter(minCount, "--UNKNOWN--");
        dataSet = transform.fitAndTransform(dataSet);
        word2Vec.estimate(dataSet);

        Resource outputLocation = null;
        if (!Strings.isNullOrBlank(context.getString(WORD2VEC_SAVE_LOCATION))) {
            outputLocation = Resources.from(context.getString(WORD2VEC_SAVE_LOCATION));
        } else if (saveLocation != null) {
            outputLocation = Resources.from(saveLocation);
            context.property(WORD2VEC_SAVE_LOCATION, saveLocation);
        }

        if (outputLocation != null) {
            word2Vec.writeWord2VecFormat(outputLocation);
        }

        context.property(WORD2VEC_EMBEDDING, word2Vec);
        return corpus;
    }
}
