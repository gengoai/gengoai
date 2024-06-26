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

package com.gengoai.hermes.tools.swing.gui;

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

import com.gengoai.collection.Maps;
import com.gengoai.collection.tree.Span;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.AnnotationType;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.Strings;
import com.gengoai.swing.component.MangoStyledSpanTextPane;
import com.gengoai.swing.component.StyledSpan;
import com.gengoai.swing.component.listener.AutoExpandAction;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

public class HStringViewer extends MangoStyledSpanTextPane {
    private final HString context;
    private final Map<KeyStroke, Action> actions = Maps.hashMapOf(
            $(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
              SwingListeners.fluentAction("SelectFirstToken",
                                          e -> setSelectionRange(getSelectionStart(), getSelectionStart()))),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
              SwingListeners.fluentAction("SelectFirstToken", e -> selectFirstToken())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
              SwingListeners.fluentAction("SelectNextToken", e -> nextSelection())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
              SwingListeners.fluentAction("SelectUpToken", e -> selectUp())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
              SwingListeners.fluentAction("SelectDownToken", e -> selectDown())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
              SwingListeners.fluentAction("SelectPreviousToken", e -> prevSelection())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK),
              SwingListeners.fluentAction("ExpandNextToken", e -> nextSelectionExpand())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK),
              SwingListeners.fluentAction("ExpandPreviousToken", e -> prevSelectionExpand())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
              SwingListeners.fluentAction("SelectNextAnnotation", e -> nextAnnotationSelection())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK),
              SwingListeners.fluentAction("SelectPreviousAnnotation", e -> previousAnnotationSelection())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK),
              SwingListeners.fluentAction("ShrinkPreviousToken", e -> shrinkRight())),
            $(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK),
              SwingListeners.fluentAction("ShrinkNextToken", e -> shrinkLeft())));

    public HStringViewer(HString document) {
        this.context = document;
        setText(document.toString().replace("\r", " "));
        setAlwaysHighlight(true);
        defaultHighlightStyle.foreground(getBackground()).background(getForeground());
        var input = new InputMap();
        actions.forEach(input::put);
        setEditorInputMap(input);
    }

    @Override
    protected void autoExpandSelection(MouseEvent event) {
        if (event.isAltDown()) {
            return;
        }
        Span span;
        if (context.document().isCompleted(Types.TOKEN)) {
            span = expandOnTokens(getSelectionStart(),
                                  getSelectionEnd(),
                                  getText(),
                                  getBestMatchingSelectedStyledSpan(),
                                  event);
        } else if (context.getLanguage().usesWhitespace()) {
            span = AutoExpandAction.contiguousNonWhitespace.expand(getSelectionStart(),
                                                                   getSelectionEnd(),
                                                                   getText(),
                                                                   getBestMatchingSelectedStyledSpan());
        } else {
            return;
        }
        setSelectionRange(span.start(), span.end());
    }

    private Span expandOnTokens(int start, int end, String text, StyledSpan span, MouseEvent event) {
        HString hSpan = HString.union(getAnnotations(Types.TOKEN, start, end));
        if (event.isControlDown() || hSpan.isEmpty()) {
            return Span.of(start, end);
        }
        if (span != null && span.encloses(hSpan)) {
            return span;
        }
        return Span.of(hSpan.start() - context.start(), hSpan.end() - context.start());
    }

    public List<Annotation> getAnnotations(AnnotationType annotationType, int start, int end) {
        return context.enclosedAnnotations(annotationType)
                      .stream()
                      .filter(a -> a.overlaps(Span.of(context.start() + start, context.start() + end)))
                      .collect(Collectors.toList());
    }

    private HString getBestSelectedSpan() {
        if (hasSelection()) {
            return context.substring(getSelectionStart(), getSelectionEnd());
        }
        return context.substring(getSelectionStart(), getSelectionStart() + 1);
    }

    public List<Annotation> getSelectedAnnotations(AnnotationType annotationType) {
        return getAnnotations(annotationType, getSelectionStart(), getSelectionEnd());
    }

    public HStringViewer highlightAnnotation(Annotation annotation) {
        if (containsStyle(annotation.getTag().label())) {
            return highlightAnnotation(annotation, annotation.getTag().label());
        } else {
            return highlightAnnotation(annotation, DEFAULT_HIGHLIGHT_STYLE_NAME, annotation.getTag().label());
        }
    }

    public HStringViewer highlightAnnotation(Annotation annotation, String style) {
        highlight(annotation.start() - context.start(), annotation.end() - context.start(), style);
        return this;
    }

    public HStringViewer highlightAnnotation(Annotation annotation, String style, String label) {
        highlight(annotation.start() - context.start(), annotation.end() - context.start(), style, label);
        return this;
    }

    public HStringViewer highlightAnnotations(Iterable<Annotation> annotations) {
        annotations.forEach(this::highlightAnnotation);
        return this;
    }

    public HStringViewer highlightAnnotationsOfType(AnnotationType annotationType,
                                                    Function<Annotation, String> annotationToStyle) {
        for (Annotation annotation : context.enclosedAnnotations(annotationType)) {
            String styleName = annotationToStyle.apply(annotation);
            if (styleName != null) {
                highlight(annotation.start() - context.start(), annotation.end() - context.start(), styleName);
            }
        }
        return this;
    }

    private void move(String command) {
        Action makeItHappen = getEditorActionMap().get(command);
        makeItHappen.actionPerformed(null);
    }

    private void nextAnnotationSelection() {
        if (context.document().isCompleted(Types.TOKEN)) {
            StyledSpan ss = nextStyledSpan(getBestSelectedSpan().lastToken().next());
            if (ss != null) {
                setSelectionRange(ss.start(), ss.end());
            }
        }
    }

    private void nextSelection() {
        if (context.document().isCompleted(Types.TOKEN)) {
            if (getSelectionStart() == 0 && getSelectionEnd() == 0) {
                selectFirstToken();
            } else {
                Annotation token = getBestSelectedSpan().lastToken().next();
                if (!token.isEmpty()) {
                    setSelectionRange(token.start(), token.end());
                }
            }
        }
    }

    private void nextSelectionExpand() {
        if (context.document().isCompleted(Types.TOKEN)) {
            if (getSelectionStart() == 0 && getSelectionEnd() == 0) {
                selectFirstToken();
            } else {
                Annotation token = getBestSelectedSpan().lastToken().next();
                if (!token.isEmpty()) {
                    setSelectionRange(getSelectionStart(), token.end());
                }
            }
        }
    }

    private void prevSelection() {
        if (context.document().isCompleted(Types.TOKEN)) {
            Annotation token = getBestSelectedSpan().firstToken().previous();
            if (!token.isEmpty()) {
                setSelectionRange(token.start(), token.end());
            }
        }
    }

    private void prevSelectionExpand() {
        if (context.document().isCompleted(Types.TOKEN)) {
            Annotation token = getBestSelectedSpan().firstToken().previous();
            if (!token.isEmpty()) {
                setSelectionRange(token.start(), getSelectionEnd());
            }
        }
    }

    private void previousAnnotationSelection() {
        if (context.document().isCompleted(Types.TOKEN)) {
            StyledSpan ss = previousStyledSpan(getBestSelectedSpan().firstToken().previous());
            if (ss != null) {
                setSelectionRange(ss.start(), ss.end());
            }
        }
    }

    private void selectDown() {
        move("caret-down");
        if (getSelectionStart() >= context.length()) {
            return;
        }
        HString h = context.substring(getSelectionStart(), getSelectionEnd() + 1);
        while (Strings.isNullOrBlank(h) && h.end() < context.length()) {
            h = context.substring(h.end(), h.end() + 1);
        }
        if (h.sentence().end() == h.start()) {
            h = h.sentence();
            if (!h.isEmpty()) {
                h = h.firstToken();
                setSelectionRange(h.start(), h.end());
            }
        } else {
            h = h.firstToken();
            setSelectionRange(h.start(), h.end());
        }
    }

    private void selectFirstToken() {
        setSelectionRange(0, 0);
        Annotation token = getBestSelectedSpan().firstToken();
        if (!token.isEmpty()) {
            setSelectionRange(token.start(), token.end());
        }
    }

    private void selectUp() {
        move("caret-up");
        HString h = context.substring(getSelectionStart(), getSelectionEnd() + 1);
        while (Strings.isNullOrBlank(h) && h.start() > 0) {
            h = context.substring(h.start() - 1, h.start());
        }
        if (h.sentence().end() == h.start()) {
            h = h.sentence();
            if (!h.isEmpty()) {
                h = h.lastToken();
                setSelectionRange(h.start(), h.end());
            }
        } else {
            h = h.firstToken();
            setSelectionRange(h.start(), h.end());
        }
    }

    private void shrinkLeft() {
        if (hasSelection()) {
            HString span = getBestSelectedSpan();
            if (span.tokenLength() > 1) {
                span = HString.union(span.tokens().subList(1, span.tokenLength()));
                setSelectionRange(span.start(), span.end());
            }
        }
    }

    private void shrinkRight() {
        if (hasSelection()) {
            HString span = getBestSelectedSpan();
            if (span.tokenLength() > 1) {
                span = HString.union(span.tokens().subList(0, span.tokenLength() - 1));
                setSelectionRange(span.start(), span.end());
            }
        }
    }

}//END OF HStringViewer
