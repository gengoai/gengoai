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
import com.gengoai.collection.disk.DiskMap;
import com.gengoai.io.Resources;
import com.gengoai.io.Xml;
import com.gengoai.io.resource.Resource;
import lombok.extern.java.Log;
import org.w3c.dom.Document;

import java.util.List;
import java.util.TimerTask;

@Log
public class RSSMonitor extends TimerTask {

    private final List<Resource> rssFeeds;
    private final DiskMap<String, RSSItem> rssItemMap;
    private final DiskMap<String, String> htmlMap;

    public RSSMonitor(Iterable<Resource> rssFeeds,
                      Resource database) {
        this.rssFeeds = Lists.asArrayList(rssFeeds);
        this.rssItemMap = DiskMap.<String, RSSItem>builder()
                                 .compressed(true)
                                 .namespace("rss")
                                 .file(database)
                                 .build();
        this.htmlMap = DiskMap.<String, String>builder()
                              .compressed(true)
                              .namespace("html")
                              .file(database)
                              .build();
    }

    @Override
    public void run() {
        for (Resource url : rssFeeds) {
            try {
                for (Document item : Xml.parse(url, "item")) {
                    RSSItem rssItem = RSSItem.from(item);
                    String guid = rssItem.getGuid();
                    if (!rssItemMap.containsKey(guid)) {
                        rssItemMap.put(guid, rssItem);
                        LogUtils.logInfo(log, "Adding {0}", guid);
                        rssItemMap.commit();
                    }
                    if (!htmlMap.containsKey(guid)) {
                        LogUtils.logInfo(log, "Retrieving html for {0}", guid);
                        htmlMap.put(guid, Resources.from(rssItem.getLink()).readToString());
                        htmlMap.commit();
                    }
                }
            } catch (Exception e) {
                LogUtils.logSevere(log, "Error processing {0}", url);
                e.printStackTrace();
            }
        }
    }
}
