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

package com.gengoai.news.extractor;

import com.gengoai.news.NewsArticle;
import com.gengoai.string.Strings;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ArticleExtractor {

    protected static void removeElements(Document doc, List<String> patterns) {
        for (String pattern : patterns) {
            Elements elements = doc.select(pattern);
            if (!elements.isEmpty()) {
                elements.remove();
            }
        }
    }

    protected static String findFirst(Document doc, List<String> patterns) {
        for (String pattern : patterns) {
            Elements elements = doc.select(pattern);
            if (!elements.isEmpty()) {
                return elements.stream()
                               .map(element -> {
                                   if (element.hasText()) {
                                       return element.text();
                                   } else {
                                       return element.attr("content");
                                   }
                               }).distinct().collect(Collectors.joining("\n"));
            }
        }
        return Strings.EMPTY;
    }


    public final NewsArticle extract(String html) {
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
        return extract(doc);
    }

    protected abstract NewsArticle extract(org.jsoup.nodes.Document doc);

    public abstract Set<String> getSupportedDomains();

}//END OF ArticleExtractor
