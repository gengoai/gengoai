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

import com.gengoai.apollo.data.DataSetGenerator;
import com.gengoai.apollo.data.transform.Transform;
import com.gengoai.apollo.model.Model;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.PipelineModel;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFModel;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.application.CommandLineApplication;
import com.gengoai.application.Option;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.ml.HStringDataSetGenerator;
import com.gengoai.hermes.ml.HStringMLModel;
import com.gengoai.io.resource.Resource;

import java.util.HashMap;
import java.util.Map;

public class ExplainModel extends CommandLineApplication {

   @Option(description = "The model to explain")
   private Resource model;

   @Override
   protected void programLogic() throws Exception {
      Model m = ModelIO.load(model);

      String version = null;
      String annotationType = null;
      Map<String, String> generators = new HashMap<>();
      Map<String, String> encoders = new HashMap<>();

      if (m instanceof HStringMLModel) {
         HStringMLModel hStringMLModel = Cast.as(m);
         HStringDataSetGenerator dataSetGenerator = hStringMLModel.getDataGenerator();
         version = hStringMLModel.getVersion();
         annotationType = dataSetGenerator.getDatumAnnotationType().name();
         for (DataSetGenerator.GeneratorInfo<HString> generator : dataSetGenerator.getGenerators()) {
            if (generator.getExtractor().toString() != null) {
               generators.put(generator.getName(), generator.getExtractor().toString());
            }
         }
         m = hStringMLModel.delegate();
      }

      if (m instanceof PipelineModel) {
         PipelineModel pipelineModel = Cast.as(m);
         for (Transform transform : pipelineModel.getTransformer().getTransforms()) {
            System.out.println(transform);
         }
         System.out.println(pipelineModel.getTransformer().getMetadata());
      }

      if (m instanceof TFModel) {
         TFModel tfModel = Cast.as(m);
         for (TFInputVar inputVar : tfModel.getInputVars()) {
            encoders.put(inputVar.getName(), inputVar.getEncoder().toString());
         }
         for (TFOutputVar outputVar : tfModel.getOutputVars()) {
            encoders.put(outputVar.getName(), outputVar.getEncoder().toString());
         }
      }

      System.out.println(m.getClass().getName());
      System.out.println("File: " + model);
      if (version != null) {
         System.out.println("Version: '" + version + "'");
      }
      if (annotationType != null) {
         System.out.println("AnnotationType: '" + annotationType + "'");
      }
      System.out.println();
      System.out.println("Inputs");
      System.out.println("=====================================");
      for (String input : m.getInputs()) {
         System.out.println(input + " " + generators.getOrDefault(input, "").replaceAll("\n", "\n\t"));
      }
      System.out.println();
      System.out.println("Outputs");
      System.out.println("=====================================");
      for (String output : m.getOutputs()) {
         System.out.println(output + " [" + m.getLabelType(output) + "] " + generators.getOrDefault(output, ""));
      }
      System.out.println();
      System.out.println("Encoders");
      System.out.println("=====================================");
      for(String var : encoders.keySet()){
         System.out.println(var + "\t" + encoders.get(var));
      }
   }

   public static void main(String[] args) {
      new ExplainModel().run(args);
   }

}//END OF ExplainModel
