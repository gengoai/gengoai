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

import com.gengoai.collection.Iterables;
import com.gengoai.collection.Lists;
import com.gengoai.collection.tree.IntervalTree;
import com.gengoai.collection.tree.Span;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.component.listener.SwingListeners;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.component.listener.SwingListeners.mouseWheelMoved;

public class MangoStyledSpanTextPane extends JComponent {
   private static final long serialVersionUID = 1L;
   public static final String DEFAULT_HIGHLIGHT_STYLE_NAME = "**DEFAULT_HIGHLIGHT**";
   protected final FluentStyle defaultHighlightStyle;
   private final IntervalTree<StyledSpan> range2Style = new IntervalTree<>();
   private final MangoTextPane editor;
   private final JScrollPane scrollPane;
   private final AtomicBoolean allowZoom = new AtomicBoolean(true);
   private final AtomicBoolean viewTooltips = new AtomicBoolean(true);

   public MangoStyledSpanTextPane() {
      editor = with(new MangoTextPane(), $ -> {
         $.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
         $.setEditable(false);
         $.setCharacterAttributes($.getDefaultStyle(), true);
         $.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
         $.setMargin(new Insets(8, 8, 8, 8));
         $.setLineSpacing(0.5f);
         $.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
         $.addMouseListener(SwingListeners.mouseReleased(this::autoExpandSelection));
         $.addMouseMotionListener(SwingListeners.mouseMoved(e -> {
            if(viewTooltips.get()) {
               int index = $.viewToModel2D(e.getPoint());
               List<StyledSpan> styledSpans = Lists.asArrayList(range2Style.overlapping(Span.of(index, index + 1)));
               onTextHover(index, styledSpans);
            }
         }));
         $.addKeyListener(SwingListeners.keyReleased(e -> {
            if(allowZoom.get()) {
               if(e.isControlDown() && e.getKeyChar() == '+') {
                  increaseFontSize();
               } else if(e.isControlDown() && e.getKeyChar() == '-') {
                  decreaseFontSize();
               }
            }
         }));
      });
      defaultHighlightStyle = new FluentStyle(editor.addStyle(DEFAULT_HIGHLIGHT_STYLE_NAME, null));
      defaultHighlightStyle.bold(true).background(Color.YELLOW).foreground(Color.BLACK);
      scrollPane = new JScrollPane(editor);
      scrollPane.addMouseWheelListener(mouseWheelMoved(e -> {
         if(allowZoom.get()) {
            if(e.isControlDown() && e.getWheelRotation() < 0) {
               increaseFontSize();
            } else if(e.isControlDown() && e.getWheelRotation() > 0) {
               decreaseFontSize();
            }
         }
      }));
      setLayout(new BorderLayout());
      add(scrollPane, BorderLayout.CENTER);
   }

   public synchronized void addCaretListener(CaretListener listener) {
      editor.addCaretListener(listener);
   }

   @Override
   public synchronized void addFocusListener(FocusListener l) {
      super.addFocusListener(l);
      editor.addFocusListener(l);
   }

   public void addHorizontalScrollListener(AdjustmentListener l) {
      scrollPane.getHorizontalScrollBar().addAdjustmentListener(l);
   }

   @Override
   public synchronized void addMouseListener(MouseListener mouseListener) {
      editor.addMouseListener(mouseListener);
   }

   @Override
   public synchronized void addMouseMotionListener(MouseMotionListener listener) {
      editor.addMouseMotionListener(listener);
   }

   public void addSelectionChangeListener(@NonNull Consumer<SelectionChangeEvent> consumer) {
      editor.addSelectionChangeListener(consumer);
   }

   public void addStyle(String name, @NonNull Consumer<FluentStyle> styleInitializer) {
      editor.addAnSetStyle(name, styleInitializer);
   }

   public void addVerticalScrollListener(AdjustmentListener l) {
      scrollPane.getVerticalScrollBar().addAdjustmentListener(l);
   }

   public boolean allowZoom() {
      return allowZoom.get();
   }

   protected void autoExpandSelection(MouseEvent event) {
      return;
   }

   public int calculateMinimumHeight() {
      return editor.calculateMinimumHeight();
   }

   public void clearAllStyles() {
      range2Style.clear();
      ;
      editor.setStyle(0, editor.getText().length(), editor.getDefaultStyle().getName());
   }

   public void clearStyle(int start, int end) {
      for(StyledSpan styledSpan : getStyleSpans(start, end)) {
         range2Style.remove(styledSpan);
         editor.getStyledDocument()
               .setCharacterAttributes(styledSpan.start(), styledSpan.length(), editor.getDefaultStyle(), true);
      }
   }

   public boolean containsStyle(String style) {
      return editor.getStyle(style) != null;
   }

   @Override
   public JToolTip createToolTip() {
      return editor.createToolTip();
   }

   private void decreaseFontSize() {
      Font font = editor.getFont();
      if(font.getSize() > 12) {
         Fonts.setFontSize(editor, font.getSize() - 2f);
         StyleConstants.setFontSize(editor.getStyle(StyleContext.DEFAULT_STYLE), editor.getFont().getSize());
      }
   }

   public void defaultStyleBackground(Color color) {
      defaultHighlightStyle.background(color);
   }

   public void defaultStyleForeground(Color color) {
      defaultHighlightStyle.foreground(color);
   }

   public void focusOnEditor() {
      editor.requestFocus();
   }

   @Override
   public Color getBackground() {
      return editor.getBackground();
   }

   public StyledSpan getBestMatchingSelectedStyledSpan() {
      return getBestMatchingStyledSpan(getSelectionStart(), getSelectionEnd());
   }

   public StyledSpan getBestMatchingStyledSpan(final int start, final int end) {
      List<StyledSpan> spans = Lists.asArrayList(range2Style.overlapping(Span.of(start, end)));
      return spans.stream().max((s1, s2) -> {
         int cmp = Integer.compare(Math.abs(s1.start() - start), Math.abs(s2.start() - start));
         if(cmp == 0) {
            cmp = Integer.compare(s1.length(), s2.length());
         } else {
            cmp = -cmp;
         }
         return cmp;
      }).orElse(null);
   }

   public ActionMap getEditorActionMap() {
      return editor.getActionMap();
   }

   public InputMap getEditorInputMap() {
      return editor.getInputMap();
   }

   public InputMap getEditorInputMap(int mode) {
      return editor.getInputMap(mode);
   }

   @Override
   public Font getFont() {
      return editor.getFont();
   }

   @Override
   public Color getForeground() {
      return editor.getForeground();
   }

   @Override
   public synchronized KeyListener[] getKeyListeners() {
      return editor.getKeyListeners();
   }

   public List<StyledSpan> getSelectedStyleSpans() {
      return getStyleSpans(getSelectionStart(), getSelectionEnd());
   }

   public String getSelectedText() {
      return editor.getSelectedText();
   }

   public int getSelectionEnd() {
      return editor.getSelectionEnd();
   }

   public int getSelectionStart() {
      return editor.getSelectionStart();
   }

   public List<StyledSpan> getStyleSpans(int start, int end) {
      return Lists.asArrayList(range2Style.overlapping(Span.of(start, end)));
   }

   public String getText() {
      return editor.getText();
   }

   public String getText(int start, int length) throws BadLocationException {
      return editor.getText(start, length);
   }

   public int getTextAtPosition(Point2D point2D) {
      return editor.viewToModel2D(point2D);
   }

   @Override
   public boolean hasFocus() {
      return editor.hasFocus();
   }

   public boolean hasSelection() {
      return editor.getSelectionStart() < editor.getSelectionEnd();
   }

   public void highlight(int start, int end, String style) {
      highlight(start, end, style, style);
   }

   public void highlight(int start, int end, String style, String label) {
      range2Style.add(new StyledSpan(start, end, style, label));
      editor.getStyledDocument().setCharacterAttributes(start, end - start, editor.getStyle(style), true);
   }

   private void increaseFontSize() {
      Font font = editor.getFont();
      if(font.getSize() < 24) {
         Fonts.setFontSize(editor, font.getSize() + 2f);
         StyleConstants.setFontSize(editor.getStyle(StyleContext.DEFAULT_STYLE), editor.getFont().getSize());
      }
   }

   public Point modelToView(int off) {
      try {
         Rectangle2D r = editor.modelToView2D(off);
         return new Point((int) r.getX() - scrollPane.getHorizontalScrollBar().getValue(),
                          (int) r.getY() - scrollPane.getVerticalScrollBar().getValue());
      } catch(BadLocationException e) {
         throw new RuntimeException(e);
      }
   }

   public StyledSpan nextStyledSpan(Span span) {
      return nextStyledSpan(span.start(), span.end());
   }

   public StyledSpan nextStyledSpan(int start, int end) {
      return Iterables.getFirst(range2Style.higher(new StyledSpan(start, end, "", "")), null);
   }

   protected void onTextHover(int cursorPosition, List<StyledSpan> styledSpans) {
      if(styledSpans.isEmpty()) {
         setToolTipText(null);
      } else {
         setToolTipText(styledSpans.stream().map(s -> s.label).collect(Collectors.joining("\n")));
      }
   }

   public StyledSpan previousStyledSpan(Span span) {
      return previousStyledSpan(span.start(), span.end());
   }

   public StyledSpan previousStyledSpan(int start, int end) {
      return Iterables.getFirst(range2Style.lower(new StyledSpan(start, end, "", "")), null);
   }

   public void removeStyle(String styleName) {
      editor.getStyledDocument().removeStyle(styleName);
   }

   public void scrollToTopLeft() {
      editor.select(0, 0);
      scrollPane.getHorizontalScrollBar().setValue(0);
      scrollPane.getVerticalScrollBar().setValue(0);
   }

   public void setAllowZoom(boolean value) {
      allowZoom.set(value);
   }

   public void setAlwaysHighlight(boolean alwaysHighlight) {
      editor.setAlwaysHighlight(alwaysHighlight);
   }

   @Override
   public void setBorder(Border border) {
      if(scrollPane != null) {
         scrollPane.setBorder(border);
      }
   }

   @Override
   public void setComponentPopupMenu(JPopupMenu jPopupMenu) {
      editor.setComponentPopupMenu(jPopupMenu);
   }

   public void setEditorInputMap(InputMap inputMap) {
      editor.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
   }

   @Override
   public void setFont(@NonNull Font font) {
      if(editor != null) {
         editor.setFont(font);
      }
   }

   public void setLineSpacing(float factor) {
      editor.setLineSpacing(factor);
   }

   public void setSelectedTextColor(Color color) {
      editor.setSelectedTextColor(color);
   }

   public void setSelectionColor(Color color) {
      editor.setSelectionColor(color);
   }

   public void setSelectionRange(int start, int end) {
      editor.requestFocus();
      editor.select(start, end);
   }

   public void setShowTooltips(boolean areTooltipsVisible) {
      viewTooltips.set(areTooltipsVisible);
   }

   public void setStyle(int start, int end, String styleName) {
      editor.getStyledDocument().setCharacterAttributes(start, end - start, editor.getStyle(styleName), true);
   }

   public void setText(String text) {
      editor.setText(text);
      range2Style.clear();
      scrollPane.getVerticalScrollBar().setValue(0);
      scrollPane.getHorizontalScrollBar().setValue(0);
   }

   @Override
   public void setToolTipText(String text) {
      editor.setToolTipText(text);
   }

   public void updateHighlight(int start, int end, String oldStyle, String newStyle) {
      range2Style.remove(new StyledSpan(start, end, oldStyle, oldStyle));
      highlight(start, end, newStyle);
   }

}//END OF HighlightedRangeViewer
