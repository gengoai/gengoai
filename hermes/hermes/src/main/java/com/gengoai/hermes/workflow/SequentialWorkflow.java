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

import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.LogUtils;
import com.gengoai.Stopwatch;
import com.gengoai.application.Option;
import com.gengoai.collection.Lists;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.gengoai.LogUtils.logInfo;

/**
 * <p>Entry point to sequentially processing a corpus via one ore more {@link Action}s. The list of
 * processors can be defined either via the <code>--processors</code> command line argument (which expects a comma
 * separated list of processor class names) or via the <code>--desc</code> argument, which specifies the processing
 * description file to load.</p>
 *
 * <p>Description files are in Mango's <code>Config</code> format. Individual {@link Action}s are implemented
 * as beans, which can have their options set via configuration using Mango's capability to parameterize objects.</p>
 *
 * @author David B. Bracewell
 */
@ToString
public final class SequentialWorkflow implements Workflow {
    public static final String TYPE = "Sequential";
    private static final long serialVersionUID = 1L;
    @Option(description = "List of actions to perform run")
    private List<Action> actions = new ArrayList<>();
    private Context startingContext = new Context();

    /**
     * Instantiates a new Workflow.
     *
     * @param actions the processors
     */
    public SequentialWorkflow(@NonNull Iterable<Action> actions) {
        this.actions = Lists.asArrayList(actions);
    }

    @Override
    public Context getStartingContext() {
        return startingContext.copy();
    }

    @Override
    public void setStartingContext(Context context) {
        this.startingContext = context.copy();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Process corpus.
     *
     * @param input   the input
     * @param context the context
     * @return the corpus
     * @throws Exception the exception
     */
    public DocumentCollection process(@NonNull DocumentCollection input,
                                      @NonNull Context context) throws
                                                                Exception {
        DocumentCollection corpus = input;
        context.merge(startingContext);
        Stopwatch sw = Stopwatch.createStarted();
        for (Action processor : actions) {
            Stopwatch actionTime = Stopwatch.createStarted();
            logInfo(LogUtils.getLogger(getClass()),
                    "Running {0} [{1}]...", processor.getClass().getSimpleName(), processor.getId());
            corpus = processor.process(corpus, context);
            logInfo(LogUtils.getLogger(getClass()),
                    "Completed {0} [{1}] ({2})",
                    processor.getClass().getSimpleName(),
                    processor.getId(),
                    actionTime);
        }

        logInfo(LogUtils.getLogger(getClass()), "Completed Workflow in " + sw);
        return corpus;
    }

    @JsonValue
    protected JsonEntry toEntry() {
        JsonEntry obj = BaseWorkflowIO.serialize(this);
        JsonEntry actionArray = JsonEntry.array();
        for (Action action : actions) {
            JsonEntry ao = JsonEntry.object();
            ao.mergeObject(Json.asJsonEntry(action));
            actionArray.addValue(ao);
        }
        obj.addProperty("actions", actionArray);
        return obj;
    }

}//END OF Controller
