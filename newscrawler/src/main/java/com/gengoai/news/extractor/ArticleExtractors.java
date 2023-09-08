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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class ArticleExtractors {

    private ArticleExtractors() {
        throw new IllegalAccessError();
    }

    private static final Map<String, ArticleExtractor> domain2Extractor = new HashMap<>();
    private static final ArticleExtractor defaultExtractor = new DefaultArticleExtractor();

    static {
        for (ArticleExtractor extractor : ServiceLoader.load(ArticleExtractor.class)) {
            for (String domain : extractor.getSupportedDomains()) {
                domain2Extractor.put(domain, extractor);
            }
        }
    }

    public static ArticleExtractor get(URL url) {
        return domain2Extractor.getOrDefault(url.getHost(), defaultExtractor);
    }

}
