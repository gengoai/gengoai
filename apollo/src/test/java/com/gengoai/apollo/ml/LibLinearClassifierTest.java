package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.evaluation.ClassifierEvaluation;
import com.gengoai.apollo.ml.evaluation.MultiClassEvaluation;
import com.gengoai.apollo.ml.model.LibLinear;
import com.gengoai.conversion.Cast;

/**
 * @author David B. Bracewell
 */
public class LibLinearClassifierTest extends BaseClassifierTest {

   public LibLinearClassifierTest() {
      super(new LibLinear(p -> {
         p.output.set("class");
      }), false);
   }

   @Override
   public boolean passes(ClassifierEvaluation evaluation) {
      MultiClassEvaluation mce = Cast.as(evaluation);
      mce.output();
      return mce.microF1() >= 0.85;
   }
}//END OF LibLinearClassifierTest
