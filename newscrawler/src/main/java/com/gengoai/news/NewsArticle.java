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

import com.gengoai.Language;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.DocumentFactory;
import com.gengoai.hermes.Hermes;
import com.gengoai.hermes.Types;
import com.gengoai.news.extractor.ArticleExtractors;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Data
public class NewsArticle {
    private final List<String> keywords = new ArrayList<>();
    private String author;
    private String headline;
    private String pubDate;
    private String publisher;
    private String body;
    private String description;
    private String url;
    private List<String> sections = new ArrayList<>();
    private String locale;

    public void addKeywords(@NonNull Collection<String> keywords) {
        this.keywords.addAll(keywords);
    }

    public void addSections(@NonNull Collection<String> sections) {
        this.sections.addAll(sections);
    }


    public void update(NewsArticle other, boolean preferNewInformation) {
        if (other == null) {
            return;
        }
        if (Strings.isNullOrBlank(author) || (preferNewInformation && Strings.isNotNullOrBlank(other.author))) {
            author = other.author;
        }
        if (Strings.isNullOrBlank(headline) || (preferNewInformation && Strings.isNotNullOrBlank(other.headline))) {
            headline = other.headline;
        }
        if (Strings.isNullOrBlank(pubDate) || (preferNewInformation && Strings.isNotNullOrBlank(other.pubDate))) {
            pubDate = other.pubDate;
        }
        if (Strings.isNullOrBlank(publisher) || (preferNewInformation && Strings.isNotNullOrBlank(other.publisher))) {
            publisher = other.publisher;
        }
        if (Strings.isNullOrBlank(body) || (preferNewInformation && Strings.isNotNullOrBlank(other.body))) {
            body = other.body;
        }
        if (Strings.isNullOrBlank(description) || (preferNewInformation && Strings.isNotNullOrBlank(other.description))) {
            description = other.description;
        }
        if (Strings.isNullOrBlank(locale) || (preferNewInformation && Strings.isNotNullOrBlank(other.locale))) {
            locale = other.locale;
        }
        if (Strings.isNullOrBlank(url) || (preferNewInformation && Strings.isNotNullOrBlank(other.url))) {
            url = other.url;
        }
        keywords.addAll(other.keywords);
        sections.addAll(other.sections);
    }

    public static NewsArticle fromHTML(URL url, String rawHtml) throws Exception {
        var article = ArticleExtractors.get(url).extract(rawHtml);

        //Structured Data Extractor
        org.jsoup.nodes.Document doc = Jsoup.parse(rawHtml);
        article.update(OpenGraphMetadataExtractor.extract(doc), true);
        Elements elements = doc.select("script[type=\"application/ld+json\"]");
        for (Element element : elements) {
            article.update(SchemaOrgArticleExtractor.extract(element.html()), true);
        }

        if (Strings.isNullOrBlank(article.getUrl())) {
            article.setUrl(url.toString());
        }

        return article;
    }


    public Document toDocument() {
        if (getLocale() == null) {
            setLocale("en");
        }
        if (Strings.isNotNullOrBlank(getBody()) && Strings.isNotNullOrBlank(getUrl())) {
            Document document = DocumentFactory.getInstance().create(getUrl(), getBody());
            //DocumentFactory.getInstance().create(DigestUtils.md5Hex(getUrl()), getBody());

            final String locale = getLocale().replace("_", "-");
            final Language language;
            if (Strings.isNullOrBlank(locale)) {
                language = Hermes.defaultLanguage();
            } else {
                language = Language.fromLocale(Locale.forLanguageTag(locale));
            }
            document.setLanguage(language);
            document.put(Types.URL, getUrl());


            if (Strings.isNotNullOrBlank(getAuthor())) {
                document.put(Types.AUTHOR, getAuthor());
            }
            if (getKeywords() != null) {
                document.put(Types.KEYWORDS, getKeywords());
            }
            if (Strings.isNotNullOrBlank(getHeadline())) {
                document.put(Types.TITLE, getHeadline());
            }
            if (Strings.isNotNullOrBlank(getPublisher())) {
                document.put(Types.SOURCE, getPublisher());
            }
            if (Strings.isNotNullOrBlank(getDescription())) {
                document.put(Types.SUMMARY, List.of(getDescription()));
            }

            DateParser dateParser = new DateParser(Locale.forLanguageTag(locale));
            if (Strings.isNotNullOrBlank(getPubDate())) {
                document.put(Types.PUBLICATION_DATE, dateParser.parse(getPubDate()));
            }

            return document;
        }
        return null;
    }

}//END OF NewsArticle
