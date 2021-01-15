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

package com.gengoai.sql;

import lombok.NonNull;

import java.io.Serializable;
import java.sql.ResultSet;

/**
 * <p>Defines an interface for mapping a ResultSet into an Object. The implementation should not call <code>next</code>
 * on the ResultSet unless it requires multiple rows to generate the returned Object.</p>
 *
 * @param <T> the type parameter
 */
@FunctionalInterface
public interface ResultSetMapper<T> extends Serializable {

   /**
    * Map the given ResultSet into an Object
    *
    * @param resultSet the result set
    * @return the new object constructed using the ResultSet
    * @throws Exception Something went wrong when mapping
    */
   T map(@NonNull ResultSet resultSet) throws Exception;

}//END OF ResultSetMapper
