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

import com.gengoai.swing.*;
import com.gengoai.swing.component.listener.FocusEventType;
import com.gengoai.swing.component.listener.FocusListeners;
import com.gengoai.swing.component.listener.SwingListeners;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;

import static com.gengoai.swing.ComponentStyle.defineStyle;
import static javax.swing.BorderFactory.*;

/**
 * A JTextField with optional JButtons on the left and right side.
 */
public class MangoButtonedTextField extends JTextField implements View {
   private static final long serialVersionUID = 1L;
   static final ComponentStyle<JButton> buttonStyle = defineStyle($ -> {
      $.setBorder(createEmptyBorder(1, 1, 1, 1));
      $.setOpaque(false);
      $.setContentAreaFilled(false);
      $.setBackground(null);
   });
   /**
    * Constant setting of the LEFT side of the textfield
    */
   public static final String LEFT = BorderLayout.WEST;
   /**
    * Constant setting of the RIGHT side of the textfield
    */
   public static final String RIGHT = BorderLayout.EAST;
   private final JButton btnLeft;
   private final JButton btnRight;
   private final JPanel panel;
   private final Border inactiveBorder = createCompoundBorder(
         createLineBorder(FlatLafColors.Button_borderColor.color(), 1, true),
         createEmptyBorder(2, 2, 2, 2));
   private final Border activeBorder = createCompoundBorder(
         createLineBorder(FlatLafColors.Button_focusedBorderColor.color(), 2, true),
         createEmptyBorder(1, 1, 1, 1));

   /**
    * Instantiates a new MangoButtonedTextField.
    *
    * @param columns  the number of columns in the text field
    * @param btnIcon  the icon to use for the button.
    * @param position the position of the button
    */
   public MangoButtonedTextField(int columns, FontAwesome btnIcon, String position) {
      this(columns,
           (position.equalsIgnoreCase(LEFT)
            ? btnIcon
            : null),
           (position.equalsIgnoreCase(RIGHT)
            ? btnIcon
            : null));
   }

   /**
    * Instantiates a new MangoButtonedTextField.
    *
    * @param columns the number of columns in the text field
    * @param left    the icon to use for the left button.
    * @param right   the icon to use for the right button.
    */
   public MangoButtonedTextField(int columns, FontAwesome left, FontAwesome right) {
      super(columns);
      setOpaque(false);
      setBackground(null);

      int btnSize = Fonts.getFontHeight(this) - 4;
      this.btnLeft = buttonStyle.style(left == null
                                       ? null
                                       : left.createButton(btnSize));
      this.btnRight = buttonStyle.style(right == null
                                        ? null
                                        : right.createButton(btnSize));

      int leftBorder = 0;
      int rightBorder = 0;
      panel = new JPanel(new BorderLayout());
      panel.add(this, BorderLayout.CENTER);
      if(btnLeft != null) {
         panel.add(btnLeft, BorderLayout.WEST);
         leftBorder = 2;
      }
      if(btnRight != null) {
         panel.add(btnRight, BorderLayout.EAST);
         rightBorder = 2;
      }
      panel.setBorder(inactiveBorder);
      FocusListeners.recursiveFocusListener(panel, SwingListeners.focusListener(this::focusListener));
      setBorder(BorderFactory.createEmptyBorder(0, leftBorder, 0, rightBorder));
   }

   /**
    * Convenience method for adding a document listener to the text field
    *
    * @param listener the listener
    */
   public void addDocumentListener(@NonNull DocumentListener listener) {
      getDocument().addDocumentListener(listener);
   }

   /**
    * Add left button action listener.
    *
    * @param listener the listener
    */
   public void addLeftButtonActionListener(ActionListener listener) {
      btnLeft.addActionListener(listener);
   }

   /**
    * Add right button action listener.
    *
    * @param listener the listener
    */
   public void addRightButtonActionListener(ActionListener listener) {
      btnRight.addActionListener(listener);
   }

   private void focusListener(FocusEventType type, FocusEvent e) {
      if(type == FocusEventType.FOCUS_GAINED) {
         panel.setBorder(activeBorder);
      } else {
         panel.setBorder(inactiveBorder);
      }
      panel.invalidate();
   }

   /**
    * @return the left button
    */
   public JButton getLeftButton() {
      return btnLeft;
   }

   /**
    * @return the right button
    */
   public JButton getRightButton() {
      return btnRight;
   }

   @Override
   public JComponent getRoot() {
      return panel;
   }
}//END OF MangoButtonedTextField
