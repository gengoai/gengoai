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

package com.gengoai.news;

import com.gengoai.LogUtils;
import com.gengoai.application.Option;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HermesCLI;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.io.resource.Resource;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.LuceneIndex;
import com.gengoai.lucene.field.Fields;
import com.gengoai.string.Strings;
import lombok.extern.java.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Log
public class CorpusCreator extends HermesCLI {
    @Option(description = "The location of the db to store rss and html")
    private Resource db;

    @Option(description = "The location of the corpus")
    private String corpus;

    public static void main(String[] args) {
        new CorpusCreator().run(args);
    }

    @Override
    protected void programLogic() throws Exception {
        LuceneIndex html = LuceneIndex.at(db)
                                      .storedField("url", Fields.KEYWORD)
                                      .storedField("html", Fields.BLOB)
                                      .readOnly(true)
                                      .open();
        LogUtils.logInfo(log, "Creating corpus at {0} with {1} documents", corpus, html.size());
        List<Document> buffer = new ArrayList<>();
        Corpus c = Corpus.open(corpus);
        for (IndexDocument indexDocument : html) {
            if (indexDocument.hasField("html")) {
                NewsArticle article = null;
                try {
                    String url = Strings.prependIfNotPresent(indexDocument.get("url").asString(), "https://");
                    article = NewsArticle.fromHTML(new URL(url), indexDocument.getBlobField("html"));
                    var doc = article.toDocument();
                    if (doc != null) {
                        buffer.add(doc);
                    } else {
                        LogUtils.logFine(log, "Error converting {0}", url);
                    }
                    if (buffer.size() >= 100) {
                        c.addAll(buffer);
                        buffer.clear();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        c.addAll(buffer);
        c.annotate(Types.SENTENCE, Types.ENTITY);
        c.close();
    }
}
