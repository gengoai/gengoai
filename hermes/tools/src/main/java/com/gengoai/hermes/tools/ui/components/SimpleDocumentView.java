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

package com.gengoai.hermes.tools.ui.components;

import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

import static com.gengoai.function.Functional.with;
import static com.gengoai.tuple.Tuples.$;

public class SimpleDocumentView extends JPanel {

   private final HStringViewer taDocument;

   private final JTabbedPane tabBottomTools = new JTabbedPane();
   private final JSplitPane spSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);


   public SimpleDocumentView(final Document document) {
      setLayout(new BorderLayout());
      this.taDocument = new HStringViewer(document);
      Fonts.setFontSize(this.taDocument, 34.0f);
      spSplit.setResizeWeight(1.0);
      spSplit.setDividerLocation(0.5);
      spSplit.setTopComponent(taDocument);
      spSplit.setBottomComponent(tabBottomTools);
      taDocument.setText(document.toString());
      add(spSplit, BorderLayout.CENTER);

      var tblSentences = with(new MangoTable($("Start", Integer.class),
                                             $("End", Integer.class),
                                             $("Sentence", String.class)), $ -> {
         $.setAutoCreateRowSorter(true);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(false);
         $.setAlternateRowColor(Color::brighter);
         $.setRowHeightPadding(3);
         $.setReorderingAllowed(false);
         $.withColumn(0, c -> c.setMaxWidth(100));
         $.withColumn(1, c -> c.setMaxWidth(100));
      });
      for (Annotation sentence : document.sentences()) {
         tblSentences.addRow(sentence.start(), sentence.end(), sentence.toString());
      }
      tblSentences.addMouseListener(SwingListeners.mouseClicked(this::tblOnClickSelection));

      tabBottomTools.addTab("Sentences", new JScrollPane(tblSentences));
      var tblTokens = with(new MangoTable($("Start", Integer.class),
                                          $("End", Integer.class),
                                          $("Token", String.class),
                                          $("POS", String.class)), $ -> {
         $.setAutoCreateRowSorter(true);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(false);
         $.setAlternateRowColor(Color::brighter);
         $.setRowHeightPadding(3);
         $.setReorderingAllowed(false);
         $.withColumn(0, c -> c.setMaxWidth(100));
         $.withColumn(1, c -> c.setMaxWidth(100));
      });
      for (Annotation token : document.tokens()) {
         tblTokens.addRow(token.start(), token.end(), token.toString(), token.pos().toString());
      }
      tblTokens.addMouseListener(SwingListeners.mouseClicked(this::tblOnClickSelection));
      tabBottomTools.addTab("Tokens", new JScrollPane(tblTokens));
      var tblChunks = with(new MangoTable($("Start", Integer.class),
                                          $("End", Integer.class),
                                          $("Chunk", String.class),
                                          $("POS", String.class)), $ -> {
         $.setAutoCreateRowSorter(true);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(false);
         $.setAlternateRowColor(Color::brighter);
         $.setRowHeightPadding(3);
         $.setReorderingAllowed(false);
         $.withColumn(0, c -> c.setMaxWidth(100));
         $.withColumn(1, c -> c.setMaxWidth(100));
      });
      for (Annotation entity : document.annotations(Types.PHRASE_CHUNK)) {
         tblChunks.addRow(entity.start(), entity.end(), entity.toString(), entity.pos().toString());
      }
      tblChunks.addMouseListener(SwingListeners.mouseClicked(this::tblOnClickSelection));
      tabBottomTools.addTab("Phrase Chunks", new JScrollPane(tblChunks));

      var tblEntities = with(new MangoTable($("Start", Integer.class),
                                            $("End", Integer.class),
                                            $("Entity", String.class),
                                            $("Type", String.class),
                                            $("Confidence", Double.class)), $ -> {
         $.setAutoCreateRowSorter(true);
         $.setFillsViewportHeight(true);
         $.setAutoCreateRowSorter(false);
         $.setAlternateRowColor(Color::brighter);
         $.setRowHeightPadding(3);
         $.setReorderingAllowed(false);
         $.withColumn(0, c -> c.setMaxWidth(100));
         $.withColumn(1, c -> c.setMaxWidth(100));
      });
      for (Annotation entity : document.annotations(Types.ENTITY)) {
         tblEntities.addRow(entity.start(),
                            entity.end(),
                            entity.toString(),
                            entity.getTag().toString(),
                            entity.attribute(Types.CONFIDENCE, 0d));
      }
      tblEntities.addMouseListener(SwingListeners.mouseClicked(this::tblOnClickSelection));
      tabBottomTools.addTab("Entities", new JScrollPane(tblEntities));
   }

   private void tblOnClickSelection(MouseEvent l){
      MangoTable mt = Cast.as(l.getSource());
      int index = mt.getRowSorter().convertRowIndexToView(mt.getSelectedRow());
      int start = (Integer) mt.getValueAtModel(index, 0);
      int end = (Integer) mt.getValueAtModel(index, 1);
      taDocument.setSelectionRange(start, end);
   }

}
