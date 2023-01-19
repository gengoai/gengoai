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

import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Menus;
import com.gengoai.swing.SwingApplication;
import com.gengoai.swing.component.VBox;
import com.gengoai.swing.component.listener.FluentAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.gengoai.swing.component.Components.button;

public class Hermes extends HermesGUI {

   private final FluentAction newCorpusAction = new FluentAction("New Corpus...", this::newCorpus)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK))
         .smallIcon(FontAwesome.MAGIC.createIcon(16))
         .largeIcon(FontAwesome.MAGIC.createIcon(32));

   private final FluentAction openCorpusAction = new FluentAction("Open Corpus...", this::openCorpus)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK))
         .smallIcon(FontAwesome.FOLDER_OPEN.createIcon(16))
         .largeIcon(FontAwesome.FOLDER_OPEN.createIcon(32));

   private final FluentAction closeCorpusAction = new FluentAction("Close Corpus", this::openCorpus)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK))
         .smallIcon(FontAwesome.FOLDER.createIcon(16))
         .largeIcon(FontAwesome.FOLDER.createIcon(32));

   private void newCorpus(ActionEvent e) {

   }

   private void openCorpus(ActionEvent e) {

   }

   @Override
   protected void initControls() throws Exception {
      menuBar(Menus.menu("File",
                         'F',
                         Menus.menuItem(newCorpusAction),
                         Menus.menuItem(openCorpusAction)));

      toolBar(button(newCorpusAction, false, true, 0),
              button(openCorpusAction, false, true, 0),
              button(closeCorpusAction, false, true, 0));

      closeCorpusAction.setEnabled(false);

   }

   public static void main(String[] args) {
      SwingApplication.runApplication(Hermes::new, "Hermes", "Hermes", args);
   }


   private static class MainPanel extends JPanel {

      public MainPanel() {
         var vbox = new VBox(15, 20);
         vbox.add(new JButton("Create Corpus"));
         vbox.add(new JButton("Open Corpus"));
         add(vbox);
      }

   }

}
