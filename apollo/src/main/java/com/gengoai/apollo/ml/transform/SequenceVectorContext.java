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

package com.gengoai.apollo.ml.transform;

import com.gengoai.apollo.math.linalg.NDArray;
import com.gengoai.apollo.math.linalg.NDArrayFactory;
import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.observation.Observation;
import com.gengoai.stream.MStream;
import lombok.NonNull;

/**
 * @author David B. Bracewell
 */
public class SequenceVectorContext extends AbstractSingleSourceTransform<SequenceVectorContext> {
   private final int left;
   private final int right;

   public SequenceVectorContext(int left, int right) {
      this.left = Math.abs(left);
      this.right = Math.abs(right);
   }

   @Override
   protected void fit(@NonNull MStream<Observation> observations) {

   }

   @Override
   protected Observation transform(@NonNull Observation observation) {
      NDArray sequence = observation.asNDArray();
      long outColumns = sequence.columns() + (left * sequence.columns()) + (right * sequence.columns());
      NDArray out = NDArrayFactory.ND.array(sequence.rows(), (int) outColumns);
      for(int r = 0; r < sequence.rows(); r++) {
         int offset = 0;
         final int row = r;
         for(int rj = r - left; rj < r + right; rj++) {
            if(rj >= 0 && rj < sequence.rows()) {
               final int o = offset;
               sequence.getRow(rj)
                       .forEachSparse((i, v) -> out.set(row, (int) i + o, v));
            }
            offset += sequence.columns();
         }
      }
      return out;
   }

   @Override
   protected void updateMetadata(@NonNull DataSet data) {
      long dimension = data.getMetadata(input).getDimension();
      final long outdimension = dimension + (left * dimension) + (right * dimension);
      data.updateMetadata(output, m -> {
         m.setDimension(outdimension);
         m.setType(NDArray.class);
         m.setEncoder(null);
      });
   }
}//END OF SequenceVectorContext
