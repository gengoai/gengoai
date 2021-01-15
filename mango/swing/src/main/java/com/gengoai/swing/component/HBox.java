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

import com.gengoai.swing.View;

import javax.swing.JPanel;
import java.awt.*;

/**
 * A panel implementation that stacks components horizontally. A "resizeWith" component can be specified that will be
 * resized to take up any extra space within the panel
 */
public class HBox extends JPanel {
   private final GridBagLayout layout = new GridBagLayout();
   private final GridBagConstraints gbc = new GridBagConstraints();
   private final Insets firstColInsets = new Insets(0, 0, 0, 0);
   private final Insets otherColInsets;
   private int nextCol = 0;

   /**
    * Instantiates a HBox with no gap
    */
   public HBox() {
      this(0, 0);
   }

   /**
    * Instantiates a HBox with a gap of the given size
    *
    * @param gap the gap
    */
   public HBox(int gap, int componentPadding) {
      super.setLayout(layout);
      setOpaque(false);
      otherColInsets = new Insets(0, gap, 0, 0);
      gbc.insets = firstColInsets;
      gbc.ipady = componentPadding;
      gbc.anchor = GridBagConstraints.LINE_START;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 0.0;
      gbc.weighty = 0.0;
      gbc.gridy = 0;
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
      add(comp, resizeWith, -1);
   }

   @Override
   public void add(Component comp, Object resizeWith, int index) {
      if(resizeWith instanceof Boolean) {
         if(comp instanceof View) {
            comp = ((View) comp).getRoot();
         }
         gbc.gridx = (index < 0
                      ? nextCol
                      : index);

         if(getComponentCount() > gbc.gridx) {
            Component c = getComponent(gbc.gridx);
            GridBagConstraints copy = layout.getConstraints(c);
            copy.gridx++;
            layout.setConstraints(c, copy);
         }
         if(gbc.gridx == 0) {
            gbc.insets = firstColInsets;
         } else {
            gbc.insets = otherColInsets;
         }
         gbc.weightx = (Boolean) resizeWith
                       ? 4.0
                       : 0.0;
         super.add(comp, gbc, index);
         gbc.weightx = 0;
         nextCol = getComponentCount();
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
      copy.weightx = 4.0;
      layout.setConstraints(component, copy);
      invalidate();
      repaint();
   }

}//END OF HBox
