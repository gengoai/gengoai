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

package com.gengoai.apollo.ml;

import com.gengoai.apollo.ml.evaluation.RegressionEvaluation;
import com.gengoai.apollo.ml.model.LibLinear;
import de.bwaldvogel.liblinear.SolverType;

/**
 * @author David B. Bracewell
 */
public class LinearRegressionTest extends BaseRegressionTest {

   public LinearRegressionTest() {
      super(new LibLinear(p -> {
         p.output.set("Pressure");
         p.solver.set(SolverType.L2R_L2LOSS_SVR);
         p.verbose.set(false);
      }));
   }

   @Override
   public boolean passes(RegressionEvaluation mce) {
      return mce.r2() >= 0.84;
   }
}//END OF LibLinearRegressionTest
