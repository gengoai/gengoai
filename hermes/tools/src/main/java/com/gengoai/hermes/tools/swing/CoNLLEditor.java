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

import com.gengoai.HierarchicalEnumValue;
import com.gengoai.SystemInfo;
import com.gengoai.collection.multimap.HashSetMultimap;
import com.gengoai.collection.multimap.Multimap;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.SuperSense;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import com.gengoai.reflection.Reflect;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.Menus;
import com.gengoai.swing.component.Components;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.listener.SwingListeners;
import com.gengoai.swing.component.model.AutoCompleteDocument;
import com.gengoai.swing.component.model.MangoTableModel;
import lombok.SneakyThrows;
import lombok.Value;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.component.Components.button;

/**
 * @author David B. Bracewell
 */
public class CoNLLEditor extends HermesGUI {

   private static final int SMALL_ICON_SIZE = 16;
   private static final int LARGE_ICON_SIZE = 32;
   private final JFileChooser openDialog = new JFileChooser();
   private final JLabel documentPositionLabel = new JLabel();
   private final List<ColumnEntry> columnEntries = new ArrayList<>();
   private final JTextField tfSearch = new JTextField(10);
   private int currentDocument = 0;
   private MangoTable table;
   private final FluentAction faSearch = new FluentAction("Search", this::search)
         .smallIcon(FontAwesome.SEARCH.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.SEARCH.createIcon(LARGE_ICON_SIZE));
   private MangoTableModel tableModel;
   private List<Resource> documents;
   private final FluentAction faSave = new FluentAction("Save", this::save)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK))
         .smallIcon(FontAwesome.SAVE.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.SAVE.createIcon(LARGE_ICON_SIZE));
   private final FluentAction faPreviousDocument = new FluentAction("Previous Document", this::previousDocument)
         .smallIcon(FontAwesome.CHEVRON_LEFT.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.CHEVRON_LEFT.createIcon(LARGE_ICON_SIZE));
   private final FluentAction faNextDocument = new FluentAction("Next Document", this::nextDocument)
         .smallIcon(FontAwesome.CHEVRON_RIGHT.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.CHEVRON_RIGHT.createIcon(LARGE_ICON_SIZE));
   private final FluentAction faOpen = new FluentAction("Open", this::openFolder)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
         .smallIcon(FontAwesome.FOLDER_OPEN.createIcon(SMALL_ICON_SIZE))
         .largeIcon(FontAwesome.FOLDER_OPEN.createIcon(LARGE_ICON_SIZE));

   public static void main(String[] args) {
      runApplication(CoNLLEditor::new, "CoNLLAnnotationEditor", "Hermes CoNLL Annotation Editor", args);
   }


   public JPopupMenu createFreeFormTagMenu(int column, Collection<String> tags) {
      var menu = new JPopupMenu();
      menu.add(with(new JMenuItem("O"), $ -> $.addActionListener(a -> iobTag(column, "O"))));
      var vector = new Vector<>(tags);
      var list = new JList<>(vector);
      var textbox = new JTextField(25);
      AutoCompleteDocument autoCompleteDocument = AutoCompleteDocument.decorate(textbox);
      autoCompleteDocument.setDocumentFilter(new DocumentFilter() {
         @Override
         public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            fb.insertString(offset, string.toUpperCase(), attr);
         }

         @Override
         public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            fb.replace(offset, length, text.toUpperCase(), attrs);
         }
      });
      textbox.addKeyListener(SwingListeners.keyPressed(e -> {
         String text = textbox.getText().toUpperCase().trim().replaceAll("\\s+", "_");
         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!vector.contains(textbox.getText())) {
               vector.add(text);
               vector.sort(Comparator.comparing(Object::toString));
               autoCompleteDocument.addSearchItem(text);
            }
            iobTag(column, text);
            menu.setVisible(false);
            textbox.setText("");
         }
      }));
      textbox.getDocument().addDocumentListener(SwingListeners.documentListener((type, e) -> {
         String text = textbox.getText().toUpperCase().trim().replaceAll("\\s+", "_");
         int index = vector.indexOf(text);
         if (index >= 0) {
            list.setSelectedIndex(index);
         }
      }));
      textbox.getDocument().addDocumentListener(SwingListeners.documentListener((type, e) -> {
         String text = textbox.getText().toUpperCase().trim().replaceAll("\\s+", "_");
         int index = vector.indexOf(text);
         if (index >= 0) {
            list.setSelectedIndex(index);
         }
      }));
      list.addMouseListener(SwingListeners.mouseClicked(e -> {
         iobTag(column, vector.get(list.getSelectedIndex()).toString());
         menu.setVisible(false);
      }));
      menu.add(textbox);
      menu.add(with(Components.scrollPaneNoBorder(list), $ -> $.setSize(100, 600)), BorderLayout.CENTER);
      return menu;
   }

   @SneakyThrows
   private JPopupMenu createHierarchicalTagMenu(int column, Class<? extends HierarchicalEnumValue> clazz) {
      var menu = new JPopupMenu();
      HierarchicalEnumValue<?> root = Reflect.onClass(clazz).getField("ROOT").get();
      menu.add(with(new JMenuItem("O"), $ -> $.addActionListener(e -> iobTag(column, "O"))));
      List<HierarchicalEnumValue<?>> children = new ArrayList<>(root.children());
      children.sort(Comparator.comparing(HierarchicalEnumValue::label));
      for (HierarchicalEnumValue<?> child : children) {
         if (child.children().size() > 0) {
            menu.add(createSubMenu(table, column, child));
         } else {
            menu.add(with(new JMenuItem(child.label()), $ -> {
               $.addActionListener(e -> iobTag(column, child.name()));
            }));
         }
      }
      return menu;
   }

   @SneakyThrows
   private JPopupMenu createHierarchicalTagMenu(int column, List<String> tags) {
      var menu = new JPopupMenu();
      Multimap<String, String> parentChild = new HashSetMultimap<>();
      Map<String, String> label2Name = new HashMap<>();
      Set<String> children = new TreeSet<>();
      for (String tag : tags) {
         String[] parts = tag.split("\\$");
         children.add(parts[0]);
         label2Name.put(parts[0], parts[0]);
         for (int i = 1; i < parts.length; i++) {
            parentChild.put(parts[i - 1], parts[i]);
            label2Name.put(parts[i], String.join("$", Arrays.copyOfRange(parts, 0, i + 1)));
         }
      }
      menu.add(with(new JMenuItem("O"), $ -> $.addActionListener(e -> iobTag(column, "O"))));
      for (String child : children) {
         if (parentChild.get(child).isEmpty()) {
            menu.add(with(new JMenuItem(child), $ -> {
               $.addActionListener(e -> iobTag(column, label2Name.getOrDefault(child, child.toUpperCase())));
            }));
         } else {
            menu.add(createSubMenu(column, child, parentChild, label2Name));
         }
      }
      return menu;
   }

   private JPopupMenu createSuperSenseMenu(int column, Enum<?>[] enums) {
      var menu = new JPopupMenu();
      var noun = new JMenu("NOUN");
      var verb = new JMenu("VERB");
      var preposition = new JMenu("PREPOSITION");

      menu.add(with(new JMenuItem("Clear"), $-> $.addActionListener(e -> {
         for (int selectedRow : table.getSelectedRows()) {
            table.setValueAt("O", selectedRow, column);
         }
      })));
      menu.add(noun);
      menu.add(verb);
      menu.add(preposition);
      for (Enum<?> anEnum : enums) {
         String name = anEnum.name();
         if (name.startsWith("NOUN")) {
            noun.add(menu.add(with(new JMenuItem(name), $ -> {
               $.addActionListener(e -> iobTag(column, name));
            })));
         } else if (name.startsWith("VERB")) {
            verb.add(menu.add(with(new JMenuItem(name), $ -> {
               $.addActionListener(e -> iobTag(column, name));
            })));
         } else {
            preposition.add(menu.add(with(new JMenuItem(name), $ -> {
               $.addActionListener(e -> iobTag(column, name));
            })));
         }
      }
      return menu;
   }

   private JMenu createSubMenu(int column, String type, Multimap<String, String> parentChild, Map<String, String> label2Name) {
      JMenu root = new JMenu(type);
      JMenuItem child = new JMenuItem(type);
      child.addActionListener(e -> iobTag(column, label2Name.get(type)));
      root.add(child);
      for (String ct : new TreeSet<>(parentChild.get(type))) {
         if (parentChild.get(ct).isEmpty()) {
            root.add(with(new JMenuItem(ct), $ -> {
               $.addActionListener(e -> iobTag(column, label2Name.get(ct)));
            }));
         } else {
            root.add(createSubMenu(column, ct, parentChild, label2Name));
         }
      }
      return root;
   }

   private JMenu createSubMenu(JTable table, int column, HierarchicalEnumValue<?> type) {
      JMenu root = new JMenu(type.label());
      JMenuItem child = new JMenuItem(type.label());
      child.addActionListener(e -> iobTag(column, type.name()));
      root.add(child);
      for (HierarchicalEnumValue<?> ct : type.children()) {
         if (ct.children().size() > 0) {
            root.add(createSubMenu(table, column, ct));
         } else {
            child = new JMenuItem(ct.label());
            child.addActionListener(e -> iobTag(column, ct.name()));
            root.add(child);
         }
      }
      return root;
   }

   private JPopupMenu createUserDefinedTagMenu(int column, List<String> tags, boolean isIOB) {
      var menu = new JPopupMenu();
      var tagList = new ArrayList<>(tags);
      Collections.sort(tagList);
      if (isIOB) {
         tagList.add(0, "O");
      }
      for (String tag : tagList) {
         JMenuItem menuItem = new JMenuItem(tag);
         if (isIOB) {
            menuItem.addActionListener(e -> iobTag(column, tag));
         } else {
            menuItem.addActionListener(e -> tag(table, column, tag));
         }
         menu.add(menuItem);
      }
      return menu;
   }

   @Override
   protected void initControls() throws Exception {
      mainWindowFrame.setTitle("Hermes CoNLL Editor");
      menuBar(Menus.menu("File", 'F', Menus.menuItem(faOpen)));
      openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      openDialog.setCurrentDirectory(properties.get("open_dialog_directory").as(File.class,
                                                                                new File(SystemInfo.USER_HOME)));


      tfSearch.setMaximumSize(tfSearch.getPreferredSize());
      toolBar(button(faOpen, false, true, 0),
              button(faSave, false, true, 0),
              null,
              button(faPreviousDocument, false, true, 0),
              documentPositionLabel,
              button(faNextDocument, false, true, 0),
              null,
              tfSearch,
              button(faSearch, false, true, 0));


      faPreviousDocument.setEnabled(false);
      faNextDocument.setEnabled(false);
      faSave.setEnabled(false);

      documentPositionLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      documentPositionLabel.addMouseListener(SwingListeners.mouseDoubleClicked(e -> {
         String rval = JOptionPane.showInputDialog(null, "Document Number");
         try {
            int doc = Integer.parseInt(rval);
            if (doc < 0) {
               doc = -1;
            } else if (doc >= documents.size()) {
               doc = documents.size() - 2;
            } else {
               doc -= 2;
            }
            System.out.println(doc);
            currentDocument = doc;
            nextDocument(null);
         } catch (Exception ee) {

         }
      }));

      tableModel = new MangoTableModel();
      table = new MangoTable(tableModel);
      Fonts.adjustFont(table, Font.PLAIN, 20);
      table.setRowHeightPadding(5);
      table.setHeaderIsVisible(true);
      table.setReorderingAllowed(false);
      table.getTableHeader().addMouseListener(SwingListeners.mouseDoubleClicked(e -> table.resizeColumnWidth(100)));
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      table.setShowGrid(true);

      mainWindowFrame.add(Components.scrollPaneNoBorder(table), BorderLayout.CENTER);
      table.addMouseListener(SwingListeners.mouseReleased(this::onTableMouseRelease));
      table.addMouseListener(SwingListeners.mouseClicked(this::onTableMouseClick));
   }

   private void iobTag(int column, String tag) {
      boolean isFirst = true;
      int lastRow = -100;
      for (int selectedRow : table.getSelectedRows()) {
         faSave.setEnabled(true);
         if (isSeparator(selectedRow)) {
            continue;
         }
         if (lastRow + 1 != selectedRow) {
            isFirst = true;
         }

         if (tag.equals("O")) {
            table.setValueAt(tag, selectedRow, column);
         } else if (isFirst) {
            table.setValueAt("B-" + tag, selectedRow, column);
         } else {
            table.setValueAt("I-" + tag, selectedRow, column);
         }

         lastRow = selectedRow;
         isFirst = false;
      }
   }

   private boolean isSeparator(int row) {
      for (int i = 0; i < table.getColumnCount(); i++) {
         if (Strings.isNullOrBlank(table.getValueAtIndex(row, i).toString())) {
            return true;
         }
      }
      return false;
   }

   private void loadDocument(Resource resource) throws Exception {
      table.clear();
      faSave.setEnabled(false);
      List<String> lines = resource.readLines();
      for (String line : lines) {
         String[] parts = line.split("\\s+");
         List<String> row = new ArrayList<>();
         for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (i >= parts.length) {
               row.add(Strings.EMPTY);
            } else {
               row.add(parts[i]);
            }
         }
         table.addRow(row);
      }
   }

   private void nextDocument(ActionEvent e) {
      if (faSave.isEnabled()) {
         save(e);
      }
      currentDocument++;
      try {
         loadDocument(documents.get(currentDocument));
         faNextDocument.setEnabled(currentDocument + 1 < documents.size());
         faPreviousDocument.setEnabled(currentDocument - 1 >= 0);
         int points = Integer.toString(documents.size()).length();
         documentPositionLabel
               .setText(Strings.padStart(Integer.toString(currentDocument + 1), points, ' ') + " / " + documents
                     .size());
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   protected void onClose() throws Exception {
      if (faSave.isEnabled()) {
         save(null);
      }
      super.onClose();
      if (openDialog.getSelectedFile() != null) {
         properties.set("open_dialog_directory", openDialog.getSelectedFile().getParentFile().getAbsolutePath());
      }
   }

   private void onTableMouseClick(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON3) {
         int index = table.columnAtPoint(new Point(e.getX(), e.getY()));
         if (index >= 0 && index < columnEntries.size()) {
            ColumnEntry entry = columnEntries.get(index);
            if (entry.tagMenu != null) {
               entry.tagMenu.show(table, e.getX(), e.getY());
            }
         }
      }
   }

   private void onTableMouseRelease(MouseEvent e) {
      if (e.isAltDown()) {
         int index = table.columnAtPoint(new Point(e.getX(), e.getY()));
         if (index >= 0 && index < columnEntries.size()) {
            ColumnEntry entry = columnEntries.get(index);
            if (entry.tagMenu != null) {
               entry.tagMenu.show(table, e.getX(), e.getY());
            }
         }
      }
   }

   private void openFolder(ActionEvent e) {
      if (openDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
         faPreviousDocument.setEnabled(false);
         faNextDocument.setEnabled(false);
         faSave.setEnabled(false);
         File dir = openDialog.getSelectedFile();
         currentDocument = 0;
         Resource rootDir = Resources.fromFile(dir);
         documents = rootDir.getChild("documents").getChildren();
         Collections.sort(documents, Comparator.comparing(Resource::path));
         int points = Integer.toString(documents.size()).length();
         documentPositionLabel.setText(Strings.padStart("1", points, ' ') + " / " + documents.size());

         try {
            tableModel.setRowCount(0);

            List<JsonEntry> columns = Json.parseArray(rootDir.getChild("annotation.json"));

            tableModel = new MangoTableModel();
            columnEntries.clear();
            for (JsonEntry column : columns) {
               String name = column.getStringProperty("name");
               tableModel.addColumn(name);

               int tableColumn = tableModel.getColumnCount() - 1;

               JPopupMenu menu = null;
               if (column.hasProperty("tagType")) {
                  final String tagType = column.getStringProperty("tagType").toUpperCase();
                  switch (tagType) {
                     case "SUPERSENSE":
                        menu = createSuperSenseMenu(tableColumn, SuperSense.values());
                        break;
                     case "IOB-FREEFORM":
                        List<String> tags = column.hasProperty("tags")
                              ? column.getProperty("tags").asArray(String.class)
                              : Collections.emptyList();
                        menu = createFreeFormTagMenu(tableColumn, tags);
                        break;
                     case "IOB-FIXED":
                        menu = createUserDefinedTagMenu(tableColumn, column.getProperty("tags")
                                                                           .asArray(String.class), true);
                        break;
                     case "IOB-FIXED-HIERARCHY":
                        menu = createHierarchicalTagMenu(tableColumn, column.getProperty("tags").asArray(String.class));
                        break;
                     case "FIXED":
                        menu = createUserDefinedTagMenu(tableColumn, column.getProperty("tags")
                                                                           .asArray(String.class), false);
                        break;
                     case "IOB-HIERARCHICAL_ENUM":
                        Class<?> o = Class.forName(column.getStringProperty("tags"));
                        if (HierarchicalEnumValue.class.isAssignableFrom(o)) {
                           menu = createHierarchicalTagMenu(tableColumn, Cast.<Class<? extends HierarchicalEnumValue>>as(o));
                        }
                        break;
                     default:
                        System.err.println("UNKNOWN '" + tagType + "'");
                  }
               }

               columnEntries.add(new ColumnEntry(name, menu));

            }
            table.setModel(tableModel);
            table.repaint();

            loadDocument(documents.get(0));
            faPreviousDocument.setEnabled(false);
            faNextDocument.setEnabled(currentDocument + 1 <= documents.size());
         } catch (Exception ex) {
            throw new RuntimeException(ex);
         }


         table.resizeColumnWidth(100);

      }
   }

   private void previousDocument(ActionEvent e) {
      if (faSave.isEnabled()) {
         save(e);
      }
      currentDocument--;
      try {
         loadDocument(documents.get(currentDocument));
         faPreviousDocument.setEnabled(currentDocument - 1 >= 0);
         faNextDocument.setEnabled(currentDocument + 1 < documents.size());
         int points = Integer.toString(documents.size()).length();
         documentPositionLabel
               .setText(Strings.padStart(Integer.toString(currentDocument + 1), points, ' ') + " / " + documents
                     .size());
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   private void save(ActionEvent event) {
      faSave.setEnabled(false);
      Resource r = documents.get(currentDocument);
      try (BufferedWriter writer = new BufferedWriter(r.writer())) {
         for (int i = 0; i < tableModel.getRowCount(); i++) {
            var joiner = new StringJoiner("\t");
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
               joiner.add(tableModel.getValueAt(i, j).toString());
            }
            writer.write(joiner.toString() + "\n");
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void search(ActionEvent e) {
      final String search = tfSearch.getText();
      table.getSelectionModel().clearSelection();
      for (int i = 0; i < table.getRowCount(); i++) {
         for (int j = 0; j < table.getColumnCount(); j++) {
            String v = table.getValueAtIndex(i, j).toString();
            if (v.contains(search)) {
               table.getSelectionModel().addSelectionInterval(i, i);
            }
         }
      }
   }

   private void tag(JTable table, int column, String tag) {
      for (int selectedRow : table.getSelectedRows()) {
         faSave.setEnabled(true);
         table.setValueAt(tag, selectedRow, column);
      }
   }

   @Value
   private static class ColumnEntry {
      String name;
      JPopupMenu tagMenu;
   }


}//END OF CoNLLEditor

