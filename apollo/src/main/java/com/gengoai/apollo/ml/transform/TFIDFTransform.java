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

package com.gengoai.apollo.ml.transform;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.observation.*;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.collection.counter.MultiCounter;
import com.gengoai.stream.MCounterAccumulator;
import com.gengoai.stream.MMultiCounterAccumulator;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class TFIDFTransform extends PerPrefixTransform<TFIDFTransform> {
   private static final long serialVersionUID = 1L;
   private MultiCounter<String, String> prefixWordDocumentCounts;
   private Counter<String> totalDocuments;

   protected double calculateTFIDF(String prefix, String suffix, double value) {
      if(prefixWordDocumentCounts.contains(prefix, suffix)) {
         return value * prefixWordDocumentCounts.get(prefix, suffix);
      } else {
         return value * totalDocuments.get(prefix);
      }
   }

   @Override
   protected void fit(@NonNull String prefix, @NonNull Iterable<Variable> variables) {

   }

   @Override
   public DataSet fitAndTransform(DataSet dataset) {
      MCounterAccumulator<String> totalDocumentAccumulator = dataset.getType()
                                                                    .getStreamingContext()
                                                                    .counterAccumulator();
      MMultiCounterAccumulator<String, String> prefixWordAccumulator = dataset.getType()
                                                                              .getStreamingContext()
                                                                              .multiCounterAccumulator();
      // Calculate Label-Feature Coccurrences
      dataset.parallelStream().forEach(d -> {
         Map<String, List<Variable>> m = splitIntoPrefixes(d.get(input));
         for(String prefix : m.keySet()) {
            totalDocumentAccumulator.increment(prefix, 1d);
            m.get(prefix)
             .stream()
             .map(Variable::getSuffix)
             .distinct()
             .forEach(s -> prefixWordAccumulator.increment(prefix, s));
         }
      });
      totalDocuments = totalDocumentAccumulator.value();
      prefixWordDocumentCounts = prefixWordAccumulator.value();
      for(String prefix : prefixWordDocumentCounts.firstKeys()) {
         double total = totalDocuments.get(prefix);
         prefixWordDocumentCounts.get(prefix)
                                 .adjustValuesSelf(v -> Math.log((total + 0.5) / (v + 0.5)));
         totalDocuments.set(prefix, Math.log((total + 0.5) / 0.5));
      }
      return transform(dataset);
   }

   @Override
   protected void reset() {

   }

   private Map<String, List<Variable>> splitIntoPrefixes(Observation d) {
      return d.getVariableSpace()
              .collect(Collectors.groupingBy(Variable::getPrefix, Collectors.toList()));
   }

   @Override
   protected VariableCollection transform(Observation o) {
      Map<String, List<Variable>> m = splitIntoPrefixes(o);
      VariableCollection vc = new VariableList();
      for(String prefix : m.keySet()) {
         Counter<String> suffixCount = Counters.newCounter(m.get(prefix)
                                                            .stream()
                                                            .map(Variable::getSuffix));
         suffixCount.divideBySum().adjustValuesSelf(v -> 0.5 + 0.5 * v);
         suffixCount.forEach((suffix, count) -> {
            vc.add(Variable.real(prefix, suffix, calculateTFIDF(prefix, suffix, count)));
         });
      }
      return vc;
   }

   @Override
   public Datum transform(@NonNull Datum datum) {
      Observation o = datum.get(input);
      if(o.isSequence()) {
         VariableCollectionSequence out = new VariableCollectionSequence();
         for(int i = 0; i < o.asSequence().size(); i++) {
            out.add(transform(o.asSequence().get(i)));
         }
         datum.put(output, out);
      } else {
         datum.put(output, transform(o));
      }
      return datum;
   }

   @Override
   protected Variable transform(@NonNull Variable variable) {
      return variable;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {

   }
}//END OF TFIDFTransform
