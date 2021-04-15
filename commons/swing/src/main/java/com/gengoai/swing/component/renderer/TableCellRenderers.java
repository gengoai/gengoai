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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public final class TableCellRenderers {

   public static final TableCellRenderer CENTER_ALIGN = new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;

      {
         super.setHorizontalAlignment(SwingConstants.CENTER);
      }

      @Override
      public void setHorizontalAlignment(int alignment) {

      }
   };
   public static final TableCellRenderer LEFT_ALIGN = new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;

      {
         super.setHorizontalAlignment(SwingConstants.LEFT);
      }

      @Override
      public void setHorizontalAlignment(int alignment) {

      }
   };
   public static final TableCellRenderer RIGHT_ALIGN = new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;

      {
         super.setHorizontalAlignment(SwingConstants.RIGHT);
      }

      @Override
      public void setHorizontalAlignment(int alignment) {

      }
   };

   private TableCellRenderers() {
      throw new IllegalAccessError();
   }

}//END OF TableCellRenderers
