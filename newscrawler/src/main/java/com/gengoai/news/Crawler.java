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

import com.gengoai.application.Option;
import com.gengoai.hermes.tools.HermesCLI;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.rss.RSSMonitor;
import com.gengoai.string.Strings;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Crawler extends HermesCLI {

    @Option(description = "The location of the db to store rss and html")
    private Resource db;

    @Option(description = "The location of the text file containing the feeds to monitor")
    private Resource feeds;

    @Override
    protected void programLogic() throws Exception {
        List<Resource> rssURLS = feeds.lines()
                .filter(l -> Strings.isNotNullOrBlank(l) && !l.strip().startsWith("#"))
                .map(l -> Resources.from(l.strip()))
                .collect();
        RSSMonitor monitor = new RSSMonitor(rssURLS, db);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(monitor, 0, TimeUnit.MINUTES.toMillis(15));
    }

    public static void main(String[] args) {
        new Crawler().run(args);
    }

}
