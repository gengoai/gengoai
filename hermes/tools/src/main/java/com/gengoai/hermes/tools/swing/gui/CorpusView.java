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

package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.swing.Colors;
import com.gengoai.swing.ComponentStyle;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.TextAlignment;
import com.gengoai.swing.component.Components;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.VBox;
import com.gengoai.swing.component.listener.SwingListeners;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.Fonts.*;
import static com.gengoai.swing.component.Components.*;
import static javax.swing.BorderFactory.*;

public class CorpusView extends JPanel {
    @Getter
    private final Corpus corpus;
    @Getter
    private final String corpusSpecification;
    private final AtomicReference<String> selectedDocumentId = new AtomicReference<>();
    @Setter
    private Consumer<String> onDocumentOpen = System.out::println;

    public CorpusView(String corpusSpecification, Corpus corpus) {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.corpusSpecification = corpusSpecification;
        this.corpus = corpus;
        add(createStatsPanel(), BorderLayout.NORTH);
        add(createDocumentPicker(), BorderLayout.CENTER);
    }

    private JPanel createDocumentPicker() {
        var tblDocumentIds = with(new MangoTable("Document Id"), $ -> {
            $.setAlternateRowColor(Color::darker);
            $.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            $.addMouseListener(SwingListeners.mouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    performDocumentOpen();
                } else {
                    selectedDocumentId.set($.getValueAt($.getSelectedRow(), 0).toString());
                }
            }));
        });
        corpus.getIds().forEach(tblDocumentIds::addRow);

        var vbox = new VBox(5, 0);

        var filter = panelHBox(5, 0, $hbox -> {
            $hbox.add(setFontStyle(new JLabel("Filter:"), Font.BOLD));
            var txtField = new JTextField();
            txtField.addKeyListener(SwingListeners.keyReleased(e -> {
                if (txtField.getText().length() >= 3) {
                    TableRowSorter<TableModel> sorter = new TableRowSorter<>(tblDocumentIds.getModel());
                    sorter.setRowFilter(RowFilter.regexFilter("^" + txtField.getText()));
                    tblDocumentIds.setRowSorter(sorter);
                } else {
                    tblDocumentIds.setRowSorter(null);
                }
            }));
            $hbox.add(txtField, true);
        });

        vbox.add(filter);
        vbox.add(scrollPaneNoBorder(tblDocumentIds), true);
        return vbox;
    }

    private JPanel createStatsPanel() {
        return Components.panelVBox($ -> {
            $.setBorder(createCompoundBorder(createLineBorder($.getBackground().brighter(), 2),
                                             createEmptyBorder(12, 12, 12, 12)));
            $.setBackground($.getBackground().darker());

            final var styler = new ComponentStyle<JLabel>($lbl -> {
                Fonts.setMinFontSize($lbl, 18);
                Fonts.setFontStyle($lbl, Font.BOLD);
                $lbl.setBackground($.getBackground());
                $lbl.setBorder(BorderFactory.createEmptyBorder());
                $lbl.setForeground(Colors.calculateBestFontColor($.getBackground()));
            });

            final JLabel lblCSName;
            $.add(panelHBox(5, 0, lblCSName = with(new JLabel("Corpus Specification:"),
                                                   $lbl -> {
                                                       styler.style($lbl);
                                                       $lbl.setPreferredSize(dim(getFontWidth($lbl, $lbl.getText()),
                                                                                 getFontHeight($lbl)));
                                                       TextAlignment.HorizontalRight.set($lbl);
                                                   }),
                            with(new JLabel(corpusSpecification),
                                 $lbl -> styler
                                         .style($lbl)
                                         .setPreferredSize(dim(getAverageFontWidth($lbl) * 40, getFontHeight($lbl))))));
            $.add(panelHBox(5, 0,
                            with(new JLabel("# of Documents:"),
                                 $lbl -> {
                                     styler.style($lbl);
                                     $lbl.setPreferredSize(dim(getFontWidth($lbl, lblCSName.getText()),
                                                               getFontHeight($lbl)));
                                     $lbl.setHorizontalAlignment(JLabel.RIGHT);
                                 }),
                            with(new JLabel(DecimalFormat.getIntegerInstance().format(corpus.size())),
                                 $lbl -> styler
                                         .style($lbl)
                                         .setPreferredSize(dim(getAverageFontWidth($lbl) * 40,
                                                               getFontHeight($lbl))))));
        });
    }

    private void performDocumentOpen() {
        if (onDocumentOpen != null) {
            onDocumentOpen.accept(selectedDocumentId.get());
        }
    }

}//END OF CorpusView
