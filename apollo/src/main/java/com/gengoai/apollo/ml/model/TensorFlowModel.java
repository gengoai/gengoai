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

package com.gengoai.apollo.ml.model;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.DataSetType;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.encoder.Encoder;
import com.gengoai.apollo.ml.encoder.FixedEncoder;
import com.gengoai.apollo.ml.encoder.NoOptEncoder;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.collection.Iterables;
import com.gengoai.io.Compression;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.reflection.Reflect;
import com.gengoai.reflection.ReflectionException;
import lombok.NonNull;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Abstract base class wrapping models trained in TensorFlow (Python) and exported using the TensorFlow Serving
 * Model format. The organization of a TensorFlow model directory is as follows:</p>
 * <ul>
 *    <li>tfmodel - Folder containing the exported Python model (saved_model.pb, variables)</li>
 *    <li>*.encoder.json.gz - One gzipped json dump per Observation</li>
 *    <li>__class__ - Plain text file with the fully qualified class name of the model implementation.</li>
 * </ul>
 * <p>To train a TensorFlow model you must:</p>
 * <ol>
 *    <li>Create a subclass of TensorFlowModel - Define the <code>createTransformer</code> and <code>process</code> methods.</li>
 *    <li>Generate a json dump of your training data by loading the DataSet and call estimate on your subclass. Note the location of where the dataset was saved.</li>
 *    <li>Train your Python model off the dumped dataset</li>
 *    <li>Copy exported serving model to a tfmodel directory where you saved your trained Java Model</li>
 *    <li>Profit!</li>
 * </ol>
 */
public abstract class TensorFlowModel implements Model {
   private static final long serialVersionUID = 1L;
   protected final Map<String, TFVarSpec> inputs;
   protected final LinkedHashMap<String, TFVarSpec> outputs;
   private final FitParameters<?> fitParameters = new FitParameters<>();
   protected Resource modelFile;
   protected volatile transient Transformer transformer;
   private volatile transient MonitoredObject<SavedModelBundle> model;


   /**
    * Instantiates a new TensorFlowModel.
    *
    * @param inputs  the model inputs
    * @param outputs the model outputs
    */
   protected TensorFlowModel(@NonNull Map<String, TFVarSpec> inputs,
                             @NonNull LinkedHashMap<String, TFVarSpec> outputs) {
      this.inputs = new HashMap<>(inputs);
      this.outputs = new LinkedHashMap<>(outputs);
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
         TensorFlowModel m = Reflect.onClass(modelClass).allowPrivilegedAccess().create().get();
         for (Resource child : resource.getChildren("*.encoder.json.gz")) {
            String name = child.baseName().replace(".encoder.json.gz", "").strip();
            m.setEncoder(name, Json.parse(child, Encoder.class));
         }
         m.transformer = m.createTransformer();
         m.modelFile = resource;
         return m;
      } catch (ReflectionException e) {
         throw new IOException(e);
      }
   }

   protected int calculate_max_sequence_length(DataSet batch) {
      return 0;
   }

   /**
    * Create tensors map.
    *
    * @param batch the batch
    * @return the map
    */
   protected Map<String, Tensor<?>> createTensors(DataSet batch) {
      int batch_size = (int) batch.size();
      int max_sequence_length = calculate_max_sequence_length(batch);
      Map<String, NDArray> ndArrays = new HashMap<>();
      inputs.forEach((name, spec) -> {
         ndArrays.put(name, spec.createBatchNDArray(batch_size, max_sequence_length));
      });
      int index = 0;
      for (Datum datum : batch) {
         for (String name : inputs.keySet()) {
            TFVarSpec spec = inputs.get(name);
            spec.updateBatch(ndArrays.get(name), index, datum.get(name).asNDArray());
         }
         index++;
      }
      Map<String, Tensor<?>> tensors = new HashMap<>();
      inputs.forEach((name, spec) -> {
         tensors.put(spec.getServingName(), spec.toTensor(ndArrays.get(name)));
      });
      return tensors;
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

   /**
    * Decode datum.
    *
    * @param datum the datum
    * @param yHat  the y hat
    * @param slice the slice
    * @return the datum
    */
   private Datum decode(Datum datum, List<NDArray> yHat, long slice) {
      int i = 0;
      for (Map.Entry<String, TFVarSpec> e : outputs.entrySet()) {
         NDArray ndArray = yHat.get(i);
         if (ndArray.shape().order() > 2) {
            datum.put(e.getKey(), decodeNDArray(e.getKey(), yHat.get(i).slice((int) slice)));
         } else {
            datum.put(e.getKey(), decodeNDArray(e.getKey(), yHat.get(i).getRow((int) slice)));
         }
         i++;
      }
      return datum;
   }

   protected abstract Observation decodeNDArray(String name, NDArray ndArray);

   @Override
   public void estimate(@NonNull DataSet dataset) {
      dataset = createTransformer().fitAndTransform(dataset);
      dataset.getMetadata().forEach((k, v) -> setEncoder(k, v.getEncoder()));
      Resource tmp = Resources.temporaryFile();
      dataset.persist(tmp);
      System.out.println("DataSet saved to: " + tmp.descriptor());
   }

   @Override
   public FitParameters<?> getFitParameters() {
      return fitParameters;
   }

   @Override
   public final Set<String> getInputs() {
      return Collections.unmodifiableSet(inputs.keySet());
   }

   @Override
   public final Set<String> getOutputs() {
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
            }
         }
      }
      return model.object;
   }

   /**
    * Process batch list.
    *
    * @param batch the batch
    * @return the list
    */
   protected final List<Datum> processBatch(DataSet batch) {
      batch = transformer.transform(batch);
      Session.Runner runner = getTensorFlowModel().session().runner();
      Map<String, Tensor<?>> tensors = createTensors(batch);
      tensors.forEach(runner::feed);
      outputs.forEach((mo, to) -> runner.fetch(to.getServingName()));
      List<NDArray> results = new ArrayList<>();
      for (Tensor<?> tensor : runner.run()) {
         results.add(NDArrayFactory.ND.fromTensorFlowTensor(tensor));
         tensor.close();
      }
      List<Datum> output = new ArrayList<>();
      batch.stream().zipWithIndex().forEachLocal((d, i) -> output.add(decode(d, results, i)));
      tensors.values().forEach(Tensor::close);
      return output;
   }

   @Override
   public void save(@NonNull Resource resource) throws IOException {
      for (Map.Entry<String, TFVarSpec> entry : Iterables.concat(inputs.entrySet(), outputs.entrySet())) {
         var encoder = entry.getValue().getEncoder();
         if (!(encoder instanceof NoOptEncoder) && !(encoder instanceof FixedEncoder)) {
            Json.dumpPretty(entry.getValue().encoder, resource.getChild(entry.getKey() + ".encoder.json.gz")
                                                              .setCompression(Compression.GZIP));
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
   public final Datum transform(@NonNull Datum datum) {
      return processBatch(DataSetType.InMemory.create(Stream.of(datum))).get(0);
   }

   @Override
   public DataSet transform(@NonNull DataSet dataset) {
      return dataset.map(this::transform);
   }

}//END OF TensorFlowModel
