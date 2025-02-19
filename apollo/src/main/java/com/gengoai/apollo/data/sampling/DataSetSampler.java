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

package com.gengoai.apollo.data.sampling;

import com.gengoai.apollo.data.DataSet;
import lombok.NonNull;

/**
 * <p>Defines a methodology for sampling the datum in a DataSet in some manner. This is typically done to balance the
 * distribution of class labels.</p>
 */
public interface DataSetSampler {

   /**
    * <p>Sample the given DataSet</p>
    *
    * @param dataSet the DataSet to sample
    * @return the sampled DataSet
    */
   DataSet sample(@NonNull DataSet dataSet);

}//END OF DataSetSampler
