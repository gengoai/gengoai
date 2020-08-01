package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.data.CSVDataSetReader;
import com.gengoai.apollo.ml.evaluation.RegressionEvaluation;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.PipelineModel;
import com.gengoai.apollo.ml.observation.VariableNameSpace;
import com.gengoai.apollo.ml.transform.Merge;
import com.gengoai.apollo.ml.transform.StandardScalar;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.VectorAssembler;
import com.gengoai.apollo.ml.transform.vectorizer.CountVectorizer;
import com.gengoai.collection.Sets;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * @author David B. Bracewell
 */
public abstract class BaseRegressionTest {
   private final Model regression;

   public BaseRegressionTest(Model regression) {
      this.regression = PipelineModel.builder()
                                     .transform(Merge.builder()
                                                     .inputs(List.of("Frequency",
                                                                     "Angle",
                                                                     "Chord",
                                                                     "Velocity",
                                                                     "Suction"))
                                                     .output(Datum.DEFAULT_INPUT)
                                                     .build())
                                     .defaultInput(new StandardScalar(VariableNameSpace.Prefix),
                                                   new CountVectorizer())
                                     .build(regression);
   }

   public DataSet airfoilDataset() {
      CSVDataSetReader csv = new CSVDataSetReader(CSV.builder()
                                                     .delimiter('\t')
                                                     .header("Frequency",
                                                             "Angle",
                                                             "Chord",
                                                             "Velocity",
                                                             "Suction",
                                                             "Pressure"));
      try {
         DataSet ds = csv.read(Resources.fromClasspath("com/gengoai/apollo/ml/airfoil_self_noise.data")).probe();
         Transformer transformer = new Transformer(List.of(
               new VectorAssembler(Sets.difference(ds.getMetadata().keySet(), Collections.singleton("Pressure")),
                                   Datum.DEFAULT_INPUT)));
         return transformer.fitAndTransform(ds);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Test
   public void fitAndEvaluate() {
      assertTrue(passes(RegressionEvaluation.crossValidation(airfoilDataset(),
                                                             regression,
                                                             Datum.DEFAULT_INPUT,
                                                             "Pressure",
                                                             3)));
   }

   public abstract boolean passes(RegressionEvaluation mce);
}//END OF BaseRegressionTest
