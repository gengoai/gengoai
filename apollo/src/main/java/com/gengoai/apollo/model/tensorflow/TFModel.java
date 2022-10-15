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

package com.gengoai.apollo.model.tensorflow;

import com.gengoai.apollo.data.DataSet;
import com.gengoai.apollo.data.DataSetType;
import com.gengoai.apollo.data.Datum;
import com.gengoai.apollo.data.StreamingDataSet;
import com.gengoai.apollo.data.transform.Transformer;
import com.gengoai.apollo.data.transform.vectorizer.IndexingVectorizer;
import com.gengoai.apollo.encoder.Encoder;
import com.gengoai.apollo.encoder.FixedEncoder;
import com.gengoai.apollo.encoder.IndexEncoder;
import com.gengoai.apollo.encoder.NoOptEncoder;
import com.gengoai.apollo.math.linalg.NumericNDArray;
import com.gengoai.apollo.math.linalg.nd;
import com.gengoai.apollo.model.FitParameters;
import com.gengoai.apollo.model.LabelType;
import com.gengoai.apollo.model.Model;
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
import com.gengoai.stream.Streams;
import lombok.*;
import lombok.extern.java.Log;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Wrapper for using a TensorFlow model. A TFModel is comprised of a set of <code>TFInputVar</code> and
 * <code>TFOutputVar</code> which define the observation name (in Java), its corresponding TensorFlow serving name,
 * its shape, and associated Encoder.</p>
 */
@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TFModel implements Model, Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<String, TFInputVar> inputs = new HashMap<>();
   private final LinkedHashMap<String, TFOutputVar> outputs = new LinkedHashMap<>();
   private final FitParameters<?> parameters = new FitParameters<>();
   protected Resource modelFile;
   protected volatile transient Transformer transformer;
   private volatile transient MonitoredObject<SavedModelBundle> model;

   @Getter
   @Setter
   private int inferenceBatchSize = 1;


   public Collection<TFInputVar> getInputVars(){
      return Collections.unmodifiableCollection(inputs.values());
   }

   public Collection<TFOutputVar> getOutputVars(){
      return Collections.unmodifiableCollection(outputs.values());
   }

   /**
    * Instantiates a new Tf model.
    *
    * @param inputVars  the input vars
    * @param outputVars the output vars
    */
   public TFModel(@NonNull List<TFInputVar> inputVars,
                  @NonNull List<TFOutputVar> outputVars) {
      for (TFInputVar inputVar : inputVars) {
         inputs.put(inputVar.getName(), inputVar);
      }
      for (TFOutputVar outputVar : outputVars) {
         outputs.put(outputVar.getName(), outputVar);
      }
   }

   /**
    * <p>Loads the TFModel from disk. The structure of a TFModel directory is as follows:</p>
    * <ul>
    *    <li><code>__class__</code> : Model Class information</li>
    *    <li><code>*.encoder.json.gz</code>: Serialized Encoders for inputs and outputs</li>
    *    <li><code>tfmodel</code>: Directory containing the TensorFlow serving model</li>
    * </ul>
    *
    * @param resource the location containing the saved model
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
         }
         m.modelFile = resource;
         return m;
      } catch (ReflectionException e) {
         throw new IOException(e);
      }
   }

   private Transformer createTransformer() {
      if (transformer == null) {
         synchronized (this) {
            if (transformer == null) {
               this.transformer = new Transformer(Stream.concat(inputs.entrySet().stream(), outputs.entrySet().stream())
                                                        .filter(e -> !(e.getValue()
                                                                        .getEncoder() instanceof NoOptEncoder))
                                                        .map(e -> new IndexingVectorizer(e.getValue().getEncoder())
                                                              .source(e.getKey()))
                                                        .collect(Collectors.toList()));
            }
         }
      }
      return transformer;
   }

   @Override
   public void estimate(@NonNull DataSet dataset) {
      dataset = createTransformer().fitAndTransform(dataset);
      dataset.getMetadata().forEach((k, v) -> setEncoder(k, v.getEncoder()));
      Resource tmp = Resources.temporaryFile();
      if (Config.hasProperty("tfmodel.data")) {
         tmp = Config.get("tfmodel.data").asResource();
      }
      try {
         dataset.shuffle().persist(tmp);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
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
            }
         }
      }
      return model.object;
   }

   protected final List<Datum> processBatch(DataSet batch) {
      final var batchTransformed = createTransformer().transform(batch).collect();
      Session.Runner runner = getTensorFlowModel().session().runner();

      //Setup the inputs to feed
      Map<String, Tensor<?>> tensors = new HashMap<>();
      //"serving_default_" +
      inputs.forEach((name, spec) -> tensors.put(spec.getServingName(), spec.toTensor(batchTransformed)));
      tensors.forEach(runner::feed);

      //Setup the outputs to fetch
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

      //Close the input tensors
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

   private void setEncoder(String name, Encoder encoder) {
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
                                             .stream(Streams.reusableStream(
                                                   () -> Streams.asStream(dataset.batchIterator(inferenceBatchSize))
                                             ))
                                             .map(this::processBatch)
                                             .flatMap(Collection::stream);
      return new StreamingDataSet(batch, dataset.getMetadata(), dataset.getNDArrayFactory()).cache();
   }


   @Override
   public Datum transform(@NonNull Datum datum) {
      return processBatch(DataSetType.InMemory.create(Stream.of(datum))).get(0);
   }


}//END OF TFModel
