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

package com.gengoai.hermes.extraction.caduceus;

import com.gengoai.collection.Iterables;
import com.gengoai.collection.multimap.ArrayListMultimap;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.ListMultimap;
import com.gengoai.collection.multimap.SetMultimap;
import com.gengoai.hermes.*;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.extraction.regex.TokenMatcher;
import com.gengoai.hermes.extraction.regex.TokenRegex;
import com.gengoai.tuple.Tuple2;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder
class Rule implements Serializable {
    private static final long serialVersionUID = 1L;
    @NonNull
    @Singular
    List<AnnotationProvider> annotationProviders;
    @NonNull
    String name;
    @NonNull
    String programFile;
    @NonNull
    @Singular
    List<RelationProvider> relationProviders;
    @NonNull
    TokenRegex trigger;

    public List<HString> execute(Document document) {
        List<HString> hits = new ArrayList<>();
        for (Annotation sentence : document.sentences()) {
            TokenMatcher matcher = trigger.matcher(sentence);
            while (matcher.find()) {
                ListMultimap<String, Annotation> groups = new ArrayListMultimap<>();
                ListMultimap<AnnotationProvider, Annotation> providers = new ArrayListMultimap<>();

                //Process all the annotation providers
                annotationProviders.forEach(ap -> {
                    if (ap.getCapture().equals("*")) {
                        Annotation annotation = createOrGet(document,
                                ap.getType(),
                                matcher.group(),
                                ap.getAttributeMap());
                        groups.put("*", annotation);
                        Iterables.getFirst(matcher.groupNames())
                                .ifPresent(s -> {
                                    groups.put(s, annotation);
                                });
                        providers.put(ap, annotation);
                    } else {
                        matcher.group(ap.getCapture()).forEach(g -> {
                            if (ap.getExpand() != null) {
                                g = ap.getExpand().applyAsHString(g);
                            }
                            Annotation annotation = createOrGet(document,
                                    ap.getType(),
                                    g,
                                    ap.getAttributeMap());
                            groups.put(ap.getCapture(), annotation);
                            providers.put(ap, annotation);
                        });
                    }
                });

                if (!groups.containsKey("*")) {
                    groups.putAll("*", matcher.group().tokens());
                }

                SetMultimap<String, Tuple2<Annotation, Relation>> relations = new HashSetMultimap<>();
                for (RelationProvider rp : relationProviders) {
                    List<Annotation> sourceAnnotations = getAnnotation(rp.getSource(), groups, matcher);
                    List<Annotation> targetAnnotations = getAnnotation(rp.getTarget(), groups, matcher);
                    for (Annotation source : sourceAnnotations) {
                        if (rp.getSourceFilter() != null && rp.getSourceFilter().test(source)) {
                            continue;
                        }
                        for (Annotation target : targetAnnotations) {
                            if (rp.getTargetFilter() != null && rp.getTargetFilter().test(target)) {
                                continue;
                            }
                            relations.put(rp.getName(), Tuple2.of(source,
                                    new Relation(rp.getType(), rp.getValue(), target.getId())));
                            if (rp.isBidirectional()) {
                                relations.put(rp.getName(), Tuple2.of(target,
                                        new Relation(rp.getType(), rp.getValue(), source
                                                .getId())));
                            }
                        }
                    }
                }

                Set<String> finalRelations = new HashSet<>();
                for (RelationProvider rp : relationProviders) {
                    if (rp.getRequired().isEmpty() || relations.keySet().containsAll(rp.getRequired())) {
                        relations.get(rp.getName()).forEach(t -> {
                            t.getV1().add(t.getV2());
                            finalRelations.add(rp.getValue());
                        });
                    }
                }


                providers.entries()
                        .stream()
                        .filter(entry -> !finalRelations.containsAll(entry.getKey().getRequired()))
                        .forEach(entry -> document.remove(entry.getValue()));

                providers.entries()
                        .stream()
                        .filter(entry -> finalRelations.containsAll(entry.getKey().getRequired()))
                        .forEach(entry -> hits.add(entry.getValue()));
            }
        }

        return hits;
    }

    private Annotation createOrGet(Document document, AnnotationType type, HString span, AttributeMap attributeValMap) {
        return document.substring(span.start(), span.end()).annotations(type).stream()
                .filter(a -> a.getType().equals(type)
                        && a.attributeMap().entrySet().equals(attributeValMap.entrySet()))
                .findFirst()
                .orElseGet(() -> document
                        .annotationBuilder(type)
                        .bounds(span)
                        .attributes(attributeValMap)
                        .attribute(Types.CADUCEUS_RULE, programFile + "::" + name)
                        .createAttached());
    }

    private List<Annotation> getAnnotation(Tuple2<String, LyreExpression> point,
                                           ListMultimap<String, Annotation> groups,
                                           TokenMatcher matcher) {
        List<Annotation> annotations;
        if (groups.containsKey(point.v1)) {
            annotations = groups.get(point.v1);
        } else {
            annotations = matcher.group(point.v1).stream().map(HString::asAnnotation).collect(Collectors.toList());
        }

        if (point.v2.getPattern().equals("$_")) {
            return annotations;
        }

        List<Annotation> toReturn = new ArrayList<>();
        for (Annotation annotation : annotations) {
            for (HString hString : point.v2.applyAsList(annotation, HString.class)) {
                Annotation a = hString.asAnnotation();
                if (a.isEmpty()) {
                    continue;
                }
                if (a.isDetached() && a.document() != null) {
                    a.attach();
                }
                toReturn.add(a);
            }
        }
        return toReturn;
    }
}//END OF Rule
