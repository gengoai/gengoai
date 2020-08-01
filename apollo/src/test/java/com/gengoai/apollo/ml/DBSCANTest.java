package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.evaluation.SilhouetteEvaluation;
import com.gengoai.apollo.ml.model.clustering.DBSCAN;

/**
 * @author David B. Bracewell
 */
public class DBSCANTest extends BaseClustererTest {

   public DBSCANTest() {
      super(new DBSCAN(p -> p.eps.set(500d)));
   }

   @Override
   public boolean passes(SilhouetteEvaluation mce) {
      return mce.getAvgSilhouette() >= 0.9;
   }

}//END OF DBSCANTest
