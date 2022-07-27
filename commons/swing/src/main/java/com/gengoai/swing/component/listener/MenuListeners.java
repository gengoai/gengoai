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

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class MenuListeners {

   private MenuListeners() {
      throw new IllegalAccessError();
   }

   public static PopupMenuListener popupMenuCanceled(@NonNull Consumer<PopupMenuEvent> listener) {
      return new PopupMenuListener() {
         @Override
         public void popupMenuCanceled(PopupMenuEvent e) {
            listener.accept(e);
         }

         @Override
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

         }

         @Override
         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

         }
      };
   }

   public static PopupMenuListener popupMenuListener(@NonNull BiConsumer<PopupMenuEventType, PopupMenuEvent> listener) {
      return new PopupMenuListener() {
         @Override
         public void popupMenuCanceled(PopupMenuEvent e) {
            listener.accept(PopupMenuEventType.CANCELLED, e);
         }

         @Override
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            listener.accept(PopupMenuEventType.WILL_BECOME_INVISIBLE, e);
         }

         @Override
         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            listener.accept(PopupMenuEventType.WILL_BECOME_VISIBLE, e);
         }
      };
   }

   public static PopupMenuListener popupMenuWillBecomeInvisible(@NonNull Consumer<PopupMenuEvent> listener) {
      return new PopupMenuListener() {
         @Override
         public void popupMenuCanceled(PopupMenuEvent e) {

         }

         @Override
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            listener.accept(e);
         }

         @Override
         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

         }
      };
   }

   public static PopupMenuListener popupMenuWillBecomeVisible(@NonNull Consumer<PopupMenuEvent> listener) {
      return new PopupMenuListener() {
         @Override
         public void popupMenuCanceled(PopupMenuEvent e) {

         }

         @Override
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

         }

         @Override
         public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            listener.accept(e);
         }
      };
   }

}//END OF MenuListeners
