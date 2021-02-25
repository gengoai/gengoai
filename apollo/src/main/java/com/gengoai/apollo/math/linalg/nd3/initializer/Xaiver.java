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

package com.gengoai.apollo.math.linalg.nd3.initializer;

import com.gengoai.apollo.math.linalg.nd3.Shape;
import org.apache.commons.math3.random.RandomDataGenerator;

public class Xaiver extends NDArrayInitializer {
   private final double limit;
   private final RandomDataGenerator rnd = new RandomDataGenerator();

   public Xaiver(Shape shape) {
      super(shape);
      limit = Math.sqrt(6) / Math.sqrt(shape.rows() + shape.columns());
   }

   @Override
   public Float get() {
      return (float) getAsDouble();
   }

   @Override
   public double getAsDouble() {
      return rnd.nextUniform(-limit, limit);
   }

}//Xaiver
