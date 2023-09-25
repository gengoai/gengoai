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
import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Light Semantic Annotator based on dependency trees
 */
public class ENLightFrameAnnotator extends SentenceLevelAnnotator {
    private static Annotation getSubTree(Annotation annotation, RelationGraph rg) {
        if (annotation == null) {
            return null;
        }
        annotation = rg.getSubTreeText(annotation, true)
                       .asAnnotation();
        if (annotation.document() != null) {
            annotation.attach();
        }
        return annotation;
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.LIGHT_FRAME_ROLE, Types.LIGHT_FRAME_ARG, Types.LIGHT_FRAME);
    }

    @Override
    protected Set<AnnotatableType> furtherRequires() {
        return Set.of(Types.ENTITY, Types.DEPENDENCY, Types.PHRASE_CHUNK, Types.MWE);
    }

    private void processPassive(VerbFrame frameObj) {
        frameObj.a0 = frameObj.getFirstIncoming("pobj_by");
        frameObj.expandA0 = false;
        if (frameObj.a0 != null) {
            frameObj.a0 = Iterables.getFirst(frameObj.a0.annotations(Types.PHRASE_CHUNK), frameObj.a0); //EXPAND TO PHRASE CHUNK
        }
        frameObj.a1 = frameObj.getFirstIncoming("nsubjpass");
        if (frameObj.a2 == null && frameObj.hasIncomingDependency("iobj")) {
            frameObj.a2 = frameObj.getFirstIncoming("iobj");
        }
    }

    private void processCopula(VerbFrame frameObj) {
        frameObj.a1 = Iterables.getFirst(frameObj.frame.parent().annotations(Types.PHRASE_CHUNK), frameObj.frame.parent());
        frameObj.a0 = Iterables.getFirst(frameObj.a1.incoming(Types.DEPENDENCY, "nsubj"),
                                         Iterables.getFirst(frameObj.a1.incoming(Types.DEPENDENCY, "csubj"), null));
        if (frameObj.a1.hasIncomingRelation(Types.DEPENDENCY, "dobj")) {
            frameObj.a2 = Iterables.getFirst(frameObj.a1.incoming(Types.DEPENDENCY, "dobj"), null);
        } else if (frameObj.a1.hasIncomingRelation(Types.DEPENDENCY, "xcomp")) {
            frameObj.a2 = Iterables.getFirst(frameObj.a1.incoming(Types.DEPENDENCY, "xcomp"), null);
        }
        if (frameObj.a2 == null && frameObj.hasIncomingDependency("iobj")) {
            frameObj.a2 = frameObj.getFirstIncoming("iobj");
        }
    }

    private Annotation getFirstPhraseChunk(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        return Iterables.getFirst(annotation.annotations(Types.PHRASE_CHUNK), null);
    }

    private void processSVO(VerbFrame frameObj) {
        frameObj.a0 = frameObj.getFirstIncoming("nsubj");

        if (frameObj.a0 == null && frameObj.hasOutgoingDependency("xcomp")) {
            //Get the A0 as the nsubj of the xcomp
            Annotation xcomp = frameObj.getFirstOutgoing("xcomp");
            frameObj.a0 = Iterables.getFirst(xcomp.incoming(Types.DEPENDENCY, "nsubj"), null);
        } else if (frameObj.a0 == null && frameObj.hasOutgoingDependency("infmod")) {
            //Get the A0 as the infmod
            frameObj.a0 = getFirstPhraseChunk(frameObj.getFirstOutgoing("infmod"));
            frameObj.expandA0 = false;
        } else if (frameObj.a0 == null && frameObj.hasOutgoingDependency("conj")) {
            //Get the A0 as the nsubj of the conj's ccomp
            Annotation conj = frameObj.getFirstOutgoing("conj");
            if (conj.hasOutgoingRelation(Types.DEPENDENCY, "ccomp")) {
                Annotation ccomp = Iterables.getFirst(conj.outgoing(Types.DEPENDENCY, "ccomp"), null);
                frameObj.a0 = Iterables.getFirst(ccomp.incoming(Types.DEPENDENCY, "nsubj"), null);
            } else if (conj.hasIncomingRelation(Types.DEPENDENCY, "nsubj")) {
                //A0 as conj's nsubj
                frameObj.a0 = Iterables.getFirst(conj.incoming(Types.DEPENDENCY, "nsubj"), null);
            }
        }

        if (frameObj.hasIncomingDependency("dobj")) {
            frameObj.a1 = frameObj.getFirstIncoming("dobj");
            frameObj.a2 = frameObj.getFirstIncoming("xcomp");
        } else if (frameObj.hasIncomingDependency("xcomp")) {
            frameObj.a1 = frameObj.getFirstIncoming("xcomp");
        } else if (frameObj.hasIncomingDependency("ccomp")) {
            frameObj.a1 = frameObj.getFirstIncoming("ccomp");
        } else if (frameObj.hasIncomingDependency("pobj_to")) {
            frameObj.a1 = frameObj.getFirstIncoming("pobj_to");
        } else if (frameObj.hasIncomingDependency("advcl")) {
            frameObj.a1 = frameObj.getFirstIncoming("advcl");
        } else {
            frameObj.a1 = frameObj.getFirstIncoming("dep");
        }

        if (frameObj.a2 == null && frameObj.hasIncomingDependency("iobj")) {
            frameObj.a2 = frameObj.getFirstIncoming("iobj");
        }
    }

    @Override
    protected void annotate(Annotation sentence) {
        RelationGraph rg = sentence.dependencyGraph();
        for (Annotation chunk : sentence.annotations(Types.PHRASE_CHUNK)) {
            if (chunk.pos().isVerb()) {
                chunk = Iterables.getFirst(chunk.annotations(Types.MWE), chunk);
                var frameObj = new VerbFrame(chunk);

                if (frameObj.isPassive()) {
                    processPassive(frameObj);
                } else if (frameObj.isCopula()) {
                    processCopula(frameObj);
                } else {
                    processSVO(frameObj);
                }
                //The frame will be the head of the verb phrase + any particles
                frameObj.expand(rg);
                Annotation frame = frameObj.attachFrame();
                Annotation a0 = frameObj.attachA0(frame);
                Annotation a1 = frameObj.attachA1(frame);
                Annotation a2 = frameObj.attachA2(frame);
                handleNegation(chunk, frame);
                handlePP(frameObj, chunk, frame, a1, rg);
                handleTMod(frameObj, frame, rg);
            }
        }
    }

    private void handleTMod(VerbFrame frameObj, Annotation frame, RelationGraph rg) {
        if (frameObj.hasIncomingDependency("tmod")) {
            var tmod = getSubTree(frameObj.getFirstIncoming("tmod"), rg);
            tmod = frame.document().createAnnotation(Types.LIGHT_FRAME_ARG,
                                                     tmod.start(),
                                                     tmod.end(),
                                                     Map.of());
            tmod.add(new Relation(Types.LIGHT_FRAME_ROLE, "time", frame.getId()));
        }
    }

    private void handlePP(VerbFrame frameObj, Annotation chunk, Annotation frame, Annotation a1, RelationGraph rg) {
        Annotation obj = frameObj.isCopula() && a1 != null ? a1 : chunk;
        for (Relation rel : obj.incomingRelations(Types.DEPENDENCY)) {
            if (rel.getValue().startsWith("pcomp_")) {
                processPComp(rel, frame, frame.document(), rg);
            }
            if (rel.getValue().startsWith("pobj_")) {
                processPObj(rel, frame, frame.document(), rg);
            }
        }
    }

    private void handleNegation(Annotation chunk, Annotation frame) {
        if (chunk.hasIncomingRelation(Types.DEPENDENCY, "neg") || frame.hasIncomingRelation(Types.DEPENDENCY, "neg")) {
            frame.put(Types.IS_NEGATED, true);
        }
    }

    private void processPComp(Relation rel, Annotation frame, Document doc, RelationGraph rg) {
        Annotation target = rel.getTarget(doc);
        Annotation vp = Iterables.getFirst(target.annotations(Types.PHRASE_CHUNK), null);
        if (vp == null) {
            return;
        }
        vp = doc.createAnnotation(
                Types.LIGHT_FRAME_ARG,
                vp.start(),
                vp.end(),
                Collections.emptyMap()
                                 );
        vp.add(new Relation(Types.LIGHT_FRAME_ROLE, rel.getValue(), frame.getId()));
    }

    private void processPObj(Relation rel, Annotation frame, Document doc, RelationGraph rg) {
        Annotation target = rel.getTarget(doc);
        target = Iterables.getFirst(target.annotations(Types.PHRASE_CHUNK), null);
        if (target == null) {
            return;
        }
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
        } else {
            processSubPObj(target, frame, doc, rg);
        }

        //Check if the part after the _ is in the frame and if so treat is as the dobj?
        if (role.indexOf('_') >= 0) {
            String prep = role.substring(role.indexOf('_') + 1);
            if (frame.toString().contains(prep)) {
                role = "a1";
            }
        }

        target.add(new Relation(Types.LIGHT_FRAME_ROLE, role, frame.getId()));
    }

    private void processSubPObj(Annotation parent, Annotation frame, Document doc, RelationGraph rg) {
        for (Relation ir : parent.incomingRelations()) {
            if (ir.getValue().startsWith("pobj_")) {
                Annotation subphrase = getSubTree(ir.getTarget(doc), rg);
                boolean isSTime = subphrase.annotations(Types.ENTITY)
                                           .stream()
                                           .anyMatch(e -> e.getTag().isInstance(Entities.DATE));
                boolean isSLocation = subphrase.annotations(Types.ENTITY)
                                               .stream()
                                               .anyMatch(e -> e.getTag().isInstance(Entities.LOCATION) || e.getTag().isInstance(Entities.FACILITY));
                subphrase = doc.createAnnotation(
                        Types.LIGHT_FRAME_ARG,
                        subphrase.start(),
                        subphrase.end(),
                        Collections.emptyMap());
                if (isSTime || isSLocation) {
                    subphrase.add(new Relation(Types.LIGHT_FRAME_ROLE, isSLocation ? "location" : "time", frame.getId()));
                } else {
                    subphrase.add(new Relation(Types.LIGHT_FRAME_ROLE, ir.getValue(), frame.getId()));
                }
            }
        }
    }

    @Data
    private static class VerbFrame {
        Annotation frame;
        Annotation a0;
        Annotation a1;
        Annotation a2;
        boolean expandA0 = true;
        boolean expandA1 = true;
        boolean expandA2 = true;

        public VerbFrame(Annotation frame) {
            this.frame = frame;
        }

        public Annotation attachFrame() {
            HString head = frame.head();
            if (head.hasIncomingRelation(Types.DEPENDENCY, "prt")) {
                head = HString.union(head, Iterables.getFirst(head.incoming(Types.DEPENDENCY, "prt"), null));
            }
            if (!head.annotations(Types.MWE).isEmpty()) {
                head = head.annotations(Types.MWE).get(0);
            }
            return frame.document().createAnnotation(Types.LIGHT_FRAME,
                                                     head.start(),
                                                     head.end(),
                                                     Map.of(Types.TAG, new StringTag("VERB_FRAME")));
        }

        public Annotation attachA0(Annotation frame) {
            if (a0 != null) {
                Annotation tempA0 = frame.document().createAnnotation(
                        Types.LIGHT_FRAME_ARG,
                        a0.start(),
                        a0.end(),
                        Collections.emptyMap()
                                                                     );
                tempA0.add(new Relation(Types.LIGHT_FRAME_ROLE, "a0", frame.getId()));
                return tempA0;
            }
            return null;
        }

        public boolean hasAtLest1Arg() {
            return a0 != null || a1 != null || a2 != null;
        }

        public Annotation attachA1(Annotation frame) {
            if (a1 != null) {
                Annotation tempA1 = frame.document().createAnnotation(
                        Types.LIGHT_FRAME_ARG,
                        a1.start(),
                        a1.end(),
                        Collections.emptyMap()
                                                                     );
                tempA1.add(new Relation(Types.LIGHT_FRAME_ROLE, "a1", frame.getId()));
                return tempA1;
            }
            return null;
        }

        public Annotation attachA2(Annotation frame) {
            if (a2 != null) {
                Annotation tempA2 = frame.document().createAnnotation(
                        Types.LIGHT_FRAME_ARG,
                        a2.start(),
                        a2.end(),
                        Collections.emptyMap()
                                                                     );
                tempA2.add(new Relation(Types.LIGHT_FRAME_ROLE, "a2", frame.getId()));
                return tempA2;
            }
            return null;
        }

        public boolean isPassive() {
            return frame.hasIncomingRelation(Types.DEPENDENCY, "nsubjpass");
        }

        public boolean isCopula() {
            return frame.hasOutgoingRelation(Types.DEPENDENCY, "cop");
        }

        public boolean hasIncomingDependency(String value) {
            return frame.hasIncomingRelation(Types.DEPENDENCY, value);
        }

        public boolean hasOutgoingDependency(String value) {
            return frame.hasOutgoingRelation(Types.DEPENDENCY, value);
        }

        public Annotation getFirstIncoming(String value) {
            return Iterables.getFirst(frame.incoming(Types.DEPENDENCY, value), null);
        }

        public Annotation getFirstOutgoing(String value) {
            return Iterables.getFirst(frame.outgoing(Types.DEPENDENCY, value), null);
        }

        public void expand(RelationGraph rg) {
            if (expandA0) a0 = getSubTree(a0, rg);
            if (expandA1) a1 = getSubTree(a1, rg);
            if (expandA2) a2 = getSubTree(a2, rg);
        }
    }

}
