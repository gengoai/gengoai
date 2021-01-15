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

package com.gengoai.swing.component.view;

import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.View;
import com.gengoai.swing.component.MangoPanel;
import com.gengoai.swing.component.MangoTreeView;
import com.gengoai.swing.component.listener.SwingListeners;
import com.gengoai.swing.component.model.AutoCompleteDocument;
import lombok.NonNull;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiPredicate;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.component.Components.scrollPaneNoBorder;
import static com.gengoai.swing.component.listener.SwingListeners.removeAllKeyListeners;

public class MangoFilteredTreeView extends MangoTreeView implements View {
   private final JPanel panel;
   private final MangoButtonedTextField filterField = new MangoButtonedTextField(15,
                                                                                 FontAwesome.BACKSPACE,
                                                                                 MangoButtonedTextField.RIGHT);
   private final AutoCompleteDocument autoCompleteDocument;

   public MangoFilteredTreeView(@NonNull BiPredicate<String, Object> itemMatcher) {
      super(itemMatcher);
      setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
      autoCompleteDocument = AutoCompleteDocument.decorate(filterField);
      with(filterField, $ -> {
         removeAllKeyListeners($);
         $.getDocument().addDocumentListener(SwingListeners.documentListener((type, e) -> {
            setFilter($.getText());
            $.getRightButton().setVisible(Strings.isNotNullOrBlank($.getText()));
         }));
         with($.getRightButton(), $btn -> {
            $btn.setVisible(false);
            $btn.addActionListener(a -> $.setText(""));
            $btn.setFocusable(false);
         });
         $.addActionListener(e -> selectTag());
         $.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                             SwingListeners.fluentAction("DOWN", e -> {
                                setSelectionRow(0);
                                requestFocus();
                             }));
      });
      panel = with(new JPanel(), $ -> {
         $.setOpaque(false);
         $.setBorder(BorderFactory.createEmptyBorder());
         $.setLayout(new BorderLayout());
         $.add(with(new MangoPanel(), $pnl -> {
            $pnl.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            $pnl.add(filterField, BorderLayout.CENTER);
         }), BorderLayout.NORTH);
         $.add(scrollPaneNoBorder(this), BorderLayout.CENTER);
      });
   }

   public void focusOnFilter() {
      filterField.requestFocus();
   }

   public String getFilterText() {
      return filterField.getText();
   }

   @Override
   public JComponent getRoot() {
      return panel;
   }

   private void selectTag() {
      DefaultMutableTreeNode root = Cast.as(baseModel.getRoot());
      Enumeration<DefaultMutableTreeNode> enumeration = Cast.as(root.breadthFirstEnumeration());
      while(enumeration.hasMoreElements()) {
         DefaultMutableTreeNode n = enumeration.nextElement();
         if(itemMatcher.test(filterField.getText(), n.getUserObject())) {
            getSelectionModel().setSelectionPath(new TreePath(n.getPath()));
            filterField.setSelectionStart(filterField.getSelectionEnd());
            requestFocus();
            fireItemSelection(n.getUserObject());
            return;
         }
      }
   }

   public void setAutocomplete(List<String> items) {
      autoCompleteDocument.setSearchItems(items);
   }

   public void setFilterText(String text) {
      filterField.setText(text);
   }
}//END OF MangoFilteredTreeView
