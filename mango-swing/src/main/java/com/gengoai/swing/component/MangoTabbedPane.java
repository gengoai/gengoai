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
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.View;
import com.gengoai.swing.component.listener.TabClosedListener;
import lombok.NonNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import static com.gengoai.function.Functional.with;

public class MangoTabbedPane extends JTabbedPane {
   @NonNull
   private Vector<TabClosedListener> tabClosedListeners = new Vector<>();
   private boolean closeButtonsVisible = true;

   public MangoTabbedPane() {
      this(JTabbedPane.TOP, WRAP_TAB_LAYOUT);
   }

   public MangoTabbedPane(int tabPlacement) {
      this(tabPlacement, WRAP_TAB_LAYOUT);
   }

   public MangoTabbedPane(int tabPlacement, int tabLayoutPolicy) {
      super(tabPlacement, tabLayoutPolicy);
      this.tabClosedListeners.add(this::remove);
   }

   public void addTabClosedListener(@NonNull TabClosedListener listener) {
      tabClosedListeners.add(listener);
   }

   private void fireTabClosed(Component component) {
      tabClosedListeners.forEach(l -> l.onClose(component));
   }

   @Override
   public void insertTab(String title, Icon icon, Component component, String tip, int index) {
      if(component instanceof View) {
         component = ((View) component).getRoot();
      }
      super.insertTab(title, icon, component, tip, index);
      setTabComponentAt(index, new CloseButtonTab(component, title, icon));
      setToolTipTextAt(getTabCount() - 1, title);
   }

   public void setCloseButtonVisible(int index, boolean visible) {
      Cast.<CloseButtonTab>as(getTabComponentAt(index)).button.setVisible(visible);
   }

   public void setCloseButtonsVisible(boolean visible) {
      closeButtonsVisible = visible;
      for(int i = 0; i < getTabCount(); i++) {
         Cast.<CloseButtonTab>as(getTabComponentAt(i)).button.setVisible(closeButtonsVisible);
      }
   }

   @Override
   public void setComponentAt(int index, Component component) {
      if(component instanceof View) {
         component = ((View) component).getRoot();
      }
      super.setComponentAt(index, component);
      Cast.<CloseButtonTab>as(getTabComponentAt(index)).tab = component;
   }

   @Override
   public void setIconAt(int index, Icon icon) {
      super.setIconAt(index, icon);
      Cast.<CloseButtonTab>as(getTabComponentAt(index)).label.setIcon(icon);
   }

   @Override
   public void setTitleAt(int index, String title) {
      Cast.<CloseButtonTab>as(getTabComponentAt(index)).label.setText(title);
   }

   protected class CloseButtonTab extends JPanel {
      private final JLabel label;
      private final JButton button;
      private Component tab;

      public CloseButtonTab(final Component tab, String title, Icon icon) {
         this.tab = tab;
         setOpaque(false);
         setLayout(new BorderLayout());
         title = Strings.abbreviate(title, 18);
         label = new JLabel(title, icon, JLabel.CENTER);
         final Icon cBtnIcon = FontAwesome.TIMES.createIcon(getFontMetrics(getFont()).getHeight() - 2);
         final Icon cHover = FontAwesome.TIMES.createIcon(getFontMetrics(getFont()).getHeight() - 2,
                                                          Color.RED.darker());
         button = with(new JButton(cBtnIcon),
                       b -> {
                          b.setOpaque(false);
                          b.setRolloverIcon(cHover);
                          b.setContentAreaFilled(false);
                          b.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                          b.addActionListener(a -> fireTabClosed(tab));
                          b.setMaximumSize(new Dimension(cBtnIcon.getIconWidth() + 2, cBtnIcon.getIconWidth() + 2));
                          b.setVisible(closeButtonsVisible);
                       });

         add(label, BorderLayout.CENTER);
         add(button, BorderLayout.EAST);
      }
   }
}//END OF MangoTabbedPane
