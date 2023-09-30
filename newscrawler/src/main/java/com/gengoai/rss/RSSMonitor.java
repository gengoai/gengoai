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

package com.gengoai.rss;

import com.gengoai.LogUtils;
import com.gengoai.collection.Lists;
import com.gengoai.concurrent.Threads;
import com.gengoai.io.Resources;
import com.gengoai.io.Xml;
import com.gengoai.io.resource.Resource;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.LuceneIndex;
import com.gengoai.lucene.field.Fields;
import lombok.extern.java.Log;
import org.apache.lucene.index.Term;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Log
public class RSSMonitor extends TimerTask {

    private final List<Resource> rssFeeds;
    private final LuceneIndex index;

    public RSSMonitor(Iterable<Resource> rssFeeds,
                      Resource database) {
        this.rssFeeds = Lists.asArrayList(rssFeeds);
        try {
            this.index = LuceneIndex.at(database)
                                    .storedField("url", Fields.KEYWORD)
                                    .storedField("html", Fields.BLOB)
                                    .createIfNotExists();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        for (Resource url : rssFeeds) {
            try {
                LogUtils.logInfo(log, "Processing: {0}", url);
                for (Document item : Xml.parse(url, "item")) {
                    RSSItem rssItem = RSSItem.from(item);
                    try {
                        if (!index.get("url", rssItem.getLink()).hasField("html")) {
                            LogUtils.logInfo(log, "Fetching: {0}", rssItem.getLink());
                            IndexDocument indexDocument = IndexDocument.from(Map.of("url", rssItem.getLink(),
                                                                                    "html", Resources.from(rssItem.getLink()).readToString()));
                            index.updateWhere(new Term("url", rssItem.getLink()), List.of(indexDocument));
                            index.commit();
                            Threads.sleep(3, TimeUnit.SECONDS);
                        }
                    } catch (Exception e) {
                        LogUtils.logSevere(log, "Error processing {0}", rssItem.getLink());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                LogUtils.logSevere(log, "Error processing {0}", url);
                e.printStackTrace();
            }
        }
    }
}
