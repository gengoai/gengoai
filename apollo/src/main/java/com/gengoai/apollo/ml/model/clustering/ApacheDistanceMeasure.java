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

package com.gengoai.apollo.ml.model.clustering;

import com.gengoai.apollo.math.statistics.measure.Measure;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.io.Serializable;

/**
 * Wraps an Apollo measure making it compatible with Apache Math
 *
 * @author David B. Bracewell
 */
class ApacheDistanceMeasure implements DistanceMeasure, Serializable {
   private static final long serialVersionUID = 1L;
   private final Measure wrapped;

   /**
    * Instantiates a new Apache distance measure.
    *
    * @param wrapped the wrapped distance measure
    */
   public ApacheDistanceMeasure(Measure wrapped) {
      this.wrapped = wrapped;
   }

   @Override
   public double compute(double[] doubles, double[] doubles1) throws DimensionMismatchException {
      return wrapped.calculate(doubles, doubles1);
   }

   /**
    * Gets the wrapped distance measure.
    *
    * @return the wrapped distance measure
    */
   public Measure getWrapped() {
      return wrapped;
   }
}// END OF ApacheDistanceMeasure
