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

package com.gengoai.hermes.tools;

import com.gengoai.LogUtils;
import com.gengoai.application.Option;
import com.gengoai.config.Config;
import com.gengoai.hermes.HermesCLI;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.workflow.BaseWorkflowIO;
import com.gengoai.hermes.workflow.Context;
import com.gengoai.hermes.workflow.Workflow;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.specification.Specification;
import com.gengoai.string.Strings;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.gengoai.LogUtils.logSevere;
import static com.gengoai.hermes.workflow.Workflow.*;

@Log
public class WorkflowApp extends HermesCLI {
    /**
     * Name of the context parameter for the location of the input corpus
     */
    public static final String INPUT_LOCATION = "inputLocation";
    private static final long serialVersionUID = 1L;
    @Option(name = "workflow", description = "The location of the workflow folder", aliases = {"w"}, required = true)
    private Resource workflowFolder;
    @Option(description = "The specification or location the document collection to process.", aliases = {"i", "corpus"})
    private String input;

    @Option(description = "The workflow stage to perform (i.e name of the workflow.json)")
    private String stage;

    public static void main(String[] args) throws Exception {
        new WorkflowApp().run(args);
    }

    public DocumentCollection getDocumentCollection(Resource workflowFolder) {
        if (Strings.isNullOrBlank(input)) {
            LogUtils.logInfo(log, "No input collection specified, looking for corpus in workflow folder");
            return Corpus.open(workflowFolder.getChild("corpus"));
        }
        try {
            Specification.parse(input);
            LogUtils.logInfo(log, "Input collection specified as ''{0}'', creating collection", input);
            return DocumentCollection.create(input);
        } catch (Exception e) {
            LogUtils.logInfo(log, "Input corpus specified as ''{0}'', opening corpus", input);
            return Corpus.open(input);
        }
    }

    private void loadContext(Resource location, Context context) throws IOException {
        if (location != null && location.exists()) {
            Context toMerge = Json.parse(location, Context.class);
            toMerge.remove(INPUT_LOCATION);
            toMerge.remove(CONTEXT_OUTPUT);
            context.merge(toMerge);
        }
    }

    private void ensurePositionalArgument(int length, String message) {
        if (getPositionalArgs().length <= length) {
            logSevere(log, message);
            System.exit(-1);
        }
    }


    private void newWorkFlow() throws Exception {
        if (workflowFolder.exists()) {
            throw new RuntimeException("Error: " + workflowFolder.path() + " already exists");
        }
        workflowFolder.mkdirs();
        workflowFolder.getChild("workflow.conf").write("");
        workflowFolder.getChild("workflow.json").write(Json.dumpsPretty(Map.of(
                "@type", "Sequential",
                "actions", Collections.emptyList()
                                                                              )));
    }

    private void cleanWorkFlow() throws Exception {
        if (workflowFolder.exists()) {
            workflowFolder.getChildren("*.log").forEach(Resource::delete);
            workflowFolder.getChildren("*.log.lck").forEach(Resource::delete);
            workflowFolder.getChildren("*.output.json.gz").forEach(Resource::delete);
            workflowFolder.getChild("actions").delete(true);
            workflowFolder.getChild("analysis").delete(true);
            workflowFolder.getChild("corpus").delete(true);
        }
    }


    private void runWorkflow() throws Exception {
        if (!workflowFolder.exists()) {
            throw new RuntimeException("Error: " + workflowFolder.path() + " does not exist");
        }
        Config.setProperty("com.gengoai.logging.dir", workflowFolder.path());

        Resource contextOutputLocation = workflowFolder.getChild("workflow.output.json.gz");
        final Resource actionsFolder = workflowFolder.getChild("actions");
        final Resource analysisFolder = workflowFolder.getChild("analysis");
        final Resource workflowConf = workflowFolder.getChild("workflow.conf");
        if (workflowConf.exists()) {
            Config.loadConfig(workflowConf);
        }


        if (Strings.isNotNullOrBlank(stage)) {
            contextOutputLocation = workflowFolder.getChild(stage + ".output.json.gz");
            LogUtils.addFileHandler(stage);

            final Resource stageConf = workflowFolder.getChild(stage + ".conf");
            if (stageConf.exists()) {
                Config.loadConfig(stageConf);
            }
        } else {
            LogUtils.addFileHandler("workflow");
        }


        if (input == null && Config.hasProperty("context.inputLocation")) {
            input = Config.get("context.inputLocation").asString();
        }

        if (input == null && !workflowFolder.getChild("corpus").exists()) {
            throw new IllegalArgumentException("Error: No input collection specified and no corpus found in workflow folder.");
        }

        actionsFolder.mkdirs();

        Context context = Context.builder()
                                 .property(INPUT_LOCATION, input)
                                 .property(CONTEXT_OUTPUT, contextOutputLocation)
                                 .property(WORKFLOW_FOLDER, workflowFolder)
                                 .property(ACTIONS_FOLDER, actionsFolder)
                                 .property(ANALYSIS_FOLDER, analysisFolder)
                                 .property(STAGE, stage)
                                 .build();


        actionsFolder.mkdirs();
        analysisFolder.mkdirs();

        loadContext(workflowFolder.getChild("workflow.input.json"), context);
        Config.getPropertiesMatching(s -> s.startsWith(CONTEXT_ARG))
              .forEach(key -> {
                  var contextName = key.substring(CONTEXT_ARG.length());
                  context.property(contextName, Config.get(key).get());
                  LogUtils.logInfo(log, "Setting Context property {0} to {1}", contextName, Config.get(key));
              });

        Workflow workflow;
        if (Strings.isNotNullOrBlank(stage)) {
            loadContext(workflowFolder.getChild(stage + ".input.json"), context);
            workflow = BaseWorkflowIO.read(workflowFolder.getChild(stage + ".json"));
        } else {
            workflow = BaseWorkflowIO.read(workflowFolder.getChild("workflow.json"));
        }

        try (DocumentCollection inputCorpus = getDocumentCollection(workflowFolder)) {
            try (DocumentCollection outputCorpus = workflow.process(inputCorpus, context)) {
                contextOutputLocation.compressed().write(Json.dumpsPretty(context));
            }
        }
    }

    @Override
    protected void programLogic() throws Exception {

        ensurePositionalArgument(0, "No Operation Given!");
        final String operation = getPositionalArgs()[0];
        switch (operation.toUpperCase()) {
            case "NEW":
                newWorkFlow();
                break;
            case "CLEAN":
                cleanWorkFlow();
                break;
            case "RUN":
                runWorkflow();
                break;
            default:
                throw new IllegalArgumentException("Error: Invalid Operation '" + operation + "'");
        }


    }

}//END OF Runner
