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

import com.gengoai.swing.FlatLafColors;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.View;
import com.gengoai.swing.component.listener.FocusEventType;
import com.gengoai.swing.component.listener.FocusListeners;
import com.gengoai.swing.component.listener.SwingListeners;
import lombok.NonNull;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.util.Vector;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.Fonts.setFontStyle;
import static javax.swing.BorderFactory.*;

/**
 * A panel implementation that allows for a title at the top with an optional close button.
 */
public class MangoTitlePane extends MangoPanel {
   private final Color bgColor = FlatLafColors.inactiveCaption.color();
   private final Color bgFocus = FlatLafColors.activeCaption.color();
   private final Color fgColor = FlatLafColors.inactiveCaptionText.color();
   private final Color fgFocus = FlatLafColors.activeCaptionText.color();
   private final Border bdrMargin = createEmptyBorder(8, 8, 8, 8);
   private final Border bdrHeader = createCompoundBorder(createMatteBorder(0, 0, 1, 0, bgColor.darker()), bdrMargin);
   private final Border bdrHeaderFocus = createCompoundBorder(createMatteBorder(0, 0, 1, 0, bgFocus.darker()),
                                                              bdrMargin);
   private final Border bdrComponentInActive;
   private final Border bdrComponentActive;
   private final JLabel lblHeading;
   private final JButton btnClose;
   private final JPanel header;
   private final JComponent component;
   private final Vector<ActionListener> onCloseHandlers = new Vector<>();

   /**
    * Instantiates a new TitlePane
    *
    * @param title              the title
    * @param includeCloseButton True - include the close button, False do not include
    * @param component          the component that this panel wraps
    */
   public MangoTitlePane(@NonNull String title, boolean includeCloseButton, @NonNull JComponent component) {
      setFocusable(true);
      setLayout(new BorderLayout());
      if(component instanceof View) {
         component = ((View) component).getRoot();
      }
      this.bdrComponentInActive = createCompoundBorder(createLineBorder(bgColor, 2), component.getBorder());
      this.bdrComponentActive = createCompoundBorder(createLineBorder(bgFocus, 2), component.getBorder());
      this.component = component;
      this.component.setBorder(bdrComponentInActive);

      this.lblHeading = with(new JLabel(), $ -> {
         $.setText(title);
         $.setHorizontalAlignment(JLabel.LEADING);
         $.setVerticalAlignment(JLabel.CENTER);
         $.setOpaque(false);
         $.setForeground(fgColor);
         setFontStyle($, Font.BOLD);
      });

      this.btnClose = with(new JButton(FontAwesome.WINDOW_CLOSE.createIcon(20)), $ -> {
         $.setRolloverIcon(FontAwesome.WINDOW_CLOSE.createIcon(20, Color.RED));
         $.setOpaque(false);
         $.setBorder(null);
         $.setContentAreaFilled(false);
         $.addActionListener(this::fireOnClose);
      });

      this.header = with(new JPanel(), $ -> {
         $.setLayout(new BorderLayout());
         $.add(lblHeading, BorderLayout.CENTER);
         if(includeCloseButton) {
            $.add(btnClose, BorderLayout.EAST);
         }
         $.setBackground(bgColor);
         $.setBorder(bdrHeader);
      });

      add(header, BorderLayout.NORTH);
      add(component, BorderLayout.CENTER);

      FocusListeners.recursiveFocusListener(component, SwingListeners.focusListener(this::focusListener));
   }

   /**
    * Adds a listener to the on the close even when the close button is clocked.
    *
    * @param actionListener the action listener
    * @return this TitlePane
    */
   public MangoTitlePane addCloseListener(@NonNull ActionListener actionListener) {
      this.onCloseHandlers.add(actionListener);
      return this;
   }

   private void fireOnClose(ActionEvent event) {
      setVisible(false);
      onCloseHandlers.forEach(a -> a.actionPerformed(event));
   }

   private void focusListener(FocusEventType type, FocusEvent event) {
      if(type == FocusEventType.FOCUS_GAINED) {
         lblHeading.setForeground(fgFocus);
         header.setBackground(bgFocus);
         header.setBorder(bdrHeaderFocus);
         component.setBorder(bdrComponentActive);
      } else {
         lblHeading.setForeground(fgColor);
         header.setBackground(bgColor);
         header.setBorder(bdrHeader);
         component.setBorder(bdrComponentInActive);
      }
      invalidate();
   }

   /**
    * Sets the title heading
    *
    * @param text the text
    * @return this TitlePane
    */
   public MangoTitlePane heading(String text) {
      this.lblHeading.setText(text);
      return this;
   }

   public void setHeadingFont(Font font) {
      lblHeading.setFont(font);
   }

}//END OF MangoTitlePane
