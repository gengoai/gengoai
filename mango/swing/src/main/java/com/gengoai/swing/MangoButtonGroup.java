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

package com.gengoai.swing;

import com.gengoai.swing.component.HBox;
import lombok.NonNull;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import java.util.Iterator;
import java.util.function.Consumer;

public class MangoButtonGroup extends ButtonGroup implements Iterable<AbstractButton> {
   private static final long serialVersionUID = 1L;

   public HBox createHorizontalView(int gap) {
      HBox hBox = new HBox(gap, 0);
      buttons.forEach(hBox::add);
      return hBox;
   }

   public HBox createHorizontalView(int gap, @NonNull Consumer<HBox> styler) {
      HBox hBox = new HBox(gap, 0);
      buttons.forEach(hBox::add);
      styler.accept(hBox);
      return hBox;
   }

   public AbstractButton getButton(int index) {
      return buttons.get(index);
   }

   public AbstractButton getFirstButton() {
      return buttons.get(0);
   }

   public AbstractButton getLastButton() {
      return buttons.get(buttons.size() - 1);
   }

   public int getSelectedIndex() {
      for(int i = 0; i < buttons.size(); i++) {
         if(buttons.get(i).isSelected()) {
            return i;
         }
      }
      return -1;
   }

   @Override
   public Iterator<AbstractButton> iterator() {
      return buttons.iterator();
   }

   public void selectFirst() {
      setSelected(0, true);
   }

   public void selectLast() {
      setSelected(buttons.size() - 1, true);
   }

   public void setSelected(int buttonIndex, boolean isSelected) {
      setSelected(buttons.get(buttonIndex).getModel(), isSelected);
   }

}//END OF MangoButtonGroup
