/*
 * (c) 2005 David B. Bracewell
 *
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gengoai.Language;
import com.gengoai.Validation;
import com.gengoai.collection.tree.Span;
import com.gengoai.conversion.Cast;
import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>Default implementation of a {@link Document} storing everything in-memory using an {@link AnnotationSet} for fast
 * access to {@link Annotation}s</p>
 *
 * @author David B. Bracewell
 */
@JsonPropertyOrder({"id", "content"})
@JsonIgnoreProperties({"start", "end"})
class DefaultDocumentImpl extends BaseHString implements Document {
    private static final long serialVersionUID = 1L;
    AnnotationSet annotationSet;
    AtomicLong idGenerator = new AtomicLong(0);
    @JsonProperty("content")
    private String content;
    @JsonProperty("id")
    private String id;
    private transient volatile List<Annotation> tokens;

    /**
     * Instantiates a new Document.
     *
     * @param id      the document id
     * @param content the document content
     */
    DefaultDocumentImpl(String id, String content) {
        this(id, content, null);
    }

    /**
     * Instantiates a new Document.
     *
     * @param id       the document id
     * @param content  the document content
     * @param language the language the document is written in
     */
    DefaultDocumentImpl(String id, String content, Language language) {
        super(0, content.length());
        this.content = content;
        setId(id);
        setLanguage(language);
        this.annotationSet = new AnnotationSet();
    }

    public void clearAnnotations() {
        annotationSet.clear();
        if (tokens != null) {
            tokens = null;
        }
    }

    @JsonCreator
    private DefaultDocumentImpl(@JsonProperty("content") String content,
                                @JsonProperty("id") String id,
                                @JsonProperty("completed") Map<String, String> providers,
                                @JsonProperty("attributes") AttributeMap attributeMap,
                                @JsonProperty("annotations") List<Annotation> annotations) {
        this(id, content);
        AnnotatableType t = Types.ENTITY;
        if (providers != null) {
            providers.forEach((type, provider) -> {
                                  this.annotationSet.setIsCompleted(AnnotatableType.valueOf(type), true, provider);
                              }
                             );
        }
        if (attributeMap != null) {
            this.attributeMap().putAll(attributeMap);
        }
        if (annotations != null) {
            long i = 0;
            for (Annotation annotation : annotations) {
                Cast.<DefaultAnnotationImpl>as(annotation).setDocument(this);
                annotationSet.add(annotation);
                i = Math.max(i, annotation.getId());
            }
            idGenerator.set(i + 1);
        }

        Map<Annotation, Set<Relation>> am = new HashMap<>();
        if (annotations != null) {
            for (Annotation a : annotations) {
                am.put(a, new HashSet<>(a.outgoingRelations(false)));
                if (a instanceof DefaultAnnotationImpl) {
                    ((DefaultAnnotationImpl) a).incomingRelations.clear();
                    ((DefaultAnnotationImpl) a).outgoingRelations.clear();
                }

            }
            am.forEach((a, relations) -> {
                relations.forEach(a::add);
            });
        }
    }

    @Override
    public void annotate(AnnotatableType... types) {
        new AnnotationPipeline(types).annotate(this);
    }

    @Override
    public Annotation annotation(long id) {
        return annotationSet.get(id);
    }

    @Override
    @JsonProperty("annotations")
    public List<Annotation> annotations() {
        if (annotationSet.size() == 0) {
            return Collections.emptyList();
        }
        return Streams.asStream(annotationSet.iterator())
                      .collect(Collectors.toList());
    }

    @Override
    public List<Annotation> annotations(AnnotationType type, Span span) {
        return annotationSet.select(span, a -> a.isInstance(type) && a.overlaps(span));
    }

    @Override
    public List<Annotation> annotations(AnnotationType type, Span span, Predicate<? super Annotation> filter) {
        return annotationSet.select(span, a -> filter.test(a) && a.isInstance(type) && a.overlaps(span));
    }

    @Override
    public List<Annotation> annotations(@NonNull AnnotationType type) {
        return annotationSet.select(a -> a.isInstance(type));
    }

    @Override
    public List<Annotation> annotations(@NonNull AnnotationType type, @NonNull Predicate<? super Annotation> filter) {
        return annotationSet.select(a -> filter.test(a) && a.isInstance(type));
    }

    @Override
    public void attach(@NonNull Annotation annotation) {
        Validation.checkArgument(annotation.document() == this,
                                 "Error: Attempting to attach an annotation to a different document.");
        if (annotation.isDetached()) {
            annotation.setId(idGenerator.getAndIncrement());
            annotationSet.add(annotation);
            annotation.outgoingRelationStream()
                      .forEach(relation -> relation.getTarget(this)
                                                   .incomingRelations()
                                                   .add(new Relation(relation.getType(),
                                                                     relation.getValue(),
                                                                     annotation.getId())));
        }
    }

    @Override
    public char charAt(int index) {
        return content.charAt(index);
    }

    public Annotation cloneAnnotation(Annotation annotation) {
        Annotation clone = new DefaultAnnotationImpl(this, annotation.getType(), annotation.start(), annotation.end());
        annotation.setId(annotation.getId());
        annotation.putAll(annotation.attributeMap());
        annotation.addAll(annotation.outgoingRelations(false));
        annotationSet.add(annotation);
        return annotation;
    }

    @Override
    public Set<AnnotatableType> completed() {
        return annotationSet.getCompleted();
    }

    @Override
    public boolean contains(@NonNull Annotation annotation) {
        return annotationSet.contains(annotation);
    }

    @Override
    public Annotation createAnnotation(@NonNull AnnotationType type,
                                       int start,
                                       int end,
                                       @NonNull Map<AttributeType<?>, ?> attributeMap,
                                       @NonNull List<Relation> relations) {
        Validation.checkArgument(start >= start(),
                                 "Annotation must have a starting position >= the start of the document");
        Validation.checkArgument(end <= end(), "Annotation must have a ending position <= the end of the document");
        Annotation annotation = new DefaultAnnotationImpl(this, type, start, end);
        annotation.setId(idGenerator.getAndIncrement());
        annotation.putAll(attributeMap);
        annotation.addAll(relations);
        annotationSet.add(annotation);
        return annotation;
    }

    @Override
    public Annotation createAnnotation(AnnotationType type,
                                       int start,
                                       int end,
                                       Map<AttributeType<?>, ?> attributeMap) {
        return createAnnotation(type, start, end, attributeMap, Collections.emptyList());
    }

    @Override
    public String getAnnotationProvider(@NonNull AnnotatableType type) {
        return annotationSet.getAnnotationProvider(type);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        if (Strings.isNullOrBlank(id)) {
            this.id = UUID.randomUUID().toString();
        } else {
            this.id = id;
        }
    }

    @Override
    public boolean isCompleted(AnnotatableType type) {
        return annotationSet.isCompleted(type);
    }

    @Override
    public Annotation next(@NonNull Annotation annotation, @NonNull AnnotationType type) {
        return annotationSet.next(annotation, type);
    }

    @Override
    public int numberOfAnnotations() {
        return annotationSet.size();
    }

    @Override
    public Annotation previous(@NonNull Annotation annotation, @NonNull AnnotationType type) {
        return annotationSet.previous(annotation, type);
    }

    @Override
    public Map<AnnotatableType, String> providers() {
        return annotationSet.getProviders();
    }

    @Override
    public boolean remove(@NonNull Annotation annotation) {
        return annotationSet.remove(annotation);
    }

    @Override
    public void removeAnnotationType(@NonNull AnnotationType type) {
        annotationSet.removeAll(type);
        completed().remove(type);
        for (AnnotationType child : type.children()) {
            removeAnnotationType(child);
            completed().remove(child);
        }

    }

    @Override
    public void setCompleted(@NonNull AnnotatableType type, @NonNull String provider) {
        annotationSet.setIsCompleted(type, true, provider);
    }

    @Override
    public void setUncompleted(@NonNull AnnotatableType type) {
        annotationSet.setIsCompleted(type, false, null);
    }

    @Override
    public String toString() {
        return content;
    }

    @Override
    public List<Annotation> tokens() {
        if (tokens == null) {
            synchronized (this) {
                if (tokens == null) {
                    tokens = annotations(Types.TOKEN);
                }
            }
        }
        return tokens;
    }

}//END OF InMemoryDocument
