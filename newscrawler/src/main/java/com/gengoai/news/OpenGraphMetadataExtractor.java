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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class OpenGraphMetadataExtractor {

    private OpenGraphMetadataExtractor() {
        throw new IllegalAccessError();
    }


    public static NewsArticle extract(Document jsoup) {
        NewsArticle article = new NewsArticle();

        //Extract Headline
        Element e = jsoup.select("meta[property='og:title']").first();
        if (e != null) {
            article.setHeadline(e.attr("content"));
        }

        //Extract URL
        e = jsoup.select("meta[property='og:url']").first();
        if (e != null) {
            article.setUrl(e.attr("content"));
        }

        //Extract description
        e = jsoup.select("meta[property='og:description']").first();
        if (e != null) {
            article.setDescription(e.attr("content"));
        }

        //Extract Publisher
        e = jsoup.select("meta[property='og:site_name']").first();
        if (e != null) {
            article.setPublisher(e.attr("content"));
        }

        //Extract Pubdate
        e = jsoup.select("meta[property='og:pubdate']").first();
        if (e != null) {
            article.setPubDate(e.attr("content"));
        }

        //Extract Locale
        e = jsoup.select("meta[property='og:locale']").first();
        if (e != null) {
            article.setLocale(e.attr("content"));
        }

        return article;
    }


}
