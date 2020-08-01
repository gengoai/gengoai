/*
 * (c) 2005 David B. Bracewell
 *
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
 *
 */

package com.gengoai.apollo.ml.evaluation;

import com.gengoai.Validation;
import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.statistics.measure.Measure;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.clustering.Cluster;
import com.gengoai.apollo.ml.model.clustering.Clusterer;
import com.gengoai.apollo.ml.model.clustering.Clustering;
import com.gengoai.conversion.Cast;
import com.gengoai.math.Math2;
import com.gengoai.stream.StreamingContext;
import com.gengoai.string.TableFormatter;
import lombok.NonNull;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>
 * Evaluates a Clustering using <a href="https://en.wikipedia.org/wiki/Silhouette_(clustering)">Silhouette</a>, which is
 * a measure of how similar an object is to its own cluster compared to others.
 * </p>
 *
 * @author David B. Bracewell
 */
public class SilhouetteEvaluation implements ClusteringEvaluation, Serializable {
   private static final long serialVersionUID = 1L;
   private final Measure measure;
   private double avgSilhouette = 0;
   private Map<Integer, Double> silhouette;

   /**
    * Evaluates the given Model with the given testing data.
    *
    * @param clusters the clustering to evaluate
    * @param measure  the measure to use to judge the clustering
    * @return the SilhouetteEvaluation
    */
   public static SilhouetteEvaluation evaluate(@NonNull Clustering clusters, @NonNull Measure measure) {
      SilhouetteEvaluation evaluation = new SilhouetteEvaluation(measure);
      evaluation.evaluate(clusters);
      return evaluation;
   }

   /**
    * Instantiates a new Silhouette evaluation.
    *
    * @param measure the measure
    */
   public SilhouetteEvaluation(Measure measure) {
      this.measure = measure;
   }

   @Override
   public void evaluate(@NonNull Model model, @NonNull DataSet dataset) {
      Validation.checkArgumentIsInstanceOf(Clusterer.class);
      model.estimate(dataset);
      evaluate(Cast.<Clusterer>as(model).getClustering());
   }

   @Override
   public void evaluate(@NonNull Clustering clustering) {
      Map<Integer, Cluster> idClusterMap = new HashMap<>();
      clustering.forEach(c -> idClusterMap.put(c.getId(), c));
      silhouette = StreamingContext.local()
                                   .stream(idClusterMap.keySet())
                                   .parallel()
                                   .mapToPair(i -> $(i, silhouette(idClusterMap, i, measure)))
                                   .collectAsMap();
      avgSilhouette = Math2.summaryStatistics(silhouette.values()).getAverage();

   }

   /**
    * Gets the average silhouette score.
    *
    * @return the average silhouette score
    */
   public double getAvgSilhouette() {
      return avgSilhouette;
   }

   /**
    * Gets the silhouette score for the given cluster id.
    *
    * @param id the id of the cluster
    * @return the silhouette score for the cluster with the given id
    */
   public double getSilhouette(int id) {
      return silhouette.get(id);
   }

   @Override
   public void output(PrintStream printStream) {
      TableFormatter formatter = new TableFormatter();
      formatter.title("Silhouette Cluster Evaluation");
      formatter.header(Arrays.asList("Cluster", "Silhouette Score"));
      silhouette.keySet()
                .stream()
                .sorted()
                .forEach(id -> formatter.content(Arrays.asList(id, silhouette.get(id))));
      formatter.footer(Arrays.asList("Avg. Score", avgSilhouette));
      formatter.print(printStream);
   }

   private double silhouette(Map<Integer, Cluster> clusters, int index, Measure distanceMeasure) {
      Cluster c1 = clusters.get(index);
      if(c1.size() <= 1) {
         return 0;
      }
      double s = 0;
      for(NDArray point1 : c1) {
         double ai = 0;
         for(NDArray point2 : c1) {
            ai += distanceMeasure.calculate(point1, point2);
         }
         ai = Double.isFinite(ai)
              ? ai
              : Double.MAX_VALUE;
         ai /= c1.size();
         double bi = clusters.keySet().parallelStream()
                             .filter(j -> j != index)
                             .mapToDouble(j -> {
                                if(clusters.get(j).size() == 0) {
                                   return Double.MAX_VALUE;
                                }
                                double b = 0;
                                for(NDArray point2 : clusters.get(j)) {
                                   b += distanceMeasure.calculate(point1, point2);
                                }
                                return b;
                             }).min()
                             .orElse(0);
         s += (bi - ai) / Math.max(bi, ai);
      }

      return s / c1.size();
   }

}//END OF SilhouetteEvaluation
