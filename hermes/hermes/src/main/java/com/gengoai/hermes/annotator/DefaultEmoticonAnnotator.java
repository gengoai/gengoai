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

package com.gengoai.hermes.annotator;

import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.lexicon.Lexicon;
import com.gengoai.hermes.lexicon.LexiconIO;
import com.gengoai.io.Resources;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class DefaultEmoticonAnnotator extends Annotator {
    final Lexicon emoji;

    public DefaultEmoticonAnnotator() {
        try {
            this.emoji = LexiconIO.read("emoji", Resources.fromClasspath("com/gengoai/hermes/lexicon/emoji.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void annotateImpl(Document document) {
        for (HString match : emoji.extract(document)) {
            String[] group_info = match.attribute(Types.MATCHED_TAG).split("::");
            document.createAnnotation(Types.EMOTICON,
                                      match.start(),
                                      match.end(),
                                      Map.of(
                                              Types.EMOTICON_GROUP, group_info[0],
                                              Types.EMOTICON_SUB_GROUP, group_info[1]
                                            ));

        }
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.EMOTICON);
    }

    @Override
    public Set<AnnotatableType> requires() {
        return Set.of(Types.TOKEN);
    }
}//END OF DefaultEmoticonAnnotator
