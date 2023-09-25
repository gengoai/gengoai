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

import com.gengoai.io.Resources;
import com.gengoai.news.NewsArticle;
import org.jsoup.nodes.Document;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Set;

@MetaInfServices
public class NBCNewsExtractor extends ArticleExtractor {
    @Override
    protected NewsArticle extract(Document doc) {
        removeElements(doc, List.of("figure", "script", "figcaption"));
        NewsArticle article = new NewsArticle();
        article.setBody(findFirst(doc, List.of("div[class*='article-body__content'] > p")));
        article.setHeadline(findFirst(doc, List.of("h1[class*='article-hero-headline__htag']")));
        article.setAuthor(findFirst(doc, List.of("div[class*='article-inline-byline'] > span")));
        return article;
    }

    @Override
    public Set<String> getSupportedDomains() {
        return Set.of("nbcnews.com");
    }

    public static void main(String[] args) throws Exception{
        String html = Resources.from("https://www.nbcnews.com/news/nbcblk/lawsuit-accuses-beverly-hills-police-racially-profiling-black-drivers-rcna104865").readToString();
        System.out.println(new NBCNewsExtractor().extract(html));
    }

}//END OF NBCNewsExtractor
