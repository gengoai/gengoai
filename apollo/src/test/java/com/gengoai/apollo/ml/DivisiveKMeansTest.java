package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.evaluation.SilhouetteEvaluation;
import com.gengoai.apollo.ml.model.clustering.DivisiveKMeans;

/**
 * @author David B. Bracewell
 */
public class DivisiveKMeansTest extends BaseClustererTest {

   public DivisiveKMeansTest() {
      super(new DivisiveKMeans(p -> {
         p.tolerance.set(1000d);
         p.verbose.set(false);
      }));
   }

   @Override
   public boolean passes(SilhouetteEvaluation mce) {
      return mce.getAvgSilhouette() >= 0.70;
   }

}//END OF KMeansTest
