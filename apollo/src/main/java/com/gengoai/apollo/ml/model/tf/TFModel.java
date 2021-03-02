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

package com.gengoai.apollo.ml.model.tf;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.DataSetType;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.StreamingDataSet;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.FixedEncoder;
import com.gengoai.apollo.ml.encoder.IndexEncoder;
import com.gengoai.apollo.ml.encoder.NoOptEncoder;
import com.gengoai.apollo.ml.model.FitParameters;
import com.gengoai.apollo.ml.model.LabelType;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.collection.Iterables;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Compression;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import com.gengoai.stream.MStream;
import com.gengoai.stream.StreamingContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TFModel implements Model, Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<String, TFInputVar> inputs = new HashMap<>();
   private final LinkedHashMap<String, TFOutputVar> outputs = new LinkedHashMap<>();
   private final FitParameters<?> parameters = new FitParameters<>();
   protected Resource modelFile;
   protected volatile transient Transformer transformer;
   private volatile transient MonitoredObject<SavedModelBundle> model;
   private volatile List<TFOutputVar> outputList;
   @Getter
   @Setter
   private int inferenceBatchSize = 1;


   public TFModel(@NonNull List<TFInputVar> inputVars, @NonNull List<TFOutputVar> outputVars) {
      for (TFInputVar inputVar : inputVars) {
         inputs.put(inputVar.getName(), inputVar);
      }
      for (TFOutputVar outputVar : outputVars) {
         outputs.put(outputVar.getName(), outputVar);
      }
   }

   /**
    * Reads the model from the given resource
    *
    * @param resource the resource
    * @return the model
    * @throws IOException Something went wrong reading the model
    */
   public static Model load(@NonNull Resource resource) throws IOException {
      Class<?> modelClass = Reflect.getClassForNameQuietly(resource.getChild("__class__").readToString().strip());
      try {
         TFModel m = Reflect.onClass(modelClass).allowPrivilegedAccess().create().get();
         for (Resource child : resource.getChildren("*.encoder.json.gz")) {
            String name = child.baseName().replace(".encoder.json.gz", "").strip();
            m.setEncoder(name, Json.parse(child, Encoder.class));
            IndexEncoder ie = Json.parse(child, Encoder.class);
         }
         m.transformer = m.createTransformer();
         m.modelFile = resource;
         return m;
      } catch (ReflectionException e) {
         throw new IOException(e);
      }
   }

   /**
    * Creates the required transformer for preparing inputs / outputs to pass to TensorFlow.
    *
    * @return the transformer
    */
   protected Transformer createTransformer() {
      return new Transformer(Stream.concat(inputs.entrySet().stream(), outputs.entrySet().stream())
                                   .filter(e -> !(e.getValue().getEncoder() instanceof NoOptEncoder))
                                   .map(e -> new IndexingVectorizer(e.getValue().getEncoder()).source(e.getKey()))
                                   .collect(Collectors.toList()));
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      outputList = new ArrayList<>();
      outputs.forEach((k, v) -> outputList.add(v));
      dataset = createTransformer().fitAndTransform(dataset);
      dataset.getMetadata().forEach((k, v) -> setEncoder(k, v.getEncoder()));
      Resource tmp = Resources.temporaryFile();
      if (Config.hasProperty("tfmodel.data")) {
         tmp = Config.get("tfmodel.data").asResource();
      }
      dataset.shuffle().persist(tmp);
      System.err.println("DataSet saved to: " + tmp.descriptor());
   }

   @Override
   public FitParameters<?> getFitParameters() {
      return parameters;
   }

   @Override
   public Set<String> getInputs() {
      return Collections.unmodifiableSet(inputs.keySet());
   }

   @Override
   public LabelType getLabelType(@NonNull String name) {
      if (outputs.containsKey(name)) {
         return outputs.get(name).getLabelType();
      }
      throw new IllegalArgumentException("Unknown output variable '" + name + "'");
   }

   @Override
   public Set<String> getOutputs() {
      return Collections.unmodifiableSet(outputs.keySet());
   }

   private SavedModelBundle getTensorFlowModel() {
      if (model == null) {
         synchronized (this) {
            if (model == null) {
               model = ResourceMonitor.monitor(SavedModelBundle.load(modelFile.getChild("tfmodel")
                                                                              .asFile()
                                                                              .orElseThrow()
                                                                              .getAbsolutePath(),
                                                                     "serve"));
               transformer = createTransformer();
               outputList = new ArrayList<>();
               outputs.forEach((k, v) -> outputList.add(v));
            }
         }
      }
      return model.object;
   }


   protected final List<Datum> processBatch(DataSet batch) {
      final var batchTransformed = transformer.transform(batch).collect();
      Session.Runner runner = getTensorFlowModel().session().runner();

      Map<String, Tensor<?>> tensors = new HashMap<>();
      inputs.forEach((name, spec) -> tensors.put(spec.getServingName(), spec.toTensor(batchTransformed)));
      tensors.forEach(runner::feed);


      outputs.forEach((mo, to) -> runner.fetch(to.getServingName()));

      List<NumericNDArray> results = new ArrayList<>();
      for (Tensor<?> tensor : runner.run()) {
         results.add(Cast.as(nd.convertTensor(tensor)));
         tensor.close();
      }


      for (int slice = 0; slice < batchTransformed.size(); slice++) {
         Datum d = batchTransformed.get(slice);
         int resultIndex = 0;
         for (Map.Entry<String, TFOutputVar> e : outputs.entrySet()) {
            NumericNDArray r = results.get(resultIndex);
            TFOutputVar v = e.getValue();
            NumericNDArray yHat = v.extractSingleDatumResult(r, slice);
            d.put(v.getName(), v.decode(yHat));
            resultIndex++;
         }

      }

      tensors.values().forEach(Tensor::close);
      return batchTransformed;
   }

   @Override
   public void save(@NonNull Resource resource) throws IOException {
      for (Map.Entry<String, ? extends TFVar> entry : Iterables.concat(inputs.entrySet(), outputs.entrySet())) {
         var encoder = entry.getValue().getEncoder();
         if (!(encoder instanceof NoOptEncoder) && !(encoder instanceof FixedEncoder)) {
            Json.dumpPretty(entry.getValue().getEncoder(),
                            resource.getChild(entry.getKey() + ".encoder.json.gz").setCompression(Compression.GZIP));
         }
      }
   }

   protected void setEncoder(String name, Encoder encoder) {
      if (inputs.containsKey(name)) {
         inputs.get(name).setEncoder(encoder);
      } else {
         outputs.get(name).setEncoder(encoder);
      }
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      if (inferenceBatchSize == 1) {
         return dataset.map(this::transform);
      }
      MStream<Datum> batch = StreamingContext.local()
                                             .stream(dataset.batchIterator(inferenceBatchSize))
                                             .map(this::processBatch)
                                             .flatMap(Collection::stream);
      return new StreamingDataSet(batch, dataset.getMetadata(), dataset.getNDArrayFactory());
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      return processBatch(DataSetType.InMemory.create(Stream.of(datum))).get(0);
   }
}
