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

import com.gengoai.Language;
import com.gengoai.hermes.*;
import com.gengoai.hermes.annotator.Annotator;
import com.gengoai.hermes.lexicon.Lexicon;

import java.util.List;
import java.util.Set;

public class ENEmpathAnnotator extends Annotator {
    final Lexicon lexicon;

    public ENEmpathAnnotator() {
        this.lexicon = ResourceType.LEXICON.load("empath", Language.ENGLISH);
    }


    @Override
    protected void annotateImpl(Document document) {
        for (HString match : lexicon.extract(document)) {
            match.firstToken().putAdd(Types.EMPATH_CATEGORY, List.of(match.attribute(Types.MATCHED_TAG)));
        }
    }

    @Override
    public Set<AnnotatableType> requires() {
        return Set.of(Types.TOKEN, Types.LEMMA);
    }

    @Override
    public Set<AnnotatableType> satisfies() {
        return Set.of(Types.EMPATH_CATEGORY);
    }

}//END OF ENEmpathAnnotator
