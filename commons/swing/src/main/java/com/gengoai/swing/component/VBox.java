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

import java.awt.*;

/**
 * A panel implementation that stacks components vertically. A "resizeWith" component can be specified that will be
 * resized to take up any extra space within the panel
 */
public class VBox extends MangoPanel {
   private static final long serialVersionUID = 1L;
   private final GridBagLayout layout = new GridBagLayout();
   private final GridBagConstraints gbc = new GridBagConstraints();
   private final Insets firstRowInsets = new Insets(0, 0, 0, 0);
   private final Insets otherRowInsets;
   private int nextRow = 0;

   /**
    * Instantiates a VBox with no gap
    */
   public VBox() {
      this(0, 0);
   }

   /**
    * Instantiates a VBox with a gap of the given size
    *
    * @param gap the gap
    */
   public VBox(int gap, int componentPadding) {
      super.setLayout(layout);
      setOpaque(false);
      otherRowInsets = new Insets(gap, 0, 0, 0);
      gbc.insets = firstRowInsets;
      gbc.ipady = componentPadding;
      gbc.anchor = GridBagConstraints.PAGE_START;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 1.0;
   }

   @Override
   public Component add(Component comp) {
      add(comp, Boolean.FALSE);
      return comp;
   }

   @Override
   public Component add(String name, Component comp) {
      add(comp, Boolean.FALSE);
      comp.setName(name);
      return comp;
   }

   @Override
   public Component add(Component comp, int index) {
      add(comp, Boolean.FALSE, index);
      return comp;
   }

   @Override
   public void add(Component comp, Object resizeWith) {
      if(resizeWith instanceof Boolean) {
         add(comp, resizeWith, -1);
      } else {
         throw new IllegalArgumentException("Expecting a boolean");
      }
   }

   @Override
   public void add(Component comp, Object resizeWith, int index) {
      if(resizeWith instanceof Boolean) {
         gbc.gridy = (index < 0
                      ? nextRow
                      : index);
         if(getComponentCount() > gbc.gridy) {
            Component c = getComponent(gbc.gridy);
            GridBagConstraints copy = layout.getConstraints(c);
            copy.gridy++;
            layout.setConstraints(c, copy);
         }
         if(gbc.gridy == 0) {
            gbc.insets = firstRowInsets;
         } else {
            gbc.insets = otherRowInsets;
         }
         gbc.weighty = (Boolean) resizeWith
                       ? 4.0
                       : 0.0;
         super.add(comp, gbc, index);
         gbc.weighty = 0;

         nextRow = getComponentCount();
      } else {
         throw new IllegalArgumentException("Expecting a boolean");
      }
   }

   @Override
   public void setLayout(LayoutManager mgr) {

   }

   /**
    * Sets the index of the component to resize to fill the extra space of this pane
    *
    * @param index the index
    */
   public void setResizeWithComponent(int index) {
      setResizeWithComponent(getComponent(index));
   }

   /**
    * Sets the component to resize to fill the extra space of this pane
    *
    * @param component the component
    */
   public void setResizeWithComponent(Component component) {
      GridBagConstraints copy = layout.getConstraints(component);
      copy.weighty = 4.0;
      layout.setConstraints(component, copy);
      invalidate();
      repaint();
   }

}//END OF VBox
