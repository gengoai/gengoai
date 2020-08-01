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

package com.gengoai.apollo.math.statistics.measure;

import com.gengoai.Validation;

/**
 * @author David B. Bracewell
 */
public enum Agreement implements ContingencyTableCalculator {
   Cohen_Kappa {
      @Override
      public double calculate(ContingencyTable table) {
         Validation.checkArgument(table.columnCount() == 2
                                     && table.rowCount() == 2,
                                  "Only 2x2 tables supported");
         double sum = table.getSum();
         double sumSq = sum * sum;
         double Po = (table.get(0, 0) + table.get(1, 1)) / sum;
         double Pe = ((table.columnSum(0) * table.rowSum(0)) / sumSq)
                        + ((table.columnSum(1) * table.rowSum(1)) / sumSq);
         return (Po - Pe) / (1.0 - Pe);
      }
   }


}// END OF Agreement
