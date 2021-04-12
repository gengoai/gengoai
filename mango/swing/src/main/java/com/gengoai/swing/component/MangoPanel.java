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

package com.gengoai.swing.component;

import com.gengoai.conversion.Cast;
import com.gengoai.swing.View;

import javax.swing.*;
import java.awt.*;

public class MangoPanel extends JPanel {
   private static final long serialVersionUID = 1L;

   public MangoPanel() {
      super(true);
      setLayout(new BorderLayout());
   }

   @Override
   public Component add(Component component) {
      if(component instanceof View) {
         return super.add(Cast.<View>as(component).getRoot());
      }
      return super.add(component);
   }

   @Override
   public Component add(String name, Component component) {
      if(component instanceof View) {
         return super.add(name, Cast.<View>as(component).getRoot());
      }
      return super.add(name, component);
   }

   @Override
   public Component add(Component component, int index) {
      if(component instanceof View) {
         return super.add(Cast.<View>as(component).getRoot(), index);
      }
      return super.add(component, index);
   }

   @Override
   public void add(Component component, Object constraints) {
      if(component instanceof View) {
         super.add(Cast.<View>as(component).getRoot(), constraints);
      } else {
         super.add(component, constraints);
      }
   }

   @Override
   public void add(Component component, Object constraints, int index) {
      if(component instanceof View) {
         super.add(Cast.<View>as(component).getRoot(), constraints, index);
      } else {
         super.add(component, constraints, index);
      }
   }

}//END OF MangoPanel
