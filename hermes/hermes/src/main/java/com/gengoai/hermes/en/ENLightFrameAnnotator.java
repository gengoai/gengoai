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

package com.gengoai.hermes.en;

import com.gengoai.StringTag;
import com.gengoai.collection.Iterables;
import com.gengoai.hermes.*;
import com.gengoai.hermes.annotator.SentenceLevelAnnotator;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ENLightFrameAnnotator extends SentenceLevelAnnotator {
    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.LIGHT_FRAME_ROLE, Types.LIGHT_FRAME_ARG, Types.LIGHT_FRAME);
    }

    public static Annotation getSubTree(Annotation annotation, RelationGraph rg) {
        if (annotation == null) {
            return null;
        }
        annotation = rg.getSubTreeText(annotation, true).asAnnotation();
        if (annotation.document() != null) {
            annotation.attach();
        }
        return annotation;
    }


    @Override
    protected Set<AnnotatableType> furtherRequires() {
        return Set.of(Types.ENTITY, Types.DEPENDENCY, Types.PHRASE_CHUNK);
    }

    @Override
    protected void annotate(Annotation sentence) {
        Document doc = sentence.document();
        RelationGraph rg = sentence.dependencyGraph();
        for (Annotation chunk : sentence.annotations(Types.PHRASE_CHUNK)) {
            if (chunk.pos().isVerb()) {
                boolean isPassive = chunk.hasIncomingRelation(Types.DEPENDENCY, "nsubjpass");
                boolean isCopula = chunk.hasOutgoingRelation(Types.DEPENDENCY, "cop");
                boolean hasDObj = chunk.hasIncomingRelation(Types.DEPENDENCY, "dobj");
                boolean hasXComp = chunk.hasIncomingRelation(Types.DEPENDENCY, "xcomp");
                boolean hasCComp = chunk.hasIncomingRelation(Types.DEPENDENCY, "ccomp");
                Annotation a0 = null, a1 = null, a2 = null;
                boolean expandA0 = true;
                boolean expandA1 = true;
                boolean expandA2 = true;

                if (isPassive) {
                    a0 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "pobj_by"), null);
                    a1 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "nsubjpass"), null);
                    a0 = getSubTree(a0, rg);
                    a1 = getSubTree(a1, rg);
                } else if (isCopula) {
                    a1 = chunk.parent();
                    a0 = Iterables.getFirst(a1.incoming(Types.DEPENDENCY, "nsubj"), null);
                    if (a1.hasIncomingRelation(Types.DEPENDENCY, "dobj")) {
                        a2 = Iterables.getFirst(a1.incoming(Types.DEPENDENCY, "dobj"), null);
                    } else if (hasXComp) {
                        a2 = Iterables.getFirst(a1.incoming(Types.DEPENDENCY, "xcomp"), null);
                    }
                    a0 = getSubTree(a0, rg);
                    a2 = getSubTree(a2, rg);
                } else {
                    a0 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "nsubj"), null);

                    if (a0 == null && chunk.hasOutgoingRelation(Types.DEPENDENCY, "xcomp")) {
                        //Get the A0 as the nsubj of the xcomp
                        Annotation xcomp = Iterables.getFirst(chunk.outgoing(Types.DEPENDENCY, "xcomp"), null);
                        a0 = Iterables.getFirst(xcomp.incoming(Types.DEPENDENCY, "nsubj"), null);
                    } else if (a0 == null && chunk.hasOutgoingRelation(Types.DEPENDENCY, "infmod")) {
                        //Get the A0 as the infmod
                        a0 = Iterables.getFirst(chunk.outgoing(Types.DEPENDENCY, "infmod"), null);
                        expandA0 = false;
                    } else if (a0 == null && chunk.hasOutgoingRelation(Types.DEPENDENCY, "conj")) {
                        //Get the A0 as the nsubj of the conj's ccomp
                        Annotation conj = Iterables.getFirst(chunk.outgoing(Types.DEPENDENCY, "conj"), null);
                        if (conj.hasOutgoingRelation(Types.DEPENDENCY, "ccomp")) {
                            Annotation ccomp = Iterables.getFirst(conj.outgoing(Types.DEPENDENCY, "ccomp"), null);
                            a0 = Iterables.getFirst(ccomp.incoming(Types.DEPENDENCY, "nsubj"), null);
                        }
                    }

                    if (hasDObj) {
                        a1 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "dobj"), null);
                        a2 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "xcomp"), null);
                    } else if (hasXComp) {
                        a1 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "xcomp"), null);
                    } else if (hasCComp) {
                        a1 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "ccomp"), null);
                    } else if (chunk.hasIncomingRelation(Types.DEPENDENCY, "pobj_to")) {
                        a1 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "pobj_to"), null);
                    } else {
                        a1 = Iterables.getFirst(chunk.incoming(Types.DEPENDENCY, "advcl"), null);
                    }

                    if (expandA0) a0 = getSubTree(a0, rg);
                    if (expandA1) a1 = getSubTree(a1, rg);
                    if (expandA2) a2 = getSubTree(a2, rg);
                }

                if (a0 != null || a1 != null || a2 != null) {
                    Annotation frame = doc.createAnnotation(
                            Types.LIGHT_FRAME,
                            chunk.start(),
                            chunk.end(),
                            Map.of(Types.TAG, new StringTag("VERB_FRAME"))
                    );
                    if (a0 != null) {
                        a0 = doc.createAnnotation(
                                Types.LIGHT_FRAME_ARG,
                                a0.start(),
                                a0.end(),
                                Collections.emptyMap()
                        );
                        a0.add(new Relation(Types.LIGHT_FRAME_ROLE, "a0", frame.getId()));
                    }
                    if (a1 != null) {
                        a1 = doc.createAnnotation(
                                Types.LIGHT_FRAME_ARG,
                                a1.start(),
                                a1.end(),
                                Collections.emptyMap()
                        );
                        a1.add(new Relation(Types.LIGHT_FRAME_ROLE, "a1", frame.getId()));
                    }
                    if (a2 != null) {
                        a2 = doc.createAnnotation(
                                Types.LIGHT_FRAME_ARG,
                                a2.start(),
                                a2.end(),
                                Collections.emptyMap()
                        );
                        a2.add(new Relation(Types.LIGHT_FRAME_ROLE, "a2", frame.getId()));
                    }

                    if (chunk.hasIncomingRelation(Types.DEPENDENCY, "neg")) {
                        frame.put(Types.IS_NEGATED, true);
                    }


                    for (Relation rel : chunk.incomingRelations(Types.DEPENDENCY)) {
                        if (rel.getValue().startsWith("pcomp_")) {
                            Annotation target = rel.getTarget(doc);
                            Annotation vp = Iterables.getFirst(target.annotations(Types.PHRASE_CHUNK), null);
                            if (vp == null) {
                                continue;
                            }
                            vp = doc.createAnnotation(
                                    Types.LIGHT_FRAME_ARG,
                                    vp.start(),
                                    vp.end(),
                                    Collections.emptyMap()
                            );
                            vp.add(new Relation(Types.LIGHT_FRAME_ROLE, rel.getValue(), frame.getId()));
                        }
                        if (rel.getValue().startsWith("pobj_")) {
                            Annotation target = getSubTree(rel.getTarget(doc), rg);
                            target = doc.createAnnotation(
                                    Types.LIGHT_FRAME_ARG,
                                    target.start(),
                                    target.end(),
                                    Collections.emptyMap()
                            );
                            String role = rel.getValue();

                            boolean isTime = target.annotations(Types.ENTITY)
                                    .stream()
                                    .anyMatch(e -> e.getTag().isInstance(Entities.DATE));
                            boolean isLocation = target.annotations(Types.ENTITY)
                                    .stream()
                                    .anyMatch(e -> e.getTag().isInstance(Entities.LOCATION) || e.getTag().isInstance(Entities.FACILITY));


                            if (isTime) {
                                role = "time";
                            } else if (isLocation) {
                                role = "location";
                            }

                            target.add(new Relation(Types.LIGHT_FRAME_ROLE, role, frame.getId()));
                        }
                    }


                }
            }
        }
    }
}
