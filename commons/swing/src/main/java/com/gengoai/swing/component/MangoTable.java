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
import com.gengoai.swing.Colors;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.component.model.MangoTableModel;
import com.gengoai.tuple.Tuple2;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The type Mango table.
 */
public class MangoTable extends JTable {
   private static final long serialVersionUID = 1L;
   @Getter
   private Color alternateRowColor;
   @Getter
   private Color rowColor;
   private MangoTableModel model;

   /**
    * Instantiates a new Mango table.
    */
   public MangoTable() {
      this(new MangoTableModel());
   }

   /**
    * Instantiates a new Mango table.
    *
    * @param columns the columns
    */
   public MangoTable(@NonNull String... columns) {
      this(new MangoTableModel(columns));
   }

   /**
    * Instantiates a new Mango table.
    *
    * @param columns the columns
    */
   @SafeVarargs
   public MangoTable(@NonNull Tuple2<String, Class<?>>... columns) {
      this(new MangoTableModel(columns));
   }


   @Override
   public MangoTableModel getModel() {
      return Cast.as(super.getModel());
   }

   /**
    * Instantiates a new Mango table.
    *
    * @param dm the dm
    */
   public MangoTable(@NonNull MangoTableModel dm) {
      super(dm);
      this.model = dm;
   }


   /**
    * Add all rows.
    *
    * @param rows the rows
    */
   public void addAllRows(Object[][] rows) {
      model.addAllRows(rows);
   }

   /**
    * Add all rows.
    *
    * @param rows the rows
    */
   public void addAllRows(List<List<?>> rows) {
      model.addAllRows(rows);
   }

   /**
    * Add row.
    *
    * @param row the row
    */
   public void addRow(Object... row) {
      model.addRow(row);
   }

   /**
    * Add row.
    *
    * @param row the row
    */
   public void addRow(List<?> row) {
      model.addRow(row);
   }

   /**
    * Clear.
    */
   public void clear() {
      model.setRowCount(0);
   }

   /**
    * Gets value at index.
    *
    * @param row    the row
    * @param column the column
    * @return the value at index
    */
   public Object getValueAtIndex(int row, int column) {
      return getModel().getValueAt(convertRowIndexToModel(row),
                                   convertColumnIndexToModel(column));
   }

   /**
    * Gets value at model.
    *
    * @param row    the row
    * @param column the column
    * @return the value at model
    */
   public Object getValueAtModel(int row, int column) {
      return getModel().getValueAt(row, column);
   }

   /**
    * On selection changed.
    *
    * @param consumer the consumer
    */
   public void onSelectionChanged(@NonNull ListSelectionListener consumer) {
      this.getSelectionModel().addListSelectionListener(consumer);
   }

   @Override
   public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
      Component comp = super.prepareRenderer(renderer, row, column);
      if (getSelectedRow() != row &&
          (comp.getBackground().equals(getBackground()) ||
           comp.getBackground().equals(alternateRowColor)
          )) {
         if (rowColor == null) {
            rowColor = comp.getBackground();
         }
         if (alternateRowColor == null) {
            alternateRowColor = comp.getBackground();
         }
         Color bg = row % 2 == 0
               ? rowColor
               : alternateRowColor;
         comp.setBackground(bg);
         if (comp.getForeground().equals(getForeground())) {
            comp.setForeground(Colors.calculateBestFontColor(comp.getBackground()));
         }
         bg = null;
      }
      return comp;
   }

   /**
    * Resize column width.
    *
    * @param minWidth the min width
    */
   public void resizeColumnWidth(int minWidth) {
      final TableColumnModel columnModel = getColumnModel();
      for (int column = 0; column < getColumnCount(); column++) {
         int width = minWidth;
         for (int row = 0; row < getRowCount(); row++) {
            TableCellRenderer renderer = getCellRenderer(row, column);
            Component comp = prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width + 1, width);
         }
         columnModel.getColumn(column).setPreferredWidth(width);
      }
   }

   /**
    * Sets alternate row color.
    *
    * @param colorConverter the color converter
    * @return the alternate row color
    */
   public MangoTable setAlternateRowColor(@NonNull Function<Color, Color> colorConverter) {
      this.alternateRowColor = colorConverter.apply(getBackground());
      return this;
   }

   /**
    * Sets alternate row color.
    *
    * @param color the color
    * @return the alternate row color
    */
   public MangoTable setAlternateRowColor(Color color) {
      this.alternateRowColor = color;
      return this;
   }

   /**
    * Sets header is visible.
    *
    * @param isVisible the is visible
    */
   public void setHeaderIsVisible(boolean isVisible) {
      if (isVisible && getTableHeader() == null) {
         setTableHeader(new JTableHeader());
         for (int i = 0; i < getModel().getColumnCount(); i++) {
            getTableHeader().getColumnModel()
                            .getColumn(i)
                            .setHeaderValue(getModel().getColumnName(i));
         }
      } else if (!isVisible) {
         setTableHeader(null);
      }
   }

   @Override
   public void setModel(TableModel model) {
      if (model instanceof MangoTableModel) {
         this.model = Cast.as(model);
         super.setModel(model);
      } else {
         throw new IllegalArgumentException("Model must be an instance of MangoTableModel");
      }
   }

   /**
    * Sets reordering allowed.
    *
    * @param isReorderingAllowed the is reordering allowed
    */
   public void setReorderingAllowed(boolean isReorderingAllowed) {
      if (getTableHeader() != null) {
         this.getTableHeader().setReorderingAllowed(isReorderingAllowed);
      }
   }

   /**
    * Sets row color.
    *
    * @param color the color
    */
   public void setRowColor(Color color) {
      this.rowColor = color;
   }

   /**
    * Sets row height padding.
    *
    * @param amount the amount
    */
   public void setRowHeightPadding(int amount) {
      this.setRowHeight(Fonts.getFontHeight(this) + amount);
   }

   /**
    * With column.
    *
    * @param index    the index
    * @param consumer the consumer
    */
   public void withColumn(int index, @NonNull Consumer<TableColumn> consumer) {
      consumer.accept(getColumnModel().getColumn(index));
   }

}//END OF FluentJTable
