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

package com.gengoai.hermes.workflow.actions;

import com.gengoai.collection.Lists;
import com.gengoai.hermes.AnnotatableType;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.workflow.Action;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.string.Strings;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.kohsuke.MetaInfServices;

import java.util.Arrays;
import java.util.List;

import static com.gengoai.LogUtils.logConfig;

/**
 * The type Annotate processor.
 *
 * @author David B. Bracewell
 */
@Log
@MetaInfServices
@Data
public class Annotate implements Action {
    public static final String ANNOTATABLE_TYPE_CONFIG = "annotate.types";
    private static final long serialVersionUID = 1L;
    private List<String> types;

    @Override
    public String getName() {
        return "ANNOTATE";
    }

    @Override
    public String getDescription() {
        return "Action to annotate the document collection with a set of AnnotatableTypes. " +
                "To specify add a 'types' property to your json definition with an array of AnnotatableType names " +
                "or set the context value 'ANNOTATE_TYPES' on the command line (comma separated list). " +
                "If no types are specified, then the types defined in `Types.BASE_ANNOTATIONS` are used. " +
                "\n\nVia Workflow Json:\n" +
                "--------------------------------------\n" +
                "{\n" +
                "   \"@type\"=\"" + Annotate.class.getName() + "\",\n" +
                "   \"types\"=[\"TOKEN\", \"SENTENCE\"]\n" +
                "}" +
                "\n\nVia Context:\n" +
                "--------------------------------------\n" +
                "ANNOTATE_TYPES=\"TOKEN\",\"SENTENCE\"";
    }

    @Override
    public DocumentCollection process(@NonNull DocumentCollection corpus, @NonNull Context context) throws Exception {
        AnnotatableType[] aTypes = Types.BASE_ANNOTATIONS;

        String contextTypes = context.getString(ANNOTATABLE_TYPE_CONFIG);
        if (Strings.isNotNullOrBlank(contextTypes)) {
            aTypes = Strings.split(contextTypes, ',')
                            .stream()
                            .map(AnnotatableType::valueOf)
                            .toArray(AnnotatableType[]::new);
        } else if (types != null) {
            aTypes = Lists.transform(types, AnnotatableType::valueOf).toArray(new AnnotatableType[0]);
        }

        logConfig(log, "Annotating corpus for {0}", Arrays.toString(aTypes));
        return corpus.annotate(aTypes);
    }

}//END OF AnnotateProcessor
