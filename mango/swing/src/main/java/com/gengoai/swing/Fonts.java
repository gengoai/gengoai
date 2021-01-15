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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.stream.IntStream;

/**
 * Convenience methods for working with Fonts and FontMetrics
 */
public class Fonts {

   /**
    * Set the font style and size for the given component keeping the current font family.
    *
    * @param <T>       the component type
    * @param component the component
    * @param size      the new font size
    * @param style     the new font style
    * @return the component
    */
   public static <T extends Component> T adjustFont(@NonNull T component, int style, float size) {
      component.setFont(component.getFont().deriveFont(style, size));
      return component;
   }

   /**
    * Gets the average character font width for the given component
    *
    * @param component the component
    * @return the average font width
    */
   public static int getAverageFontWidth(@NonNull Component component) {
      return (int) IntStream.of(component.getFontMetrics(component.getFont()).getWidths()).average().orElse(1d);
   }

   /**
    * Gets the height of the font used by the given component
    *
    * @param component the component
    * @return the font height
    */
   public static int getFontHeight(@NonNull Component component) {
      return component.getFontMetrics(component.getFont()).getHeight();
   }

   /**
    * Gets the font metrics for the given component and its currently assigned font.
    *
    * @param component the component
    * @return the font metrics
    */
   public static FontMetrics getFontMetrics(@NonNull Component component) {
      return component.getFontMetrics(component.getFont());
   }

   /**
    * Gets the width of the given string using the component's font.
    *
    * @param component the component
    * @param string    the string
    * @return the font width
    */
   public static int getFontWidth(@NonNull Component component, @NonNull String string) {
      return component.getFontMetrics(component.getFont()).stringWidth(string);
   }

   /**
    * Gets the maximum character font width for the given component
    *
    * @param component the component
    * @return the maximum font width
    */
   public static int getMaxFontWidth(@NonNull Component component) {
      return IntStream.of(component.getFontMetrics(component.getFont()).getWidths()).max().orElse(1);
   }

   /**
    * Gets the minimum character font width for the given component
    *
    * @param component the component
    * @return the minimum font width
    */
   public static int getMinFontWidth(@NonNull Component component) {
      return IntStream.of(component.getFontMetrics(component.getFont()).getWidths()).min().orElse(1);
   }

   /**
    * Set the font family for the given component retaining all other font settings.
    *
    * @param <T>       the component type
    * @param component the component
    * @param fontName  the font faily name
    * @return the component
    */
   public static <T extends Component> T setFontFamily(@NonNull T component, String fontName) {
      component.setFont(new Font(fontName, component.getFont().getStyle(), component.getFont().getSize()));
      return component;
   }

   /**
    * Set the font size for the given component keeping the current font family and style.
    *
    * @param <T>       the component type
    * @param component the component
    * @param size      the new font size
    * @return the component
    */
   public static <T extends Component> T setFontSize(@NonNull T component, float size) {
      component.setFont(component.getFont().deriveFont(size));
      return component;
   }

   /**
    * Set the font style for the given component keeping the current font family and size.
    *
    * @param <T>       the component type
    * @param component the component
    * @param style     the new font style
    * @return the component
    */
   public static <T extends Component> T setFontStyle(@NonNull T component, int style) {
      component.setFont(component.getFont().deriveFont(style));
      return component;
   }

   /**
    * Set the maximum font size (i.e. the given size or current size whichever is smaller) for the given component
    * keeping the current font family and style.
    *
    * @param <T>       the component type
    * @param component the component
    * @param maxSize   the maximum size the font is allowed to be
    * @return the component
    */
   public static <T extends Component> T setMaxFontSize(@NonNull T component, float maxSize) {
      component.setFont(component.getFont().deriveFont(Math.min(maxSize, component.getFont().getSize())));
      return component;
   }

   /**
    * Set the minimum font size (i.e. the given size or current size whichever is larger) for the given component
    * keeping the current font family and style.
    *
    * @param <T>       the component type
    * @param component the component
    * @param minSize   the minimum size the font is allowed to be
    * @return the component
    */
   public static <T extends Component> T setMinFontSize(@NonNull T component, float minSize) {
      component.setFont(component.getFont().deriveFont(Math.max(minSize, component.getFont().getSize())));
      return component;
   }

}//END OF Fonts
