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

import com.gengoai.Stopwatch;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.evaluation.ClassifierEvaluation;
import com.gengoai.apollo.evaluation.Evaluation;
import com.gengoai.apollo.evaluation.SequenceLabelerEvaluation;
import com.gengoai.apollo.model.FitParameters;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.application.Option;
import com.gengoai.config.Config;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.ml.HStringMLModel;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import com.gengoai.string.Strings;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.gengoai.LogUtils.logInfo;

@Log
public class TaggerApp extends HermesCLI {
   private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

   @Option(description = "The task to execute (specified in your custom .conf file)")
   private String task;
   @Option(description = "The action to perform (TRAIN|TEST|PRODUCTION|PARAMETERS)")
   private String action;

   @Option(description = "Print a Confusion Matrix",
         defaultValue = "false")
   private boolean printCM;

   public static void main(String[] args) throws Exception {
      new TaggerApp().run(args);
   }


   protected FitParameters<?> getFitParameters(HStringMLModel model,
                                               String task) {
      FitParameters<?> parameters = model.getFitParameters();
      for (String parameterName : parameters.parameterNames()) {
         String confName = task + ".param." + parameterName;
         if (Config.hasProperty(confName)) {
            parameters.set(parameterName, Config.get(confName).as(parameters.getParam(parameterName).type));
         }
      }
      return parameters;
   }

   private void logFitParameters(FitParameters<?> parameters) {
      logInfo(log, "========================================================");
      logInfo(log, "                 FitParameters");
      logInfo(log, "========================================================");
      for (String name : parameters.parameterNames()) {
         logInfo(log,
                 "{0} ({1}), value={2}",
                 name,
                 parameters.getParam(name).type.getSimpleName(),
                 parameters.get(name));
      }
      logInfo(log, "========================================================");
   }

   protected void test(String task) throws Exception {
      task = task.toUpperCase();
      action = action.toLowerCase();
      HStringMLModel model = ModelIO.load(Config.get(task + ".model").asResource());
      String documentCollectionSpec = Config.get(task + "." + action).asString();
      String query = Config.get(task + "." + action + ".query").asString();

      logInfo(log, "========================================================");
      logInfo(log, "                         TEST");
      logInfo(log, "========================================================");
      logInfo(log, "   Data: {0}", documentCollectionSpec);
      logInfo(log, " Tagger: {0}", model);
      logInfo(log, "========================================================");
      logInfo(log, "Loading data set");

      DocumentCollection docs = DocumentCollection.create(documentCollectionSpec);
      if (Strings.isNotNullOrBlank(query)) {
         docs = docs.query(query);
      }
      DataSet testingData = model.transform(docs);
      Evaluation evaluation = model.getEvaluator();
      Stopwatch stopwatch = Stopwatch.createStarted();
      logInfo(log, "Testing Started at {0}", LocalDateTime.now().format(TIME_FORMATTER));
      evaluation.evaluate(model.delegate(), testingData);
      stopwatch.stop();
      logInfo(log, "Testing Stopped at {0} ({1})",
              LocalDateTime.now().format(TIME_FORMATTER),
              stopwatch);

      Resource stdOut = new StringResource();
      try (OutputStream os = stdOut.outputStream();
           PrintStream printStream = new PrintStream(os)) {
         if (evaluation instanceof SequenceLabelerEvaluation) {
            ((SequenceLabelerEvaluation) evaluation).output(printStream, printCM);
         } else if (evaluation instanceof ClassifierEvaluation) {
            ((ClassifierEvaluation) evaluation).output(printStream, printCM);
         } else {
            evaluation.output(printStream);
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      try {
         logInfo(log, "\n{0}", stdOut.readToString());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected void train(String task, String action) throws Exception {
      task = task.toUpperCase();
      action = action.toLowerCase();

      HStringMLModel model = Config.get(task).as(HStringMLModel.class);
      FitParameters<?> parameters = getFitParameters(model, task);
      String documentCollectionSpec = Config.get(task + "." + action).asString();
      String query = Config.get(task + "." + action + ".query").asString();

      DocumentCollection docs = DocumentCollection.create(documentCollectionSpec);
      if (Strings.isNotNullOrBlank(query)) {
         docs = docs.query(query);
      }
      DataSet trainingData = docs.asDataSet(model.getDataGenerator());

      logInfo(log, "========================================================");
      logInfo(log, "                         Train");
      logInfo(log, "========================================================");
      logInfo(log, "   Data: {0}", documentCollectionSpec);
      if (Strings.isNotNullOrBlank(query)) {
         logInfo(log, "  Query: {0}", query);
      }
      logInfo(log, "Trainer: {0}", Config.get(task));
      logInfo(log, "  Model: {0}", model);
      logInfo(log, "========================================================");
      logFitParameters(parameters);
      Stopwatch stopwatch = Stopwatch.createStarted();
      logInfo(log,
              "Training Started at {0}",
              LocalDateTime.now().format(TIME_FORMATTER));
      model.estimate(trainingData);
      stopwatch.stop();
      logInfo(log, "Training Stopped at {0} ({1})",
              LocalDateTime.now().format(TIME_FORMATTER),
              stopwatch);

      ModelIO.save(model, Config.get(task + ".model").asResource());
   }

   @Override
   protected void programLogic() throws Exception {
      switch (action.toUpperCase()) {
         case "TRAIN":
         case "PRODUCTION":
            train(task, action);
            break;
         case "TEST":
            test(task);
            break;
      }
   }
}
