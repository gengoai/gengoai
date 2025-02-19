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

package com.gengoai.hermes.ml.model;

import com.gengoai.Language;
import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.InMemoryDataSet;
import com.gengoai.apollo.data.observation.Variable;
import com.gengoai.apollo.data.observation.VariableSequence;
import com.gengoai.apollo.data.transform.vectorizer.IndexingVectorizer;
import com.gengoai.apollo.data.transform.vectorizer.Vectorizer;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.ModelIO;
import com.gengoai.apollo.model.tensorflow.TFInputVar;
import com.gengoai.apollo.model.tensorflow.TFModel;
import com.gengoai.apollo.model.tensorflow.TFOutputVar;
import com.gengoai.hermes.*;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gengoai.apollo.encoder.FixedEncoder.fixedEncoder;
import static com.gengoai.hermes.ResourceType.WORD_LIST;
import static com.gengoai.tuple.Tuples.$;

public class TextualEntailment implements Serializable {
   private static final long serialVersionUID = 1L;
   public static final Map<String, Integer> label2Id = Map.of("neutral", 1,
                                                              "contradiction", 0,
                                                              "entailment", 2);
   public static final Map<Integer, String> id2Label = Map.of(0, "contradiction",
                                                              1, "neutral",
                                                              2, "entailment");
   private TFModel model;


   public static class TEModel extends TFModel {
      static final Encoder glove = fixedEncoder(WORD_LIST.locate("glove_large", Language.ENGLISH)
                                                         .orElseThrow(),
                                                "--UNKNOWN--");


      /**
       * Instantiates a new Tf model.
       */
      public TEModel() {
         super(List.of(TFInputVar.sequence("input_1", glove, 42),
                       TFInputVar.sequence("input_2", glove, 42)),
               List.of(TFOutputVar.classification("output", "dense_5/Softmax:0")));
      }
   }

   /**
    * Instantiates a new Tf model.
    */
   public TextualEntailment() {
      model = ResourceType.MODEL.load("entailment", Language.ENGLISH);
   }

   protected static VariableSequence words(com.gengoai.hermes.HString d) {
      VariableSequence vl = new VariableSequence();
      for (Annotation token : d.tokens()) {
         vl.add(Variable.binary(token.toString()));
      }
      return vl;
   }

   public void train(Resource snliData) throws Exception {
      int i = 0;
      List<Datum> data = new ArrayList<>();
      for (String line : snliData.readLines()) {
         JsonEntry e = Json.parse(line);
         String label = e.getStringProperty("gold_label");
         if (!label2Id.containsKey(label)) {
            continue;
         }
         String sentence1 = e.getStringProperty("sentence1");
         String sentence2 = e.getStringProperty("sentence2");
         com.gengoai.hermes.Document d1 = com.gengoai.hermes.Document.create(sentence1);
         com.gengoai.hermes.Document d2 = com.gengoai.hermes.Document.create(sentence2);
         d1.annotate(Types.TOKEN);
         d2.annotate(Types.TOKEN);
         data.add(Datum.of(
               $("w1", words(d1)),
               $("w2", words(d2)),
               $("label", nd.DFLOAT32.scalar(label2Id.get(label)))));
         i++;
         if (i > 1) break;
         System.out.println(i);
      }
      DataSet dataSet = new InMemoryDataSet(data);
      Map<String, Encoder> vars = this.model.getInputVars().stream().collect(Collectors.toMap(TFInputVar::getName,
                                                                                              TFInputVar::getEncoder));
      Vectorizer<?>[] vs = {
            new IndexingVectorizer(vars.get("w1")).source("w1"),
            new IndexingVectorizer(vars.get("w1")).source("w2")
      };
      for (Vectorizer<?> v : vs) {
         dataSet = v.transform(dataSet);
      }
      ModelIO.save(model, Resources.from("/home/ik/snl"));
      dataSet.persist(Resources.from("/home/ik/snl/snli_data.db"));
   }

   public void test(Resource snliData) throws Exception {
      int i = 0;
      List<Datum> data = new ArrayList<>();
      for (String line : snliData.readLines()) {
         JsonEntry e = Json.parse(line);
         String label = e.getStringProperty("gold_label");
         if (!label2Id.containsKey(label)) {
            continue;
         }
         String sentence1 = e.getStringProperty("sentence1");
         String sentence2 = e.getStringProperty("sentence2");
         com.gengoai.hermes.Document d1 = com.gengoai.hermes.Document.create(sentence1);
         com.gengoai.hermes.Document d2 = com.gengoai.hermes.Document.create(sentence2);
         d1.annotate(Types.TOKEN);
         d2.annotate(Types.TOKEN);
         data.add(Datum.of(
               $("w1", words(d1)),
               $("w2", words(d2)),
               $("label", nd.DFLOAT32.scalar(label2Id.get(label)))));
         i++;
         System.out.println(i);
      }
      DataSet dataSet = new InMemoryDataSet(data);
      Map<String, Encoder> vars = this.model.getInputVars().stream().collect(Collectors.toMap(TFInputVar::getName,
                                                                                              TFInputVar::getEncoder));
      Vectorizer<?>[] vs = {
            new IndexingVectorizer(vars.get("w1")).source("w1"),
            new IndexingVectorizer(vars.get("w1")).source("w2")
      };
      for (Vectorizer<?> v : vs) {
         dataSet = v.transform(dataSet);
      }
      ModelIO.save(model, Resources.from("/home/ik/snl"));
      dataSet.persist(Resources.from("/homes/ik/snl/snli_test.db"));
   }


   public static void main(String[] args) throws Exception {
      TextualEntailment textualEntailment = new TextualEntailment();
      Document d1 = Document.create("The Flu is on the rise in the northeast.");
      Document d2 = Document.create("Flu is a person");
      d1.annotate(Types.SENTENCE, Types.TOKEN);
      d2.annotate(Types.SENTENCE, Types.TOKEN);
      System.out.println(textualEntailment.predict(d1, d2));
      d1 = Document.create("John met with Mary last friday.");
      d2 = Document.create("John is a disease");
      d1.annotate(Types.SENTENCE, Types.TOKEN);
      d2.annotate(Types.SENTENCE, Types.TOKEN);
      System.out.println(textualEntailment.predict(d1, d2));
//      textualEntailment.test(Resources.from("/Users/ik/Downloads/snli_1.0/snli_1.0_test.jsonl"));
//      textualEntailment.train(Resources.from("/home/ik/snli_1.0/snli_1.0_train.jsonl"));
   }


   public String predict(HString hypothesis, HString statement) {
      Datum datum = model.transform(Datum.of($("input_1", words(hypothesis)), $("input_2", words(statement))));
      var result = datum.get("output").asNDArray();
      System.out.println(result);
      return id2Label.get(result.argMax().get(-1));
   }

}//END OF TextualEntailment
