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

package com.gengoai.hermes.tools.swing;

import com.gengoai.SystemInfo;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.extraction.lyre.LyreExpression;
import com.gengoai.hermes.lexicon.*;
import com.gengoai.io.FileUtils;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Menus;
import com.gengoai.swing.component.MangoPanel;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.listener.SwingListeners;
import com.gengoai.swing.component.model.MangoTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import static com.gengoai.swing.component.Components.button;
import static com.gengoai.tuple.Tuples.$;

public class LexiconEditor extends HermesGUI {
    private static final int SMALL_ICON_SIZE = 16;
    private static final int LARGE_ICON_SIZE = 32;
    private static final FileFilter ALL_FILE_FIlTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getAbsoluteFile().toString().toLowerCase().endsWith(".lexicon") ||
                    file.getAbsoluteFile().toString().toLowerCase().endsWith(".json");
        }

        @Override
        public String getDescription() {
            return "All Lexicon Files";
        }
    };
    private final JFileChooser openDialog = new JFileChooser();
    private Lexicon lexicon;
    private final JComboBox<String> edtTagComboBox = new JComboBox<>();
    private File currentLexiconFile;
    private final MangoTableModel tblModel = new MangoTableModel(
            $("Lemma", String.class, false),
            $("Tag", String.class, true),
            $("Constraint", String.class, true)
    );
    private final MangoTable tblView = new MangoTable(tblModel);

    private final FluentAction faOpen = new FluentAction("Open", this::openLexicon)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.FOLDER_OPEN.createIcon(SMALL_ICON_SIZE))
            .largeIcon(FontAwesome.FOLDER_OPEN.createIcon(LARGE_ICON_SIZE));
    private final FluentAction faSave = new FluentAction("Save", this::saveLexicon)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.SAVE.createIcon(SMALL_ICON_SIZE))
            .largeIcon(FontAwesome.SAVE.createIcon(LARGE_ICON_SIZE));

    private final FluentAction faNewEntry = new FluentAction("New Entry", this::newEntry)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.PLUS.createIcon(SMALL_ICON_SIZE))
            .largeIcon(FontAwesome.PLUS.createIcon(LARGE_ICON_SIZE));

    private final FluentAction faDeleteEntry = new FluentAction("Delete", this::deleteEntry)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.MINUS.createIcon(SMALL_ICON_SIZE))
            .largeIcon(FontAwesome.MINUS.createIcon(LARGE_ICON_SIZE));

    public static void main(String[] args) {
        runApplication(LexiconEditor::new, "LexiconEditor", "Hermes Lexicon Editor", args);
    }

    @Override
    protected void initControls() throws Exception {
        faDeleteEntry.setEnabled(false);
        mainWindowFrame.setTitle("Hermes Lexicon Editor");
        faSave.setEnabled(false);
        faNewEntry.setEnabled(false);
        edtTagComboBox.setEditable(true);
        openDialog.setCurrentDirectory(properties.get("open_dialog_directory").as(File.class,
                new File(SystemInfo.USER_HOME)));

        menuBar(Menus.menu("File",
                'F',
                Menus.menuItem(faOpen),
                Menus.menuItem(faSave),
                new JPopupMenu.Separator(),
                Menus.menuItem(new FluentAction(
                        "Exit",
                        (a) -> System.exit(0)
                ))
        ));
        toolBar(button(faOpen, false, true, 0),
                button(faSave, false, true, 0),
                null,
                button(faNewEntry, false, true, 0),
                button(faDeleteEntry, false, true, 0));

        tblView.setAutoCreateRowSorter(true);
        tblView.setRowHeightPadding(10);
        tblView.getTableHeader().setReorderingAllowed(false);
        tblView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblView.addMouseListener(SwingListeners.mouseClicked(this::onTableClick));
        tblView.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(edtTagComboBox));

        //Alternate row colors for tables
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        if (defaults.get("Table.alternateRowColor") == null)
            defaults.put("Table.alternateRowColor", new Color(240, 240, 240));

        tblModel.addTableModelListener(tableModelEvent -> {
            if (tableModelEvent.getColumn() == 1) {
                int effectiveRow = tableModelEvent.getFirstRow();
                String tag = tblModel.getValueAt(effectiveRow, 1).toString();
                edtTagComboBox.removeItem(tag);
                int size = edtTagComboBox.getModel().getSize();
                int index = 0;
                for (index = 0; index < size; index++) {
                    Comparable c = (Comparable) edtTagComboBox.getModel().getElementAt(index);
                    if (c.compareTo(tag) > 0) {
                        break;
                    }
                }
                DefaultComboBoxModel<String> m = Cast.as(edtTagComboBox.getModel());
                m.insertElementAt(tag, index);
                edtTagComboBox.setSelectedIndex(index);
                faSave.setEnabled(true);
            }
        });
        this.mainWindowFrame.add(new

                JScrollPane(tblView), BorderLayout.CENTER);

        MangoPanel editPanel = new MangoPanel();
        this.mainWindowFrame.add(editPanel, BorderLayout.SOUTH);
    }


    private void onTableClick(MouseEvent e) {
        faDeleteEntry.setEnabled(tblView.getSelectedRow() >= 0);
    }

    private void deleteEntry(ActionEvent e) {
        if (tblView.getSelectedRow() >= 0) {
            int effectiveRow = tblView.getRowSorter().convertRowIndexToModel(tblView.getSelectedRow());
            tblView.getModel().removeRow(effectiveRow);
            faSave.setEnabled(true);
        }
    }

    private void newEntry(ActionEvent e) {
        String result = (String) JOptionPane.showInputDialog(
                mainWindowFrame,
                "Enter the Lemma",
                "New Lexicon Entry",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );
        if (Strings.isNotNullOrBlank(result)) {
            tblView.addRow(result, "", "");
            int effectiveRow = tblView.getRowSorter().convertRowIndexToView(tblModel.getRowCount() - 1);
            tblView.getSelectionModel().setSelectionInterval(effectiveRow, effectiveRow);
            tblView.scrollRectToVisible(new Rectangle(tblView.getCellRect(effectiveRow, 0, true)));
            faSave.setEnabled(true);
        }
    }

    private void saveLexicon(ActionEvent e) {

        //Make backup
        Resource lexFile = Resources.fromFile(currentLexiconFile);
        Resource backup = Resources.from(currentLexiconFile.getAbsolutePath() + ".bak");

        try {
            lexFile.copy(backup);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        TrieLexicon newLexicon = new TrieLexicon(
                lexicon.getName(),
                lexicon.isCaseSensitive()
        );
        for (int i = 0; i < tblView.getRowCount(); i++) {
            final String lemma = tblModel.getValueAt(i, 0).toString();
            final String tag = tblModel.getValueAt(i, 1).toString();
            final String constraint = tblModel.getValueAt(i, 2).toString();
            newLexicon.add(LexiconEntry.of(
                    lemma,
                    1.0,
                    tag,
                    Strings.isNullOrBlank(constraint) ? null : LyreExpression.parse(constraint),
                    lemma.split("\\s+").length
            ));
        }

        try {
            LexiconIO.write(newLexicon, lexFile, "");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        lexicon = newLexicon;
        faSave.setEnabled(false);
    }

    @Override
    protected void onClose() throws Exception {
        super.onClose();
        if (openDialog.getSelectedFile() != null) {
            properties.set("open_dialog_directory", openDialog.getSelectedFile().getParentFile().getAbsolutePath());
        }
    }

    private void openLexicon(ActionEvent e) {
        openDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openDialog.setMultiSelectionEnabled(false);
        openDialog.setFileFilter(ALL_FILE_FIlTER);
        if (openDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            currentLexiconFile = openDialog.getSelectedFile();
            StringBuilder spec = new StringBuilder();
            if (currentLexiconFile.getAbsoluteFile().toString().toLowerCase().endsWith(".json")) {
                spec.append("lexicon:mem:")
                        .append(FileUtils.baseName(Resources.fromFile(currentLexiconFile).baseName(), ".json"))
                        .append(":json::");
            } else {
                spec.append("lexicon:disk::");
            }
            spec.append("/").append(currentLexiconFile.getAbsoluteFile());
            LexiconSpecification lexiconSpec = LexiconSpecification.parse(spec.toString());
            try {
                lexicon = lexiconSpec.create();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Set<String> tags = new TreeSet<>();
            tblView.clear();
            for (LexiconEntry entry : lexicon.entries()) {
                tags.add(entry.getTag());
                tblView.addRow(entry.getLemma(),
                        entry.getTag(),
                        entry.getConstraint() == null ? "" : entry.getConstraint().toString());
            }

            while (edtTagComboBox.getModel().getSize() > 0) {
                edtTagComboBox.removeItem(edtTagComboBox.getModel().getElementAt(0));
            }
            tags.forEach(edtTagComboBox::addItem);
            faNewEntry.setEnabled(true);
            faDeleteEntry.setEnabled(false);
        }
    }


}
