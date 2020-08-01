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

package com.gengoai.apollo.ml.data;

import com.gengoai.apollo.ml.DataSet;
import com.gengoai.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>
 * Reads a {@link DataSet} in a pre-defined format from the given resource. Implementations should handle setting the
 * metadata on the DataSet.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface DataSetReader extends Serializable {

   /**
    * Reads in {@link DataSet} from the given resource.
    *
    * @param dataResource the resource containing the data to read
    * @return the DataSet
    * @throws IOException something went wrong reading from the resource
    */
   DataSet read(@NonNull Resource dataResource) throws IOException;

}//END OF DataSetReader
