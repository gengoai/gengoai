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

package com.gengoai.tuple;

import lombok.Value;

import java.io.Serializable;

/**
 * <p>A specialized Tuple of integer values. Comparison is made by comparing the first value (v1) and if they are the
 * same then comparing the second value (v2).</p>
 */
@Value(staticConstructor = "of")
public class IntPair implements Serializable, Comparable<IntPair> {
   /**
    * The first value
    */
   public int v1;
   /**
    * The second value
    */
   public int v2;

   @Override
   public int compareTo(IntPair o) {
      if (o == null) {
         return 1;
      }
      int cmp = Integer.compare(v1, o.v1);
      if (cmp == 0) {
         cmp = Integer.compare(v2, o.v2);
      }
      return cmp;
   }
}//END OF IntPair
