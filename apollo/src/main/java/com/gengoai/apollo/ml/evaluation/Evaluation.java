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

package com.gengoai.apollo.ml.evaluation;

import com.gengoai.LogUtils;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import lombok.NonNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The interface Evaluation.
 *
 * @author David B. Bracewell
 */
public interface Evaluation {

   /**
    * Evaluates the given {@link Model} on the given {@link DataSet}.
    *
    * @param model   the model to evaluate
    * @param dataset the dataset to evaluate over
    */
   void evaluate(@NonNull Model model, @NonNull DataSet dataset);

   /**
    * Outputs the results of the classification to the given <code>PrintStream</code>
    *
    * @param printStream the print stream to write to
    */
   void output(PrintStream printStream);

   /**
    * Outputs the evaluation results to standard out.
    */
   default void output() {
      output(System.out);
   }

   /**
    * Outputs the results of the classification to the given logger.
    *
    * @param logger   the logger to use for logging
    * @param logLevel The level to log at
    */
   default void report(@NonNull Logger logger, @NonNull Level logLevel) {
      Resource r = new StringResource();
      try(PrintStream ps = new PrintStream(r.outputStream())) {
         ps.println();
         output(ps);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      try {
         LogUtils.log(logger, logLevel, r.readToString());
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Outputs the results of the classification to the global logger at level INFO.
    */
   default void report() {
      report(LogUtils.getGlobalLogger(), Level.INFO);
   }

}//END OF Evaluation
