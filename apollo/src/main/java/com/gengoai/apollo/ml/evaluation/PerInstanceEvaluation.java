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

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.observation.Variable;
import com.gengoai.apollo.ml.observation.VariableSequence;
import com.gengoai.conversion.Cast;
import lombok.NonNull;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * <p>Sequence labeling evaluation that evaluates each item in the sequence (i.e. instance) independently.</p>
 *
 * @author David B. Bracewell
 */
public class PerInstanceEvaluation implements SequenceLabelerEvaluation, Serializable {
   private static final long serialVersionUID = 1L;
   private final MultiClassEvaluation eval;
   private final String outputName;

   /**
    * Instantiates a new PerInstanceEvaluation.
    *
    * @param outputName the name of the output source we will evaluate
    */
   public PerInstanceEvaluation(String outputName) {
      this.outputName = outputName;
      this.eval = new MultiClassEvaluation(outputName);
   }

   @Override
   public void evaluate(@NonNull Model model, @NonNull DataSet dataset) {
      dataset.forEach(sequence -> {
         VariableSequence gold = toVariableSequence(sequence.get(outputName));
         VariableSequence pred = toVariableSequence(model.transform(sequence).get(outputName));
         for(int i = 0; i < gold.size(); i++) {
            eval.entry(gold.get(i).getName(), pred.get(i).getName());
         }
      });
   }

   @Override
   public void merge(@NonNull SequenceLabelerEvaluation evaluation) {
      Validation.checkArgument(evaluation instanceof PerInstanceEvaluation);
      eval.merge(Cast.<PerInstanceEvaluation>as(evaluation).eval);
   }

   @Override
   public void output(@NonNull PrintStream printStream, boolean printConfusionMatrix) {
      eval.output(printStream, printConfusionMatrix);
   }

   private VariableSequence toVariableSequence(Observation observation) {
      if(observation instanceof VariableSequence) {
         return Cast.as(observation);
      } else if(observation.isNDArray()) {
         VariableSequence vs = new VariableSequence();
         NDArray ndArray = observation.asNDArray();
         for(int i = 0; i < ndArray.rows(); i++) {
            NDArray row = ndArray.getRow(i);
            if(ndArray.columns() == 1) {
               vs.add(Variable.binary(Double.toString(row.get(0))));
            } else {
               vs.add(Variable.binary(Double.toString(row.argmax())));
            }
         }
         return vs;
      }
      throw new IllegalArgumentException(observation.getClass() + " is not supported");
   }

}//END OF PerInstanceEvaluation
