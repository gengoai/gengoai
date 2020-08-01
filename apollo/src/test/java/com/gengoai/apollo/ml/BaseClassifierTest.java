package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.data.CSVDataSetReader;
import com.gengoai.apollo.ml.evaluation.ClassifierEvaluation;
import com.gengoai.apollo.ml.evaluation.MultiClassEvaluation;
import com.gengoai.apollo.ml.model.Model;
import com.gengoai.apollo.ml.model.PipelineModel;
import com.gengoai.apollo.ml.transform.Merge;
import com.gengoai.apollo.ml.transform.Transform;
import com.gengoai.apollo.ml.transform.Transformer;
import com.gengoai.apollo.ml.transform.VectorAssembler;
import com.gengoai.apollo.ml.transform.vectorizer.IndexingVectorizer;
import com.gengoai.collection.Sets;
import com.gengoai.io.CSV;
import com.gengoai.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author David B. Bracewell
 */
public abstract class BaseClassifierTest {

   private final Model classifier;
   private final boolean merge;

   public BaseClassifierTest(Model classifier, boolean merge) {
      if(merge) {
         this.classifier = classifier;
      } else {
         this.classifier = PipelineModel.builder()
                                        .source("class", new IndexingVectorizer())
                                        .build(classifier);
      }
      this.merge = merge;
   }

   @Test
   public void fitAndEvaluate() {
      assertTrue(passes(MultiClassEvaluation.crossvalidation(irisDataset(),
                                                             classifier,
                                                             10,
                                                             "class")));
   }

   public DataSet irisDataset() {
      CSVDataSetReader csv = new CSVDataSetReader(CSV.builder().hasHeader(true));
      try {
         DataSet ds = csv.read(Resources.fromClasspath("com/gengoai/apollo/ml/iris.csv"));
         ds.probe();
         List<Transform> transforms = new ArrayList<>();
         if(merge) {
            transforms.add(Merge.builder()
                                .inputs(Sets.difference(ds.getMetadata().keySet(),
                                                        Collections.singleton("class")))
                                .output(Datum.DEFAULT_INPUT)
                                .build());
         } else {
            transforms.add(new VectorAssembler(Sets.difference(ds.getMetadata().keySet(),
                                                               Collections.singleton("class")), Datum.DEFAULT_INPUT));
         }
         return new Transformer(transforms).fitAndTransform(ds);
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
   }

   public abstract boolean passes(ClassifierEvaluation mce);

}//END OF BaseClassifierTest
