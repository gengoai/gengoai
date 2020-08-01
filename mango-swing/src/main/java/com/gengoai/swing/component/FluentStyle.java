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

import javax.swing.Icon;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import java.awt.Color;
import java.awt.Component;

public class FluentStyle {
   private final Style style;

   public FluentStyle(Style style) {
      this.style = style;
   }

   public FluentStyle alignment(int value) {
      StyleConstants.setAlignment(style, value);
      return this;
   }

   public FluentStyle background(Color color) {
      StyleConstants.setBackground(style, color);
      return this;
   }

   public FluentStyle bidiLevel(int value) {
      StyleConstants.setBidiLevel(style, value);
      return this;
   }

   public FluentStyle bold(boolean value) {
      StyleConstants.setBold(style, value);
      return this;
   }

   public FluentStyle component(Component value) {
      StyleConstants.setComponent(style, value);
      return this;
   }

   public FluentStyle firstLineIndent(int value) {
      StyleConstants.setFirstLineIndent(style, value);
      return this;
   }

   public FluentStyle fontFamily(String value) {
      StyleConstants.setFontFamily(style, value);
      return this;
   }

   public FluentStyle fontSize(int value) {
      StyleConstants.setFontSize(style, value);
      return this;
   }

   public FluentStyle foreground(Color color) {
      StyleConstants.setForeground(style, color);
      return this;
   }

   public FluentStyle icon(Icon value) {
      StyleConstants.setIcon(style, value);
      return this;
   }

   public FluentStyle italic(boolean value) {
      StyleConstants.setItalic(style, value);
      return this;
   }

   public FluentStyle leftIndent(int value) {
      StyleConstants.setLeftIndent(style, value);
      return this;
   }

   public FluentStyle lineSpacing(float value) {
      StyleConstants.setLineSpacing(style, value);
      return this;
   }

   public FluentStyle rightIndent(int value) {
      StyleConstants.setRightIndent(style, value);
      return this;
   }

   public FluentStyle spaceAbove(float value) {
      StyleConstants.setSpaceAbove(style, value);
      return this;
   }

   public FluentStyle spaceBelow(float value) {
      StyleConstants.setSpaceBelow(style, value);
      return this;
   }

   public FluentStyle strikeThrough(boolean value) {
      StyleConstants.setStrikeThrough(style, value);
      return this;
   }

   public FluentStyle subscript(boolean value) {
      StyleConstants.setSubscript(style, value);
      return this;
   }

   public FluentStyle superscript(boolean value) {
      StyleConstants.setSuperscript(style, value);
      return this;
   }

   public FluentStyle tabSet(TabSet value) {
      StyleConstants.setTabSet(style, value);
      return this;
   }

   public FluentStyle underline(boolean value) {
      StyleConstants.setUnderline(style, value);
      return this;
   }

}//END OF FluentStyle
