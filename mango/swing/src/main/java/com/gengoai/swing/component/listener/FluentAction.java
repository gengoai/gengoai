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

package com.gengoai.swing.component.listener;

import com.gengoai.conversion.Cast;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <p>A fluent interface for constructing actions.</p>
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FluentAction extends AbstractAction {
   private final ActionListener listener;

   /**
    * Instantiates a new unnamed FluentAction.
    *
    * @param listener the action listener
    */
   public FluentAction(@NonNull ActionListener listener) {
      this.listener = listener;
   }

   /**
    * Instantiates a  new named FluentAction.
    *
    * @param name     the name of the action
    * @param listener the action listener
    */
   public FluentAction(@NonNull String name, @NonNull ActionListener listener) {
      putValue(NAME, name);
      this.listener = listener;
   }

   /**
    * Sets the accelerator key for the action
    *
    * @param keyStroke the key stroke representing the accelerator
    * @return this FluentAction
    */
   public FluentAction accelerator(KeyStroke keyStroke) {
      putValue(ACCELERATOR_KEY, keyStroke);
      return this;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      listener.actionPerformed(e);
   }

   /**
    * @return the accelerator associated with the action if any
    */
   public KeyStroke getAccelerator() {
      return Cast.as(getValue(ACCELERATOR_KEY));
   }

   /**
    * @return the large icon associated with the action if any
    */
   public Icon getLargeIcon() {
      return Cast.as(getValue(Action.LARGE_ICON_KEY));
   }

   /**
    * @return the long description associated with the action if any
    */
   public String getLongDescription() {
      return Cast.as(getValue(Action.LONG_DESCRIPTION));
   }

   /**
    * @return the mnemonic associated with the action if any
    */
   public Integer getMnemonic() {
      return Cast.as(getValue(MNEMONIC_KEY));
   }

   /**
    * @return the name of the action if defined
    */
   public String getName() {
      return getValue(NAME).toString();
   }

   /**
    * @return the short description of the action if any
    */
   public String getShortDescription() {
      return Cast.as(getValue(SHORT_DESCRIPTION));
   }

   /**
    * @return the small icon associated with the action if any
    */
   public Icon getSmallIcon() {
      return Cast.as(getValue(SMALL_ICON));
   }

   /**
    * @return True if the action is in a selected state, false otherwise
    */
   public boolean isSelected() {
      Object o = getValue(SELECTED_KEY);
      return o != null && (Boolean) o;
   }

   /**
    * Sets the large icon associated with the action
    *
    * @param icon the icon
    * @return this FluentAction
    */
   public FluentAction largeIcon(Icon icon) {
      putValue(LARGE_ICON_KEY, icon);
      return this;
   }

   /**
    * Sets the Long description associated with the action
    *
    * @param value the value
    * @return this FluentAction
    */
   public FluentAction longDescription(String value) {
      putValue(LONG_DESCRIPTION, value);
      return this;
   }

   /**
    * Sets the Mnemonic associated with the action
    *
    * @param key the key
    * @return this FluentAction
    */
   public FluentAction mnemonic(int key) {
      putValue(MNEMONIC_KEY, key);
      return this;
   }

   /**
    * Sets the selected state of the action
    *
    * @param selected the selected state
    * @return this FluentAction
    */
   public FluentAction selected(boolean selected) {
      putValue(SELECTED_KEY, selected);
      return this;
   }

   /**
    * Sets the Short description associated with the action
    *
    * @param value the value
    * @return this FluentAction
    */
   public FluentAction shortDescription(String value) {
      putValue(SHORT_DESCRIPTION, value);
      return this;
   }

   /**
    * Sets the Small icon associated with the action
    *
    * @param icon the icon
    * @return this FluentAction
    */
   public FluentAction smallIcon(Icon icon) {
      putValue(SMALL_ICON, icon);
      return this;
   }

}//END OF NamedAction
