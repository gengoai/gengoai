package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.morphology.PartOfSpeech;
import com.gengoai.hermes.morphology.TokenType;
import com.gengoai.swing.component.MangoTable;
import lombok.NonNull;

import static com.gengoai.tuple.Tuples.$;

public class TokenAnnotationTable extends MangoTable {

    public TokenAnnotationTable(HString hString) {
        super($("Start", Integer.class),
              $("End", Integer.class),
              $("POS", PartOfSpeech.class),
              $("TokenType", TokenType.class),
              $("Token", String.class));
        for (Annotation annotation : hString.tokens()) {
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
            c.setPreferredWidth(100);
            c.setMaxWidth(100);
            c.setResizable(false);
        });
        withColumn(3, c -> {
            c.setPreferredWidth(150);
            c.setMaxWidth(200);
            c.setResizable(false);
        });
    }

    public void addAnnotation(@NonNull Annotation annotation) {
        addRow(annotation.start(),
               annotation.end(),
               annotation.pos(),
               annotation.attribute(Types.TOKEN_TYPE),
               annotation.toString());
    }

}//END OF EntityAnnotationTable
