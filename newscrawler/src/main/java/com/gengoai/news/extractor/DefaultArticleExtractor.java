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
import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DefaultArticleExtractor extends ArticleExtractor {

    protected NewsArticle extract(org.jsoup.nodes.Document doc) {

        for (Element figure : doc.select("figure")) {
            figure.remove();
        }
        for (Element figcaption : doc.select("figcaption")) {
            figcaption.remove();
        }
        for (Element script : doc.select("script")) {
            script.remove();
        }


        List<String> headlines = List.of(
                "meta[property='headline']",
                "[class~=-headline$]"
                                        );

        List<String> author = List.of(
                "meta[name='author']",
                "meta[property='article:author']",
                "[class*='byline']"
                                     );

        List<String> pubdate = List.of(
                "meta[name='date']",
                "[class~=timestamp] > time",
                "[class*='published']"
                                      );

        List<String> content = List.of(
                "[class~=caas-body] > p", // Yahoo News 7/4/2023
                "div[class*='article__content'] > p[class*='paragraph']", // CNN News 7/4/2023
                "article", //HTML 5
                "[class~=(body|story).*paragraph]",
                "[class*='current-article']",
                "[class*='article-body']",
                "script[data-schema='NewsArticle']",
                "[class~=story-body]",
                "[class~=content] > p",
                "[id=*'article-content'] > p"
                                      );


        NewsArticle article = new NewsArticle();
        article.setBody(findFirst(doc, content));
        article.setPubDate(findFirst(doc, pubdate));
        article.setAuthor(findFirst(doc, author));
        article.setHeadline(findFirst(doc, headlines));
        article.setLocale(doc.select("html").first().attr("lang"));
        return article;
    }

    @Override
    public Set<String> getSupportedDomains() {
        return Collections.emptySet();
    }


}//END OF ArticleExtractor
