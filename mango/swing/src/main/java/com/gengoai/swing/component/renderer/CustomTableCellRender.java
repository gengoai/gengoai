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

package com.gengoai.swing.component.renderer;

import com.gengoai.swing.Fonts;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class CustomTableCellRender extends DefaultTableCellRenderer {
   private Integer fontStyle = null;
   private Float fontSize = null;

   public CustomTableCellRender alignment(int alignment) {
      setHorizontalAlignment(alignment);
      return this;
   }

   public CustomTableCellRender background(Color color) {
      setBackground(color);
      return this;
   }

   public CustomTableCellRender fontSize(float size) {
      fontSize = size;
      return this;
   }

   public CustomTableCellRender fontStyle(int style) {
      fontStyle = style;
      return this;
   }

   public CustomTableCellRender foreground(Color color) {
      setForeground(color);
      return this;
   }

   @Override
   public Component getTableCellRendererComponent(JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row,
                                                  int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if(fontSize != null) {
         Fonts.setFontSize(this, fontSize);
      }
      if(fontStyle != null) {
         Fonts.setFontStyle(this, fontStyle);
      }
      return this;
   }
}//END OF CustomTableCellRender
