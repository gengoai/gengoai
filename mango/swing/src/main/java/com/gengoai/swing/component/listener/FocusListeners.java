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

import com.gengoai.conversion.Cast;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Static helper methods for creating FocusListeners.
 */
public final class FocusListeners {

   private FocusListeners() {
      throw new IllegalAccessError();
   }

   /**
    * Creates a FocusListener with the given focus gained and focus lost event handlers.
    *
    * @param focusGained the focus gained event handler
    * @param focusLost   the focus lost event handler
    * @return the FocusListener
    */
   public static FocusListener focusListener(@NonNull Consumer<FocusEvent> focusGained,
                                             @NonNull Consumer<FocusEvent> focusLost) {
      return new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent e) {
            focusGained.accept(e);
         }

         @Override
         public void focusLost(FocusEvent e) {
            focusLost.accept(e);
         }
      };
   }

   public static void interceptFocusListener(@NonNull JComponent source,
                                             @NonNull JComponent dispatchTo,
                                             boolean recursive) {
      final FocusListener listener = new FocusListener() {
         @Override
         public void focusGained(FocusEvent e) {
            dispatchTo.dispatchEvent(new FocusEvent(dispatchTo,
                                                    e.getID(),
                                                    e.isTemporary()));
         }

         @Override
         public void focusLost(FocusEvent e) {
            dispatchTo.dispatchEvent(new FocusEvent(dispatchTo,
                                                    e.getID(),
                                                    e.isTemporary()));
         }
      };
      interceptFocusListener(listener, source, recursive);
   }

   private static void interceptFocusListener(FocusListener listener, Component child, boolean recursive) {
      child.addFocusListener(listener);
      if(recursive && child instanceof JComponent) {
         for(Component component : Cast.<JComponent>as(child).getComponents()) {
            interceptFocusListener(listener, component, recursive);
         }
      }
   }

   /**
    * Creates a FocusListener with the given focus gained event handler.
    *
    * @param focusGained the focus gained event handler
    * @return the FocusListener
    */
   public static FocusListener onFocusGained(@NonNull Consumer<FocusEvent> focusGained) {
      return new FocusAdapter() {
         @Override
         public void focusGained(FocusEvent e) {
            focusGained.accept(e);
         }
      };
   }

   public static <T extends JComponent> T onFocusGained(@NonNull T component,
                                                        @NonNull Consumer<FocusEvent> focusGained) {
      component.addFocusListener(onFocusGained(focusGained));
      return component;
   }

   /**
    * Creates a FocusListener with the given focus lost event handler.
    *
    * @param focusLost the focus lost event handler
    * @return the FocusListener
    */
   public static FocusListener onFocusLost(@NonNull Consumer<FocusEvent> focusLost) {
      return new FocusAdapter() {
         @Override
         public void focusLost(FocusEvent e) {
            focusLost.accept(e);
         }
      };
   }

   public static void recursiveFocusListener(@NonNull JComponent component,
                                             @NonNull FocusListener focusListener) {
      Queue<Component> queue = new LinkedList<>();
      queue.add(component);
      while(!queue.isEmpty()) {
         Component c = queue.remove();
         c.addFocusListener(focusListener);
         if(c instanceof JComponent) {
            queue.addAll(Arrays.asList(Cast.<JComponent>as(c).getComponents()));
         }
      }
   }

}//END OF FocusListeners
