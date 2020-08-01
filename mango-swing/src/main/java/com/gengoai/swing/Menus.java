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

import com.gengoai.Validation;
import com.gengoai.conversion.Cast;
import com.gengoai.swing.component.listener.FluentAction;
import lombok.NonNull;

import javax.swing.*;

/**
 * Convenience methods for creating menus and menu items.
 */
public final class Menus {
   public static final JSeparator SEPARATOR = new JPopupMenu.Separator();

   private Menus() {
      throw new IllegalAccessError();
   }

   private static void add(JComponent menu, Object... menuItems) {
      for(Object menuItem : menuItems) {
         Validation.notNull(menuItem);
         if(menuItem instanceof JPopupMenu.Separator) {
            if(menu instanceof JMenu) {
               Cast.<JMenu>as(menu).addSeparator();
            } else {
               Cast.<JPopupMenu>as(menu).addSeparator();
            }
         } else if(menuItem instanceof JMenuItem) {
            menu.add(Cast.<JMenuItem>as(menuItem));
         } else if(menuItem instanceof FluentAction) {
            menu.add(menuItem(Cast.as(menuItem)));
         } else {
            throw new IllegalArgumentException("Cannot add object of type " + menuItem.getClass());
         }
      }
   }

   /**
    * Constructs a JCheckBoxMenu item with the given action and initial state set to checked.
    *
    * @param fluentAction the named action
    * @param checked      the initial state
    * @return the JCheckBoxMenuItem
    */
   public static JCheckBoxMenuItem checkedMenuItem(@NonNull FluentAction fluentAction, boolean checked) {
      JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
      menuItem.setAction(fluentAction);
      menuItem.setState(checked);
      return menuItem;
   }

   /**
    * Constructs a JMenu with the given title, mnemonic, and menu items. Pass a null value to add a separator.
    *
    * @param title     the title
    * @param mnemonic  the mnemonic
    * @param menuItems the menu items
    * @return the JMenu
    */
   public static JMenu menu(@NonNull String title, int mnemonic, @NonNull Object... menuItems) {
      JMenu menu = new JMenu(title);
      menu.setMnemonic(mnemonic);
      add(menu, menuItems);
      return menu;
   }

   /**
    * Constructs a JMenu with the given title, mnemonic, and menu item.
    *
    * @param title    the title
    * @param mnemonic the mnemonic
    * @param menuItem the menu item
    * @return the JMenu
    */
   public static JMenu menu(@NonNull String title, int mnemonic, @NonNull JMenuItem menuItem) {
      return menu(title, mnemonic, new Object[]{menuItem});
   }

   /**
    * Constructs a new JMenuItem from the given NamedAction.
    *
    * @param fluentAction the named action
    * @return the JMenuItem
    */
   public static JMenuItem menuItem(@NonNull FluentAction fluentAction) {
      JMenuItem menuItem = new JMenuItem();
      menuItem.setAction(fluentAction);
      return menuItem;
   }

   public static JPopupMenu popupMenu(@NonNull Object... menuItems) {
      JPopupMenu menu = new JPopupMenu();
      add(menu, menuItems);
      return menu;
   }

}//END OF Menus
