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

import com.gengoai.ParamMap;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyViewer extends JTable {
   private static final long serialVersionUID = 1L;
   private final KeyValeModel model;

   public PropertyViewer(ParamMap<?> properties) {
      this.model = new KeyValeModel(properties);
      this.setModel(model);
      this.setShowGrid(true);
      this.setFillsViewportHeight(true);
      this.getTableHeader().setReorderingAllowed(false);
      this.setAutoCreateRowSorter(true);
      this.getRowSorter().toggleSortOrder(0);

   }

   @Override
   public TableCellEditor getCellEditor(int row, int column) {
      row = convertRowIndexToModel(row);
      column = convertColumnIndexToModel(column);
      Class<?> clazz = model.getColumnClass(row, column);
      if (clazz == Boolean.class || clazz == boolean.class) {
         final JCheckBox cb = new JCheckBox();
         cb.setHorizontalAlignment(JLabel.CENTER);
         return new DefaultCellEditor(cb);
      }
      return super.getCellEditor(row, column);
   }

   @Override
   public TableCellRenderer getCellRenderer(int row, int column) {
      if (column == 1) {
         row = convertRowIndexToModel(row);
         column = convertColumnIndexToModel(column);
         Class<?> clazz = model.getColumnClass(row, column);
         if (clazz == Boolean.class || clazz == boolean.class) {
            return new CheckBoxCell();
         }
      }
      return new DefaultTableCellRenderer();
   }

   private static class CheckBoxCell extends JCheckBox implements TableCellRenderer {
      private static final long serialVersionUID = 1L;

      public CheckBoxCell() {
         setHorizontalAlignment(JLabel.CENTER);
      }

      @Override
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
         if (isSelected) {
            setForeground(table.getSelectionForeground());
            //super.setBackground(table.getSelectionBackground());
            setBackground(table.getSelectionBackground());
         } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
         }
         setSelected((value != null && ((Boolean) value).booleanValue()));
         return this;
      }
   }

   private static class KeyValeModel extends AbstractTableModel {
      private static final long serialVersionUID = 1L;
      private final ParamMap<?> properties;
      private final List<String> keys;

      private KeyValeModel(ParamMap<?> properties) {
         this.properties = properties;
         this.keys = new ArrayList<>(properties.parameterNames());
      }

      public Class<?> getColumnClass(int row, int column) {
         return properties.getParam(keys.get(row)).type;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
         switch (columnIndex) {
            case 0:
            case 1:
               return Object.class;
            default:
               throw new IndexOutOfBoundsException();
         }
      }

      @Override
      public int getColumnCount() {
         return 2;
      }

      @Override
      public String getColumnName(int columnIndex) {
         switch (columnIndex) {
            case 0:
               return "Name";
            case 1:
               return "Value";
            default:
               throw new IndexOutOfBoundsException();
         }
      }

      @Override
      public int getRowCount() {
         return keys.size();
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
         switch (columnIndex) {
            case 0:
               return keys.get(rowIndex);
            case 1:
               return properties.get(keys.get(rowIndex));
            default:
               throw new IndexOutOfBoundsException();
         }
      }

      @Override
      public boolean isCellEditable(int rowIndex, int columnIndex) {
         return columnIndex == 1;
      }

      @Override
      public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         try {
            properties.set(keys.get(rowIndex), aValue);
         } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                                          String.format("Invalid Value for %s of type %s",
                                                        keys.get(rowIndex),
                                                        properties.getParam(keys.get(rowIndex)).type.getSimpleName()),
                                          "Error Setting Value",
                                          JOptionPane.ERROR_MESSAGE
            );
         }
      }
   }

}//END OF PropertyViewer
