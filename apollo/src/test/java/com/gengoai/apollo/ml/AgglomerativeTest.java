package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.evaluation.SilhouetteEvaluation;
import com.gengoai.apollo.ml.model.clustering.AgglomerativeClusterer;
import com.gengoai.apollo.ml.model.clustering.Clusterer;
import com.gengoai.apollo.ml.model.clustering.Clustering;
import com.gengoai.apollo.ml.model.clustering.HierarchicalClustering;
import com.gengoai.conversion.Cast;

/**
 * @author David B. Bracewell
 */
public class AgglomerativeTest extends BaseClustererTest {

   public AgglomerativeTest() {
      super(new AgglomerativeClusterer());
   }

   @Override
   public Clustering convertClustering(Clusterer clustering) {
      Clustering c = clustering.getClustering();
      return Cast.<HierarchicalClustering>as(c).asFlat(4000);
   }

   @Override
   public boolean passes(SilhouetteEvaluation mce) {
      return mce.getAvgSilhouette() >= 0.85;
   }

}//END OF KMeansTest
