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

package com.gengoai.apollo.math.linalg;

import com.gengoai.apollo.math.NumericComparison;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public class LearningTest {

   @Test
   public void simpleLogisticRegression() {
      //Create random data of 5-dimensional vectors where 200 are initialized with a gaussian with a mean=0, and sigma=1,
      // and 200 are initialized with a gaussian with a mean=4, and sigma=1
      var x = nd.vstack(
            nd.DFLOAT32.create(Shape.shape(200, 5), NDArrayInitializer.gaussian(0, 1)),
            nd.DFLOAT32.create(Shape.shape(200, 5), NDArrayInitializer.gaussian(4, 1))
      );

      //The first 200 are "0" and the second 200 are "1"
      var y = nd.vstack(nd.DFLOAT32.zeros(200, 1),
                        nd.DFLOAT32.ones(200, 1)
      );

      //Rescale the data to be between 0 and 1
      var min = x.min(Shape.ROW);
      var max = x.max(Shape.ROW);
      var diff = max.sub(min);
      x = x.subi(min).divi(diff);

      //Model Parameters
      NumericNDArray W = nd.DFLOAT32.create(Shape.shape(5, 1), NDArrayInitializer.xavier);
      double bias = 0;

      //Start with high learning rate and lower it as the gradient needs to be clipped.
      double learningRate = 1;

      for (int epoch = 0; epoch < 300; epoch++) {
         var pred = nd.sigmoid(nd.dot(x, W).addi(bias));
         var error = pred.sub(y);
         var grad = nd.dot(x.T(), error).muli(learningRate / x.length());
         double gNorm = grad.norm2();
         if (gNorm > 1.0) {
            grad = grad.muli(1.0 / gNorm);
            learningRate *= 0.9;
         }
         W.subi(grad);
         bias -= error.sum() * (learningRate / x.length());
      }

      //Create a random test set in the same manner as the training set
      x = nd.vstack(
            nd.DFLOAT32.create(Shape.shape(200, 5), NDArrayInitializer.gaussian(0, 1)),
            nd.DFLOAT32.create(Shape.shape(200, 5), NDArrayInitializer.gaussian(4, 1))
      );

      //Rescale the test set
      x = x.subi(min).divi(diff);


      var pred = nd.roundi(nd.sigmoidi(nd.dot(x, W).addi(bias)));
      var acc = nd.test(pred, y, NumericComparison.EQ).sum() / y.shape().rows();

      assertEquals(1.0, acc, 0.1);
   }

}//END OF LearningTest
