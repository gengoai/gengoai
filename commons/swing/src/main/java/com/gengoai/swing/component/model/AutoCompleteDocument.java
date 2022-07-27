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

package com.gengoai.swing.component.model;

import com.gengoai.collection.tree.Trie;
import com.gengoai.string.Strings;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.util.Iterator;
import java.util.List;

/**
 * Autocomplete decorator for JTextFields. Items it can complete are defined using the {@link #setSearchItems(List)}
 * method.
 */
public class AutoCompleteDocument extends PlainDocument {
   private static final long serialVersionUID = 1L;
   private final Trie<String> items = new Trie<>();
   private final JTextField owner;

   private AutoCompleteDocument(@NonNull JTextField owner) {
      this.owner = owner;
   }

   /**
    * Decorates the given text field by changing its document to an Autocomplete document.
    *
    * @param textField the text field to decorate
    * @return the AutoCompleteDocument used to decorate the text field
    */
   public static AutoCompleteDocument decorate(@NonNull JTextField textField) {
      var acd = new AutoCompleteDocument(textField);
      textField.setDocument(acd);
      return acd;
   }

   public void addSearchItem(String value) {
      this.items.put(value.toLowerCase(), value);
   }

   private String complete(String str) {
      Iterator<String> itr = items.prefixKeyIterator(str.toLowerCase());
      return itr.hasNext()
             ? items.get(itr.next())
             : null;
   }

   @Override
   public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      String text = getText(0, offs);
      String completion = complete(text + str);
      if(Strings.isNotNullOrBlank(completion)) {
         int length = offs + str.length();
         str = completion.substring(length - 1);
         super.insertString(offs, str, a);
         owner.select(length, getLength());
      } else {
         super.insertString(offs, str, a);
      }
   }

   public void removeSearchItem(String value) {
      this.items.remove(value.toLowerCase());
   }

   /**
    * Sets search items.
    *
    * @param items the items that can be autocompleted
    */
   public void setSearchItems(List<String> items) {
      this.items.clear();
      items.forEach(s -> this.items.put(s.toLowerCase(), s));
   }

}//END OF AutoCompleteDocument
