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

import com.gengoai.collection.tree.Span;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.gengoai.swing.component.listener.SwingListeners.mouseReleased;

public class MangoTextPane extends JTextPane {
   private static final long serialVersionUID = 1L;
   private final Style DEFAULT;
   private final AtomicBoolean alwaysHighlight = new AtomicBoolean(false);
   private final AtomicReference<Span> selection = new AtomicReference<>();

   public MangoTextPane() {
      this(new DefaultStyledDocument());
   }

   public MangoTextPane(@NonNull StyledDocument document) {
      super(document);
      setCaret(new DefaultCaret() {
         private static final long serialVersionUID = 1L;
         @Override
         public void setSelectionVisible(boolean visible) {
            super.setSelectionVisible(visible || alwaysHighlight.get());
         }
      });
      this.DEFAULT = addStyle("DEFAULT", null);
   }

   public MangoTextPane addAnSetStyle(String name, @NonNull Consumer<FluentStyle> styleInitializer) {
      styleInitializer.accept(new FluentStyle(super.addStyle(name, null)));
      return this;
   }

   public MangoTextPane addSelectionChangeListener(@NonNull Consumer<SelectionChangeEvent> listener) {
      addMouseListener(mouseReleased(e -> {
         Span newSelection = null;
         if (getSelectionStart() < getSelectionEnd()) {
            newSelection = Span.of(getSelectionStart(), getSelectionEnd());
         }
         if (selection.get() == null || newSelection == null || !selection.get().equals(newSelection)) {
            listener.accept(new SelectionChangeEvent(selection.get(), newSelection));
         }
         selection.set(newSelection);
      }));
      return this;
   }

   public FluentStyle addStyle(String name) {
      return new FluentStyle(super.addStyle(name, null));
   }

   public int calculateMinimumHeight() {
      if (getText().length() == 0) {
         return 0;
      }
      AttributedString text = new AttributedString(getText());
      FontRenderContext frc = getFontMetrics(getFont()).getFontRenderContext();
      AttributedCharacterIterator charIt = text.getIterator();
      LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIt, frc);
      float formatWidth = (float) getSize().width;
      lineMeasurer.setPosition(charIt.getBeginIndex());
      int noLines = 0;
      while (lineMeasurer.getPosition() < charIt.getEndIndex()) {
         lineMeasurer.nextLayout(formatWidth);
         noLines++;
      }
      noLines += 2;
      int fh = getFontMetrics(getFont()).getHeight();
      int lineHeight = (int) Math.ceil(fh + (fh * 0.5));
      Insets insets = getInsets();
      return (int) Math.ceil(noLines * lineHeight) + insets.top + insets.bottom;
   }

   public MangoTextPane characterAttributes(AttributeSet attr, boolean replace) {
      setCharacterAttributes(attr, replace);
      return this;
   }

   public Style getDefaultStyle() {
      return DEFAULT;
   }

   public Span getSelectionRange() {
      return selection.get();
   }

   public boolean isAlwaysHighlight() {
      return alwaysHighlight.get();
   }

   public MangoTextPane setAlwaysHighlight(boolean value) {
      this.alwaysHighlight.set(value);
      return this;
   }

   public void setLineSpacing(float space) {
      MutableAttributeSet aSet = new SimpleAttributeSet(getParagraphAttributes());
      StyleConstants.setLineSpacing(aSet, space);
      setParagraphAttributes(aSet, true);
   }

   public MangoTextPane setStyle(int start, int end, String styleName) {
      getStyledDocument().setCharacterAttributes(start, end - start, getStyle(styleName), true);
      return this;
   }

}
