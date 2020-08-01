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

package com.gengoai.apollo.ml.data.sampling;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.apollo.ml.Datum;
import com.gengoai.apollo.ml.InMemoryDataSet;
import com.gengoai.apollo.ml.StreamingDataSet;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class RandomSampler implements DataSetSampler, Serializable {
   private boolean withReplacement;
   private int sampleSize;

   @Override
   public DataSet sample(@NonNull DataSet dataSet) {
      if(dataSet instanceof InMemoryDataSet) {
         return new InMemoryDataSet(dataSet.stream().sample(withReplacement, sampleSize).map(Datum::copy).collect(),
                                    dataSet.getMetadata(),
                                    dataSet.getNDArrayFactory());
      }
      return new StreamingDataSet(dataSet.stream().sample(withReplacement, sampleSize).map(Datum::copy),
                                  dataSet.getMetadata(),
                                  dataSet.getNDArrayFactory());
   }
}//END OF RandomSampler
