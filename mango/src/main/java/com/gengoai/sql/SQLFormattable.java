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

/**
 * <p>Extension of an {@link SQLElement} that uses an {@link SQLDialect} to format components.</p>
 */
public interface SQLFormattable extends SQLElement {

   /**
    * Converts the SQL Statement into valid SQL for a given SQL dialect
    *
    * @param dialect the dialect of the SQL to convert this statement to.
    * @return the SQL
    */
   String toSQL(@NonNull SQLDialect dialect);
}//END OF SQLFormattable
