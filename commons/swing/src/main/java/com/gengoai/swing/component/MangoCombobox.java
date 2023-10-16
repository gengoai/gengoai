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

package com.gengoai.swing.component;

import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.swing.component.listener.SwingListeners;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.BadLocationException;
import java.util.Objects;
import java.util.function.Function;

public class MangoCombobox<E> extends JComboBox<E> {
   private static final long serialVersionUID = 1L;
   private final AutoCompleteEditor autoCompleteEditor = new AutoCompleteEditor();
   private final JTextField textField;
   private final Function<E, String> toStringFunction;
   private int caretPos = 0;

   public MangoCombobox() {
      this(Objects::toString);
   }

   public MangoCombobox(@NonNull Function<E, String> toStringFunction) {
      this.setEditor(autoCompleteEditor);
      this.setEditable(true);
      this.textField = autoCompleteEditor.getEditorComponent();
      this.toStringFunction = toStringFunction;
   }

   public void setSelectedIndex(int index) {
      super.setSelectedIndex(index);
      textField.setText(getItemAt(index).toString());
   }

   private class AutoCompleteEditor extends BasicComboBoxEditor {

      public AutoCompleteEditor() {
         editor.addKeyListener(SwingListeners.keyPressed(e -> {
            char key = e.getKeyChar();
            if(!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key))) return;
            caretPos = editor.getCaretPosition();
            String text = Strings.EMPTY;
            try {
               text = editor.getText(0, caretPos);
            } catch(BadLocationException ble) {
               //pass
            }
            text += key;
            text = text.toLowerCase();
            for(int i = 0; i < getItemCount(); i++) {
               String item = toStringFunction.apply(getItemAt(i));
               if(item.toLowerCase().startsWith(text)) {
                  setSelectedIndex(i);
                  return;
               }
            }
         }));
      }

      @Override
      public JTextField getEditorComponent() {
         return Cast.as(super.getEditorComponent());
      }

   }

}//END OF MangoCombobox
