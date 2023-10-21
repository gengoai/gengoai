package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.EntityType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.swing.Colors;
import com.gengoai.swing.component.MangoTable;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static com.gengoai.tuple.Tuples.$;

public class EntityAnnotationTable extends MangoTable {
    private final EntityColors entityColors = new EntityColors();

    public EntityAnnotationTable(HString hString) {
        super($("Start", Integer.class),
              $("End", Integer.class),
              $("Type", EntityType.class),
              $("Confidence", Double.class),
              $("Entity", String.class));
        for (Annotation annotation : hString.entities()) {
            addAnnotation(annotation);
        }
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);
        setRowHeightPadding(3);
        setShowGrid(true);
        setReorderingAllowed(false);
        withColumn(0, c -> {
            c.setPreferredWidth(75);
            c.setMaxWidth(75);
            c.setResizable(false);
        });
        withColumn(1, c -> {
            c.setPreferredWidth(75);
            c.setMaxWidth(75);
            c.setResizable(false);
        });
        withColumn(2, c -> {
            c.setMaxWidth(350);
            c.setPreferredWidth(350);
            c.setCellRenderer(new AnnotationTableCellRender());
            c.setResizable(true);
        });
        withColumn(3, c -> {
            c.setPreferredWidth(150);
            c.setMaxWidth(150);
            c.setResizable(true);
        });
    }

    public void addAnnotation(@NonNull Annotation annotation) {
        addRow(annotation.start(),
               annotation.end(),
               annotation.attribute(Types.ENTITY_TYPE),
               annotation.attribute(Types.CONFIDENCE, 1.0),
               annotation.toString());
    }

    private class AnnotationTableCellRender extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
            EntityType tag = Cast.as(value);
            setText(tag.label());
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                Color bg = entityColors.get(tag);
                setForeground(Colors.calculateBestFontColor(bg));
                setBackground(bg);
            }
            return this;
        }
    }
}//END OF EntityAnnotationTable
