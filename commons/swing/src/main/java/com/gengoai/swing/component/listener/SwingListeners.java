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

import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The type Swing listeners.
 */
public class SwingListeners {

   private SwingListeners() {
      throw new IllegalAccessError();
   }

   /**
    * Component hidden component listener.
    *
    * @param eventHandler the event handler
    * @return the component listener
    */
   public static ComponentListener componentHidden(@NonNull Consumer<ComponentEvent> eventHandler) {
      return new ComponentAdapter() {
         @Override
         public void componentHidden(ComponentEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Component listener component listener.
    *
    * @param listener the listener
    * @return the component listener
    */
   public static ComponentListener componentListener(@NonNull BiConsumer<ComponentEventType, ComponentEvent> listener) {
      return new ComponentListener() {
         @Override
         public void componentHidden(ComponentEvent e) {
            listener.accept(ComponentEventType.HIDDEN, e);
         }

         @Override
         public void componentMoved(ComponentEvent e) {
            listener.accept(ComponentEventType.MOVED, e);
         }

         @Override
         public void componentResized(ComponentEvent e) {
            listener.accept(ComponentEventType.RESIZED, e);
         }

         @Override
         public void componentShown(ComponentEvent e) {
            listener.accept(ComponentEventType.SHOWN, e);
         }
      };
   }

   /**
    * Component moved component listener.
    *
    * @param eventHandler the event handler
    * @return the component listener
    */
   public static ComponentListener componentMoved(@NonNull Consumer<ComponentEvent> eventHandler) {
      return new ComponentAdapter() {
         @Override
         public void componentMoved(ComponentEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Component resized component listener.
    *
    * @param eventHandler the event handler
    * @return the component listener
    */
   public static ComponentListener componentResized(@NonNull Consumer<ComponentEvent> eventHandler) {
      return new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Component shown component listener.
    *
    * @param eventHandler the event handler
    * @return the component listener
    */
   public static ComponentListener componentShown(@NonNull Consumer<ComponentEvent> eventHandler) {
      return new ComponentAdapter() {
         @Override
         public void componentShown(ComponentEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   public static DocumentListener documentListener(@NonNull BiConsumer<DocumentEventType, DocumentEvent> eventHandler) {
      return new DocumentListener() {
         @Override
         public void changedUpdate(DocumentEvent e) {
            eventHandler.accept(DocumentEventType.CHANGE, e);
         }

         @Override
         public void insertUpdate(DocumentEvent e) {
            eventHandler.accept(DocumentEventType.INSERT, e);
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            eventHandler.accept(DocumentEventType.REMOVE, e);
         }
      };
   }

   public static KeyListener enterKeyPressed(@NonNull Consumer<KeyEvent> eventConsumer) {
      return keyPressed(e -> {
         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            eventConsumer.accept(e);
         }
      });
   }

   /**
    * Action fluent action.
    *
    * @param name     the name
    * @param listener the listener
    * @return the fluent action
    */
   public static FluentAction fluentAction(@NonNull String name, @NonNull ActionListener listener) {
      return new FluentAction(name, listener);
   }

   /**
    * Action fluent action.
    *
    * @param listener the listener
    * @return the fluent action
    */
   public static FluentAction fluentAction(@NonNull ActionListener listener) {
      return new FluentAction(listener);
   }

   /**
    * Focus listener focus listener.
    *
    * @param listener the listener
    * @return the focus listener
    */
   public static FocusListener focusListener(@NonNull BiConsumer<FocusEventType, FocusEvent> listener) {
      return new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent e) {
            listener.accept(FocusEventType.FOCUS_GAINED, e);
         }

         @Override
         public void focusLost(FocusEvent e) {
            listener.accept(FocusEventType.FOCUS_LOST, e);
         }
      };
   }

   /**
    * Key listener key listener.
    *
    * @param listener the listener
    * @return the key listener
    */
   public static KeyListener keyListener(@NonNull BiConsumer<KeyEventType, KeyEvent> listener) {
      return new KeyListener() {
         @Override
         public void keyPressed(KeyEvent e) {
            listener.accept(KeyEventType.PRESSED, e);
         }

         @Override
         public void keyReleased(KeyEvent e) {
            listener.accept(KeyEventType.RELEASED, e);
         }

         @Override
         public void keyTyped(KeyEvent e) {
            listener.accept(KeyEventType.TYPED, e);
         }
      };
   }

   /**
    * Key pressed key listener.
    *
    * @param listener the listener
    * @return the key listener
    */
   public static KeyListener keyPressed(@NonNull Consumer<KeyEvent> listener) {
      return new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            listener.accept(e);
         }
      };
   }

   /**
    * Key released key listener.
    *
    * @param eventHandler the event handler
    * @return the key listener
    */
   public static KeyListener keyReleased(@NonNull Consumer<KeyEvent> eventHandler) {
      return new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Key typed key listener.
    *
    * @param listener the listener
    * @return the key listener
    */
   public static KeyListener keyTyped(@NonNull Consumer<KeyEvent> listener) {
      return new KeyAdapter() {
         @Override
         public void keyTyped(KeyEvent keyEvent) {
            listener.accept(keyEvent);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mouseClicked events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseListener mouseClicked(Consumer<MouseEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mouseClicked events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseListener mouseDoubleClicked(Consumer<MouseEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               eventHandler.accept(e);
            }
         }
      };
   }

   /**
    * Constructs a MouseMotionListener that handles mouseDragged events.
    *
    * @param mouseDragged the mouseDragged handler
    * @return the MouseMotionListener
    */
   public static MouseMotionListener mouseDragged(@NonNull Consumer<MouseEvent> mouseDragged) {
      return new MouseMotionAdapter() {
         @Override
         public void mouseDragged(MouseEvent e) {
            mouseDragged.accept(e);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mouseEntered events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseListener mouseEntered(@NonNull Consumer<MouseEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mouseExited events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseListener mouseExited(@NonNull Consumer<MouseEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mouseExited(MouseEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Mouse listener mouse listener.
    *
    * @param listener the listener
    * @return the mouse listener
    */
   public static MouseListener mouseListener(@NonNull BiConsumer<MouseEventType, MouseEvent> listener) {
      return new MouseListener() {
         @Override
         public void mouseClicked(MouseEvent e) {
            listener.accept(MouseEventType.CLICKED, e);
         }

         @Override
         public void mouseEntered(MouseEvent e) {
            listener.accept(MouseEventType.ENTERED, e);
         }

         @Override
         public void mouseExited(MouseEvent e) {
            listener.accept(MouseEventType.EXITED, e);
         }

         @Override
         public void mousePressed(MouseEvent e) {
            listener.accept(MouseEventType.PRESSED, e);
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            listener.accept(MouseEventType.RELEASED, e);
         }
      };
   }

   /**
    * Mouse motion listener mouse motion listener.
    *
    * @param listener the listener
    * @return the mouse motion listener
    */
   public static MouseMotionListener mouseMotionListener(@NonNull BiConsumer<MouseEventType, MouseEvent> listener) {
      return new MouseMotionListener() {
         @Override
         public void mouseDragged(MouseEvent e) {
            listener.accept(MouseEventType.DRAGGED, e);
         }

         @Override
         public void mouseMoved(MouseEvent e) {
            listener.accept(MouseEventType.MOVED, e);
         }
      };
   }

   /**
    * Constructs a MouseMotionListener that handles mouseMoved events.
    *
    * @param mouseMoved the mouseMove handler
    * @return the MouseMotionListener
    */
   public static MouseMotionListener mouseMoved(@NonNull Consumer<MouseEvent> mouseMoved) {
      return new MouseMotionAdapter() {
         @Override
         public void mouseMoved(MouseEvent e) {
            mouseMoved.accept(e);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mousePressed events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseListener mousePressed(Consumer<MouseEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mouseReleased events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseListener mouseReleased(@NonNull Consumer<MouseEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mouseReleased(MouseEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Constructs a MouseAdapter that handles mouseWheelMoved events.
    *
    * @param eventHandler the event handler
    * @return the MouseAdapter
    */
   public static MouseWheelListener mouseWheelMoved(@NonNull Consumer<MouseWheelEvent> eventHandler) {
      return new MouseAdapter() {
         @Override
         public void mouseWheelMoved(MouseWheelEvent e) {
            eventHandler.accept(e);
         }
      };
   }

   /**
    * Remove all key listeners
    *
    * @return This FluentComponent
    */
   public static <T extends JComponent> T removeAllKeyListeners(@NonNull T component) {
      for (KeyListener kl : component.getListeners(KeyListener.class)) {
         component.removeKeyListener(kl);
      }
      return component;
   }

}//END OF SwingListeners
