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

import lombok.NonNull;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public enum TextAlignment {
   HorizontalCenter {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setHorizontalTextPosition(SwingConstants.CENTER);
         label.setHorizontalAlignment(SwingConstants.CENTER);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         textField.setHorizontalAlignment(SwingConstants.CENTER);
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setHorizontalTextPosition(SwingConstants.CENTER);
         return button;
      }

   },
   HorizontalLeft {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setHorizontalTextPosition(SwingConstants.LEFT);
         label.setHorizontalAlignment(SwingConstants.LEFT);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         textField.setHorizontalAlignment(SwingConstants.LEFT);
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setHorizontalTextPosition(SwingConstants.LEFT);
         return button;
      }
   },
   HorizontalRight {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setHorizontalTextPosition(SwingConstants.RIGHT);
         label.setHorizontalAlignment(SwingConstants.RIGHT);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         textField.setHorizontalAlignment(SwingConstants.RIGHT);
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setHorizontalTextPosition(SwingConstants.RIGHT);
         return button;
      }
   },
   HorizontalLeading {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setHorizontalTextPosition(SwingConstants.LEADING);
         label.setHorizontalAlignment(SwingConstants.LEADING);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         textField.setHorizontalAlignment(SwingConstants.LEADING);
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setHorizontalTextPosition(SwingConstants.LEADING);
         return button;
      }
   },
   HorizontalTrailing {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setHorizontalTextPosition(SwingConstants.TRAILING);
         label.setHorizontalAlignment(SwingConstants.TRAILING);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         textField.setHorizontalAlignment(SwingConstants.TRAILING);
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setHorizontalTextPosition(SwingConstants.TRAILING);
         return button;
      }
   },
   VerticalTop {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setVerticalTextPosition(SwingConstants.TOP);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setVerticalTextPosition(SwingConstants.TOP);
         return button;
      }
   },
   VerticalCenter {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setVerticalTextPosition(SwingConstants.CENTER);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setVerticalTextPosition(SwingConstants.CENTER);
         return button;
      }
   },
   VerticalBottom {
      @Override
      public JLabel set(@NonNull JLabel label) {
         label.setVerticalTextPosition(SwingConstants.BOTTOM);
         return label;
      }

      @Override
      public JTextField set(@NonNull JTextField textField) {
         return textField;
      }

      @Override
      public <T extends AbstractButton> T set(@NonNull T button) {
         button.setVerticalTextPosition(SwingConstants.BOTTOM);
         return button;
      }
   };

   public abstract JLabel set(@NonNull JLabel label);

   public abstract JTextField set(@NonNull JTextField textField);

   public abstract <T extends AbstractButton> T set(@NonNull T button);
}//END OF TextAlignment
