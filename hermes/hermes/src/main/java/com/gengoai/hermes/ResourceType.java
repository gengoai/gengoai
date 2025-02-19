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

package com.gengoai.hermes;

import com.gengoai.Language;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.embedding.OnDiskVectorStore;
import com.gengoai.cache.Cache;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.function.Unchecked;
import com.gengoai.hermes.annotator.NerPatterns;
import com.gengoai.hermes.extraction.caduceus.CaduceusProgram;
import com.gengoai.hermes.lexicon.LexiconManager;
import com.gengoai.hermes.lexicon.TrieWordList;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.parsing.ParseException;
import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.gengoai.LogUtils.logFinest;
import static com.gengoai.hermes.Hermes.HERMES_PACKAGE;

/**
 * Defines common resource used by Hermes and methods for finding configuration values and resources for them.
 */
@Log
public enum ResourceType {
    EMBEDDINGS("vectors") {
        @Override
        public <T> T load(@NonNull String configKey, @NonNull String resourceName, @NonNull Language language) {
            OnDiskVectorStore vectorStore = new OnDiskVectorStore(locate(configKey, resourceName, language)
                    .orElseThrow(() -> new RuntimeException(resourceName + " does not exist.")));
            return Cast.as(vectorStore);
        }
    },
    /**
     * Lexicon Resources (supports in-memory via json files and on-disk)
     */
    LEXICON("lexicons", ".json", ".lexicon") {
        @Override
        public <T> T load(@NonNull String configKey, @NonNull String resourceName, @NonNull Language language) {
            return Cast.as(LexiconManager.getLexicon(resourceName, language));
        }
    },
    WORD_LIST("lexicons", ".txt", ".dic", ".txt.gz", ".dic.gz") {
        @Override
        public <T> T load(@NonNull String configKey, @NonNull String resourceName, @NonNull Language language) {
            try {
                return Cast.as(TrieWordList.read(locate(configKey, resourceName, language)
                        .orElseThrow(() -> new RuntimeException(resourceName + " does not exist."))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    },
    /**
     * Machine Learning Model resources
     */
    MODEL("models") {
        @Override
        public <T> T load(@NonNull String configKey,
                          @NonNull String resourceName,
                          @NonNull Language language) {
            return Cast.as(locate(configKey, resourceName, language)
                    .map(Unchecked.function(ModelIO::load))
                    .orElseThrow(() -> new RuntimeException(resourceName + " does not exist.")));
        }
    },
    /**
     * Caduceus programs
     */
    CADUCEUS("caduceus", ".cg") {
        @Override
        public <T> T load(@NonNull String configKey, @NonNull String resourceName, @NonNull Language language) {
            Resource resource = locate(configKey, resourceName, language).orElseThrow(() -> new RuntimeException(resourceName + " does not exist."));
            try {
                if (resource.isDirectory()) {
                    return Cast.as(CaduceusProgram.read(resource.getChildren("*.cg", true)));
                }
                return Cast.as(CaduceusProgram.read(resource));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    },
    NER_PATTERNS("lexicons", ".patterns") {
        @Override
        public <T> T load(@NonNull String configKey, @NonNull String resourceName, @NonNull Language language) {
            return Cast.as(locate(configKey, resourceName, language)
                    .map(Unchecked.function(NerPatterns::load))
                    .orElseThrow(() -> new RuntimeException(resourceName + " does not exist.")));
        }
    },
    MORPHOLOGY_RULES("lexicons", ".json") {
        @Override
        public <T> T load(@NonNull String configKey, @NonNull String resourceName, @NonNull Language language) {
            return Cast.as(locate(configKey, resourceName, language)
                    .map(Unchecked.function(Json::parse))
                    .orElseThrow(() -> new RuntimeException(resourceName + " does not exist.")));
        }
    };

    @Getter
    private final String type;
    private final String[] fileExtensions;

    ResourceType(String type, String... fileExtensions) {
        this.type = type;
        this.fileExtensions = fileExtensions.length > 0
                ? fileExtensions
                : new String[]{Strings.EMPTY};
    }

    /**
     * Creates a Language-keyed cache for this resource type that loads the resource based on the given configKey and
     * resourceName
     *
     * @param <M>          the resource type parameter
     * @param size         the maximum size of the cache
     * @param configKey    the configuration key to use when looking for the location in Config
     * @param resourceName the resource name
     * @return the cache
     */
    public <M> Cache<Language, M> createCache(int size, @NonNull String configKey, @NonNull String resourceName) {
        return Cache.create(size,
                language -> load(configKey, resourceName, language));
    }

    /**
     * Creates a Language-keyed cache for this resource type that loads the resource based on the given configKey and
     * resourceName
     *
     * @param <M>          the resource type parameter
     * @param configKey    the configuration key to use when looking for the location in Config
     * @param resourceName the resource name
     * @return the cache
     */
    public <M> Cache<Language, M> createCache(@NonNull String configKey, @NonNull String resourceName) {
        return Cache.create(Integer.MAX_VALUE,
                language -> load(configKey, resourceName, language));
    }

    /**
     * <p>Gets the config value, if any, for the this resource type with the given name and language.</p>
     *
     * <p>
     * The method will look for the following configuration keys:
     * <ol>
     * <li>[resourceName].[language].[type]</li>
     * <li>[resourceName].[language]</li>
     * <li>[resourceName].[type]</li>
     * <li>[resourceName]</li>
     * </ol>
     * </p>
     *
     * @param resourceName the resource name
     * @param language     the language
     * @return the config value
     */
    public Optional<String> getConfigValue(@NonNull String resourceName, @NonNull Language language) {
        for (String r : new String[]{
                Config.get(resourceName, language, type).asString(),
                Config.get(resourceName, language).asString(),
                Config.get(resourceName, type).asString(),
                Config.get(resourceName).asString()
        }) {
            if (Strings.isNotNullOrBlank(r)) {
                logFinest(log, "Found configuration value for ''{0}'' and language ''{1}'': {2}",
                        resourceName,
                        language,
                        r);
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    /**
     * Loads a resource of this type by finding the resource with the given resource name and language. Will throw a
     * runtime exception if the resources is not located or there is a problem loading it.
     *
     * @param <T>          the type of resource returned
     * @param resourceName the resource name
     * @param language     the language version of the resource to load
     * @return the loaded resource
     */
    public <T> T load(@NonNull String resourceName, @NonNull Language language) {
        return load(resourceName, resourceName, language);
    }

    /**
     * Loads a resource of this type by finding the resource with the given configuration key, resource name, and
     * language. Will throw a runtime exception if the resources is not located or there is a problem loading it.
     *
     * @param <T>          the type of resource returned
     * @param configKey    the configuration key to use when looking for the location in Config
     * @param resourceName the resource name
     * @param language     the language version of the resource to load
     * @return the loaded resource
     */
    public abstract <T> T load(@NonNull String configKey,
                               @NonNull String resourceName,
                               @NonNull Language language);

    /**
     * <p>Common method for finding a resource of a given type. {@see ResourceType.findResource(String, String,
     *Language)}** for details. This method uses the name also as the config property</p>
     *
     * @param resourceName the resource name
     * @param language     the language of the model
     * @return the resource location or null
     */
    public Optional<Resource> locate(@NonNull String resourceName,
                                     @NonNull Language language) {
        return locate(resourceName, resourceName, language);
    }

    /**
     * <p>Common method for finding a resource of a given type</p>
     *
     * <p>
     * The method will look in the following locations:
     * <ol>
     * <li>[configKey].[language].[type]</li>
     * <li>[hermes.resources.dir]/[language]/[type]/[resourceName]</li>
     * <li>classpath:com/gengoai/hermes/[language]/[type]/[resourceName]</li>
     * <li>configKey.[language]</li>
     * <li>configKey</li>
     * <li>configKey.[type]</li>
     * <li>[hermes.resources.dir]/default/[type]/[resourceName]</li>
     * <li>classpath:com/gengoai/hermes/default/[type]/[resourceName]</li>
     * </ol>
     * where <code>language</code> is the two-letter (lowercased) language code.
     * </p>
     *
     * @param configKey    the config key to use for locating the model
     * @param resourceName the resource resourceName
     * @param language     the language of the model
     * @return the resource location or null
     */
    public Optional<Resource> locate(@NonNull String configKey,
                                     @NonNull String resourceName,
                                     @NonNull Language language) {
        String langCode = language.getCode().toLowerCase();
        Resource baseDir = Hermes.getResourcesDir();
        Resource classpathDir = Resources.fromClasspath(HERMES_PACKAGE.replace('.', '/') + "/");
        for (String ext : fileExtensions) {
            String nameWithExt = Strings.appendIfNotPresent(resourceName, ext);
            for (String fileName : new String[]{nameWithExt, Strings.appendIfNotPresent(nameWithExt, ".gz")}) {
                for (Resource r : Arrays.asList(
                        Config.get(configKey, language, type).asResource(),
                        baseDir.getChild(langCode).getChild(type).getChild(fileName),
                        classpathDir.getChild(langCode).getChild(type).getChild(fileName),
                        Config.get(configKey, language).asResource(),
                        Config.get(configKey).asResource(),
                        Config.get(configKey, type).asResource(),
                        baseDir.getChild("default").getChild(type).getChild(fileName),
                        classpathDir.getChild("default").getChild(type).getChild(fileName))) {
                    if (r != null && r.exists()) {
                        if (resourceName.equalsIgnoreCase(configKey)) {
                            logFinest(log, "Found resource ''{0}'' for language ''{1}'': {2}",
                                    resourceName,
                                    language,
                                    r);
                        } else {
                            logFinest(log, "Found resource ''{0}'' for language ''{1}'' with given config property ''{2}'': {3}",
                                    resourceName,
                                    language,
                                    configKey,
                                    r);
                        }
                        return Optional.of(r);
                    }
                }
            }
        }
        return Optional.empty();
    }

}//END OF ResourceType
