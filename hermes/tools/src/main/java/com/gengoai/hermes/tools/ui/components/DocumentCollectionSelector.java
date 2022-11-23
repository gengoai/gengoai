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

import com.gengoai.hermes.format.DocFormatService;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.component.listener.FluentAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.swing.component.Components.button;

public class DocumentCollectionSelector extends JPanel {


   public boolean showSelector() {
      if (JOptionPane.showConfirmDialog(null, this,
                                        "Document Collection Selector",
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
         return Strings.isNotNullOrBlank(this.path.getText());
      }
      return false;
   }

   public String getFormat() {
      StringBuilder format = new StringBuilder(this.formats.getSelectedItem().toString());
      if (this.isOnePerLine.isSelected()) {
         format.append("_opl");
      }
      format.append("::").append(this.path.getText());
      return format.toString();
   }

   private final JComboBox<String> formats = new JComboBox<>();
   private final JTextField path = new JTextField();
   private final JButton btnSelectPath;
   private final JFileChooser openDialog = new JFileChooser();
   private final JCheckBox isOnePerLine = new JCheckBox("One Per Line", false);

   private void openFolder(ActionEvent e) {
      if (openDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
         path.setText(openDialog.getSelectedFile().getAbsolutePath());
      }
   }

   public DocumentCollectionSelector(File startingPath) {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      //The Formats
      List<String> items = DocFormatService.getProviders().stream()
                                           .map(p -> p.getName().toLowerCase())
                                           .collect(Collectors.toList());
      items.add("corpus");
      Collections.sort(items);
      items.forEach(formats::addItem);
      add(formats);


      JPanel pathBar = new JPanel();
      //The path
      path.setColumns(25);
      path.setEditable(false);
      pathBar.add(path);
      FluentAction faOpen = new FluentAction("Open", this::openFolder)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.FOLDER_OPEN.createIcon(16))
            .largeIcon(FontAwesome.FOLDER_OPEN.createIcon(32));
      btnSelectPath = button(faOpen, false, true, 0);
      pathBar.add(btnSelectPath);
      add(pathBar);

      openDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      openDialog.setCurrentDirectory(startingPath);

      add(isOnePerLine);
   }

}
