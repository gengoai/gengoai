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

import com.fasterxml.jackson.core.JsonParseException;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.util.*;

public class SchemaOrgArticleExtractor {

    public SchemaOrgArticleExtractor() {
        throw new IllegalAccessError();
    }

    private static String getPropertyName(Map<String, JsonEntry> map, String property) {
        JsonEntry entry = map.getOrDefault(property, JsonEntry.nullValue());
        if (entry.isNull()) {
            return Strings.EMPTY;
        }
        if (entry.isString()) {
            return entry.asString();
        }
        if (entry.isObject()) {
            if (entry.hasProperty("name")) {
                return entry.getStringProperty("name");
            } else if (entry.hasProperty("@id")) {
                return entry.getStringProperty("@id");
            }
            return Strings.EMPTY;
        }
        List<String> values = new ArrayList<>();
        entry.elementIterator()
                .forEachRemaining(e -> {
                    if (e.isString()) {
                        values.add(e.asString());
                    } else if (e.hasProperty("name")) {
                        values.add(e.getStringProperty("name"));
                    } else {
                        values.add(e.getStringProperty("@id"));
                    }
                });
        return String.join(", ", values);
    }

    private static Set<String> getSchemaTypes(Map<String, JsonEntry> map) {
        JsonEntry type = map.getOrDefault("@type", JsonEntry.nullValue());

        if (type.isNull()) {
            return Collections.emptySet();
        }

        Set<String> types = new HashSet<>();
        if (type.isString()) {
            types.add(type.asString());
        } else {
            type.elementIterator()
                    .forEachRemaining(e -> types.add(e.asString()));
        }

        return types;
    }

    private static List<String> getText(Map<String, JsonEntry> map, String property) {
        JsonEntry entry = map.getOrDefault(property, JsonEntry.nullValue());
        if (entry.isNull()) {
            return Collections.emptyList();
        }
        if (entry.isString()) {
            return Strings.split(entry.asString(), ',');
        }
        List<String> values = new ArrayList<>();
        entry.elementIterator()
                .forEachRemaining(e -> {
                    if (e.isObject()) {
                        if (e.hasProperty("termCode")) {
                            JsonEntry termCode = e.getProperty("termCode");
                            if (termCode.isObject()) {
                                values.add(termCode.getStringProperty("label"));
                            } else {
                                values.add(e.getStringProperty("termCode"));
                            }
                        }
                    } else {
                        values.add(e.asString());
                    }
                });
        return values;
    }

    public static NewsArticle extract(String json) throws IOException {
        JsonEntry e;
        try {
            e = Json.parse(json);
        } catch (JsonParseException ex) {
            return null;
        }
        NewsArticle article = new NewsArticle();
        if (!e.isObject()) {
            Iterator<JsonEntry> itr = e.elementIterator();
            while (itr.hasNext()) {
                e = itr.next();
                Map<String, JsonEntry> map = e.asMap();
                if (getText(map, "@type").contains("NewsArticle")) {
                    break;
                }
            }
        }
        Map<String, JsonEntry> map = e.asMap();
        if (getText(map, "@type").contains("NewsArticle")) {
            article.setAuthor(getPropertyName(map, "author"));
            article.setPublisher(getPropertyName(map, "publisher"));
            article.setHeadline(map.get("headline").asString());
            article.addKeywords(getText(map, "keywords"));
            article.addSections(getText(map, "articleSection"));
//            article.setUrl(getPropertyName(map, "mainEntityOfPage"));
            article.setLocale(getPropertyName(map, "inLanguage"));
            article.setPubDate(getPropertyName(map, "datePublished"));
            article.setDescription(getPropertyName(map, "description"));
            article.setBody(getPropertyName(map, "articleBody"));
        }
        return article;
    }

}//END OF SchemaOrgArticleExtractor
