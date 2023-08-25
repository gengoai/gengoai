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
 *
 */

package com.gengoai.hermes.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gengoai.config.Config;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>An action defines a processing step to perform on a {@link Corpus} with a given {@link Context} which results in
 * either modifying the corpus or the context. Action implementations can persist their state to be reused at a later
 * time including across jvm instances & runs. This is done by implementing the {@link
 * #loadPreviousState(DocumentCollection, Context)} method. An action can ignore its state and reprocess the corpus when
 * either the config setting  <code>processing.override.all</code> is set to true or the config setting
 * <code>className.override</code> is set tp true.
 * </p>
 *
 * @author David B. Bracewell
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface Action extends Serializable {

    /**
     * Gets the name of the Action.
     *
     * @return The name of the action
     */
    String getName();

    String getId();

    void setId(String id);

    /**
     * Gets the Description of the Action.
     *
     * @return The Description of the action
     */
    String getDescription();

    /**
     * Process corpus.
     *
     * @param corpus  the corpus
     * @param context the context
     * @throws Exception the exception
     */
    DocumentCollection process(DocumentCollection corpus, Context context) throws Exception;


    default void saveActionState(Resource actionFolder) throws IOException {
        final Resource saveLocation = actionFolder.getChild(getId());
        saveLocation.mkdirs();
        saveLocation.getChild("action.json").write(Json.dumpsPretty(this));
    }

}//END OF Action
