package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.evaluation.ClassifierEvaluation;
import com.gengoai.apollo.ml.evaluation.MultiClassEvaluation;
import com.gengoai.apollo.ml.model.NaiveBayes;
import com.gengoai.conversion.Cast;

/**
 * @author David B. Bracewell
 */
public class NaiveBayesTest extends BaseClassifierTest {

   public NaiveBayesTest() {
      super(new NaiveBayes(p -> {
         p.output.set("class");
         p.modelType.set(NaiveBayes.ModelType.Multinomial);
      }), false);
   }

   @Override
   public boolean passes(ClassifierEvaluation evaluation) {
      MultiClassEvaluation mce = Cast.as(evaluation);
      return mce.microF1() >= 0.28;
   }
}//END OF NaiveBayesTest
