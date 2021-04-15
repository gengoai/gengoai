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

package com.gengoai.swing;

import java.awt.Component;
import java.awt.Dimension;

/**
 * Methods for calculating various sizes of a component taking into account its visibility.
 */
public enum SizeCalculator {
   /**
    * <code>Component.getSize()</code>
    */
   ActualSize {
      @Override
      public Dimension calculate(Component component) {
         return component == null || component.isVisible()
                ? new Dimension(0, 0)
                : component.getSize();
      }
   },
   /**
    * <code>Component.getPreferredSize()</code>
    */
   PreferredSize {
      @Override
      public Dimension calculate(Component component) {
         return component == null || !component.isVisible()
                ? new Dimension(0, 0)
                : component.getPreferredSize();
      }
   },
   /**
    * <code>Component.getMinimumSize()</code>
    */
   MinimumSize {
      @Override
      public Dimension calculate(Component component) {
         return component == null || !component.isVisible()
                ? new Dimension(0, 0)
                : component.getMinimumSize();
      }
   },
   /**
    * <code>Component.getMaximumSize()</code>
    */
   MaximumSize {
      @Override
      public Dimension calculate(Component component) {
         return component == null || !component.isVisible()
                ? new Dimension(0, 0)
                : component.getMaximumSize();
      }
   };

   /**
    * Calculate the size of the given component.
    *
    * @param component the component
    * @return the size as a dimension
    */
   public abstract Dimension calculate(Component component);

}//END OF SizeType
