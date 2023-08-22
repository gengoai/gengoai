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

import com.gengoai.LogUtils;
import com.gengoai.apollo.math.measure.Similarity;
import com.gengoai.collection.Lists;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.extraction.Extraction;
import com.gengoai.hermes.extraction.summarization.Summarizer;
import com.gengoai.hermes.extraction.summarization.TextRankSummarizer;
import com.gengoai.hermes.ml.model.huggingface.Summarization;
import com.gengoai.hermes.similarity.ExtractorBasedSimilarity;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import lombok.Data;
import lombok.extern.java.Log;
import org.kohsuke.MetaInfServices;

/**
 * @author David B. Bracewell
 */
@Data
@MetaInfServices
@Log
public class Summarize implements Action {
    private static final long serialVersionUID = 1L;
    private String algorithm;
    private int maxLength = 250;
    private int device = -1;


    private Summarizer getSummarizer(String name) {
        switch (name.toLowerCase()) {
            case "text-rank":
                TextRankSummarizer summarizer = new TextRankSummarizer(new ExtractorBasedSimilarity(Similarity.Cosine));
                summarizer.setNumberOfSentences(maxLength);
                return summarizer;
            case "t5":
                return new Summarization(Summarization.FLAN_T5_BASE_SAMSUM, device, maxLength);
            default:
                throw new IllegalArgumentException(String.format("Unknown summarization algorithm: '%s'", name));
        }
    }

    @Override
    public String getName() {
        return "SUMMARIZE";
    }

    @Override
    public String getDescription() {
        return "Summarizes a text";
    }

    @Override
    public DocumentCollection process(DocumentCollection corpus, Context context) throws Exception {
        Summarizer summarizer = getSummarizer(algorithm);
        summarizer.fit(corpus);
        if (summarizer instanceof Summarization && corpus instanceof Corpus) {
            long i = 0;
            for (Document document : corpus) {
                Extraction summary = summarizer.extract(document);
                document.put(Types.SUMMARY, Lists.asArrayList(summary.string()));
                ((Corpus) corpus).update(document);
                i++;
                if (i % 100 == 0) {
                    LogUtils.logInfo(log, "Processed summaries for {0} documents", i);
                }
            }
            LogUtils.logInfo(log, "Processed summaries for {0} documents", i);
            return corpus;
        } else if (summarizer instanceof Summarization) {
            return DocumentCollection.create(corpus.stream().javaStream().sequential().map(doc -> {
                Extraction summary = summarizer.extract(doc);
                doc.put(Types.SUMMARY, Lists.asArrayList(summary.string()));
                return doc;
            }));
        }
        return corpus.update("Summarize", doc -> {
            Extraction summary = summarizer.extract(doc);
            doc.put(Types.SUMMARY, Lists.asArrayList(summary.string()));
        });
    }

}//END OF Summarize
