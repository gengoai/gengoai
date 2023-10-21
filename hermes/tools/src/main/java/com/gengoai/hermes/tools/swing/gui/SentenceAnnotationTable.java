package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.swing.component.MangoTable;
import lombok.NonNull;

import static com.gengoai.tuple.Tuples.$;

public class SentenceAnnotationTable extends MangoTable {
    public SentenceAnnotationTable(HString hString) {
        super($("Start", Integer.class),
              $("End", Integer.class),
              $("Sentence", String.class));
        for (Annotation annotation : hString.sentences()) {
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
    }

    public void addAnnotation(@NonNull Annotation annotation) {
        addRow(annotation.start(),
               annotation.end(),
               annotation.toString());
    }

}//END OF SentenceAnnotationTable
