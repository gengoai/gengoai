package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.data.CSVDataSetReader;
import com.gengoai.apollo.ml.evaluation.SilhouetteEvaluation;
import com.gengoai.apollo.ml.model.clustering.Clusterer;
import com.gengoai.apollo.ml.model.clustering.Clustering;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.VectorAssembler;
import com.gengoai.collection.Sets;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public abstract class BaseClustererTest {
   private final Clusterer clusterer;

   public BaseClustererTest(Clusterer clusterer) {
      this.clusterer = clusterer;
   }

   public Clustering convertClustering(Clusterer clustering) {
      return clustering.getClustering();
   }

   @Test
   public void fitAndEvaluate() {
      SilhouetteEvaluation evaluation = new SilhouetteEvaluation(clusterer.getFitParameters().measure.value());
      clusterer.estimate(loadWaterData());
      evaluation.evaluate(convertClustering(clusterer));
      assertTrue(passes(evaluation));
   }

   protected DataSet loadWaterData() {
      CSVDataSetReader csv = new CSVDataSetReader(CSV.builder());
      try {
         DataSet ds = csv.read(Resources.fromClasspath("com/gengoai/apollo/ml/water-treatment.data")).probe();
         Transformer transformer = new Transformer(List.of(
               new VectorAssembler(Sets.difference(ds.getMetadata().keySet(), Collections.singleton("AutoColumn-0")),
                                   Datum.DEFAULT_INPUT)));
         return transformer.fitAndTransform(ds);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected abstract boolean passes(SilhouetteEvaluation evaluation);

}//END OF BaseClustererTest
