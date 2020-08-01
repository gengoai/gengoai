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

import com.gengoai.string.Strings;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.MangoButtonGroup;
import com.gengoai.swing.TextAlignment;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.function.Consumer;

/**
 * The type Components.
 */
public final class Components {

   private Components() {
      throw new IllegalAccessError();
   }

   /**
    * Create a button from the given action.
    *
    * @param action       the action
    * @param includeText  True include the action name as the button's text
    * @param largeIcon    True include the large icon, False include the small icon
    * @param textPosition Where to place the text relative to the icon
    * @return the button
    */
   public static JButton button(@NonNull FluentAction action,
                                boolean includeText,
                                boolean largeIcon,
                                int textPosition) {
      return buttonInit(new JButton(action), action, includeText, largeIcon, textPosition);
   }

   /**
    * Creates a button group containing the given buttons
    *
    * @param buttons the buttons
    * @return the button group
    */
   public static MangoButtonGroup buttonGroup(@NonNull AbstractButton... buttons) {
      MangoButtonGroup buttonGroup = new MangoButtonGroup();
      for(AbstractButton button : buttons) {
         buttonGroup.add(button);
      }
      return buttonGroup;
   }

   private static <T extends AbstractButton> T buttonInit(T btn,
                                                          FluentAction action,
                                                          boolean includeText,
                                                          boolean largeIcon,
                                                          int textPosition) {
      if(!includeText) {
         btn.setText(Strings.EMPTY);
      }
      if(!largeIcon) {
         btn.setIcon(action.getSmallIcon());
      }
      TextAlignment horizontal = TextAlignment.HorizontalCenter;
      TextAlignment vertical = TextAlignment.VerticalCenter;
      switch(textPosition) {
         case SwingConstants.TOP:
            vertical = TextAlignment.VerticalTop;
            break;
         case SwingConstants.LEFT:
            horizontal = TextAlignment.HorizontalLeft;
            break;
         case SwingConstants.LEADING:
            horizontal = TextAlignment.HorizontalLeading;
            break;
         case SwingConstants.TRAILING:
            horizontal = TextAlignment.HorizontalTrailing;
            break;
         case SwingConstants.RIGHT:
            horizontal = TextAlignment.HorizontalRight;
            break;
         default:
            vertical = TextAlignment.VerticalBottom;
            break;
      }
      horizontal.set(btn);
      vertical.set(btn);
      return btn;
   }

   /**
    * Create a button from the given action using the large icon if defined.
    *
    * @param action       the action
    * @param textPosition Where to place the text relative to the icon
    * @return the button
    */
   public static JButton buttonLargeIconWithText(@NonNull FluentAction action, int textPosition) {
      return button(action, true, true, textPosition);
   }

   /**
    * Create a button from the given action using only the large icon.
    *
    * @param action the action
    * @return the button
    */
   public static JButton buttonLargeIconWithoutText(@NonNull FluentAction action) {
      return button(action, false, true, -1);
   }

   /**
    * Create a button from the given action using the small icon if defined.
    *
    * @param action       the action
    * @param textPosition Where to place the text relative to the icon
    * @return the button
    */
   public static JButton buttonSmallIconWithText(@NonNull FluentAction action, int textPosition) {
      return button(action, true, false, textPosition);
   }

   /**
    * Create a button from the given action using only the small icon.
    *
    * @param action the action
    * @return the button
    */
   public static JButton buttonSmallIconWithoutText(@NonNull FluentAction action) {
      return button(action, false, false, -1);
   }

   /**
    * Creates a dimension for the given width and height
    *
    * @param width  the width
    * @param height the height
    * @return the dimension
    */
   public static Dimension dim(int width, int height) {
      return new Dimension(width, height);
   }

   /**
    * Creates a new label and updates its font style and size to the given values.
    *
    * @param text  the text of the label
    * @param style the font style
    * @param size  the font size
    * @return the label
    */
   public static JLabel label(String text, int style, float size) {
      return Fonts.setFontStyle(Fonts.setFontSize(new JLabel(text), size), style);
   }

   /**
    * Creates a new JPanel with a BorderLayout and applies the given consumer to further define its properties.
    *
    * @param consumer the consumer to use to style the panel
    * @return the panel
    */
   public static JPanel panel(@NonNull Consumer<JPanel> consumer) {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      consumer.accept(panel);
      return panel;
   }

   /**
    * Creates a new JPanel with a BorderLayout and applies the given consumer to further define its properties.
    *
    * @param components the tuples of (position, component)
    * @return the panel
    */
   @SafeVarargs
   public static JPanel panel(@NonNull Tuple2<String, Component>... components) {
      JPanel panel = new JPanel(new BorderLayout());
      for(Tuple2<String, Component> component : components) {
         panel.add(component.v2, component.v1);
      }
      return panel;
   }

   /**
    * Creates a horizontally aligned panel (HBox) and applies the given consumer to further define its properties.
    *
    * @param consumer the consumer to use to style the panel
    * @return the panel
    */
   public static HBox panelHBox(@NonNull Consumer<HBox> consumer) {
      return panelHBox(0, 0, consumer);
   }

   /**
    * Creates a horizontally aligned panel (HBox) and applies the given consumer to further define its properties.
    *
    * @param gap      the amount of space between components
    * @param consumer the consumer to use to style the panel
    * @return the panel
    */
   public static HBox panelHBox(int gap, int componentPadding, @NonNull Consumer<HBox> consumer) {
      HBox hbox = new HBox(gap, componentPadding);
      consumer.accept(hbox);
      return hbox;
   }

   /**
    * Creates a horizontally aligned panel (HBox) containing the given components
    *
    * @param components the components
    * @return the HBbox
    */
   public static HBox panelHBox(@NonNull Component... components) {
      return panelHBox(0, 0, components);
   }

   /**
    * Creates a horizontally aligned panel (HBox) containing the given components
    *
    * @param gap              the amount of space between components
    * @param componentPadding the amount of horizontal padding to add to the components
    * @param components       the components
    * @return the HBbox
    */
   public static HBox panelHBox(int gap, int componentPadding, @NonNull Component... components) {
      HBox hBox = new HBox(gap, componentPadding);
      for(Component component : components) {
         hBox.add(component);
      }
      return hBox;
   }

   /**
    * Creates a vertically aligned panel (VBox) containing the given components
    *
    * @param components the components
    * @return the VBox
    */
   public static VBox panelVBox(@NonNull Component... components) {
      return panelVBox(0, 0, components);
   }

   /**
    * Creates a vertically aligned panel (VBox) containing the given components
    *
    * @param gap              the amount of space between components
    * @param componentPadding the amount of vertical padding to add to the components
    * @param components       the components
    * @return the VBox
    */
   public static VBox panelVBox(int gap, int componentPadding, @NonNull Component... components) {
      VBox vbox = new VBox(gap, componentPadding);
      for(Component component : components) {
         vbox.add(component);
      }
      return vbox;
   }

   /**
    * Creates a vertically aligned panel (VBox) and applies the given consumer to further define its properties.
    *
    * @param consumer the consumer to use to style the panel
    * @return the panel
    */
   public static VBox panelVBox(@NonNull Consumer<VBox> consumer) {
      return panelVBox(0, 0, consumer);
   }

   /**
    * Creates a vertically aligned panel (VBox) and applies the given consumer to further define its properties.
    *
    * @param gap              the amount of space between components
    * @param componentPadding the amount of vertical padding to add to the components
    * @param consumer         the consumer to use to style the panel
    * @return the panel
    */
   public static VBox panelVBox(int gap, int componentPadding, @NonNull Consumer<VBox> consumer) {
      VBox vBox = new VBox(gap, componentPadding);
      consumer.accept(vBox);
      return vBox;
   }

   /**
    * Creates a new MangoPanel with a BorderLayout and applies the given consumer to further define its properties.
    *
    * @param components the tuples of (position, component)
    * @return the panel
    */
   @SafeVarargs
   public static JPanel panelViewAwareBorderLayout(@NonNull Tuple2<String, Component>... components) {
      JPanel panel = new MangoPanel();
      for(Tuple2<String, Component> component : components) {
         panel.add(component.v2, component.v1);
      }
      return panel;
   }

   /**
    * Creates a new JScrollPane that has an empty border with the given component in its viewport.
    *
    * @param component the component
    * @return the JScrollPane
    */
   public static JScrollPane scrollPaneNoBorder(Component component) {
      JScrollPane scrollPane = new JScrollPane(component);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      return scrollPane;
   }

   private static JSplitPane splitPane(int orientation, int resized, @NonNull JComponent... components) {
      JSplitPane pane = new JSplitPane(orientation);
      if(components.length == 0) {
         return pane;
      }
      pane.setLeftComponent(components[0]);
      pane.setResizeWeight(resized == 0
                           ? 1
                           : 0);
      for(int i = 1; i < components.length; i++) {
         pane.setRightComponent(components[i]);
         if(resized == i) {
            pane.setResizeWeight(0);
         }
         if(i + 1 < components.length) {
            pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pane, null);
            pane.setResizeWeight(resized == i
                                 ? 1
                                 : 0);
         }
      }
      return pane;
   }

   public static JSplitPane splitPaneHorizontal(int resized, @NonNull JComponent... components) {
      return splitPane(JSplitPane.HORIZONTAL_SPLIT, resized, components);
   }

   public static JSplitPane splitPaneVertical(int resized, @NonNull JComponent... components) {
      return splitPane(JSplitPane.VERTICAL_SPLIT, resized, components);
   }

   /**
    * Create a button from the given action.
    *
    * @param action       the action
    * @param includeText  True include the action name as the button's text
    * @param largeIcon    True include the large icon, False include the small icon
    * @param textPosition Where to place the text relative to the icon
    * @return the button
    */
   public static JToggleButton toggleButton(@NonNull FluentAction action,
                                            boolean includeText,
                                            boolean largeIcon,
                                            int textPosition) {
      return buttonInit(new JToggleButton(action), action, includeText, largeIcon, textPosition);
   }

   /**
    * Create a button from the given action using the large icon if defined.
    *
    * @param action       the action
    * @param textPosition Where to place the text relative to the icon
    * @return the button
    */
   public static JToggleButton toggleButtonLargeIconWithText(@NonNull FluentAction action, int textPosition) {
      return toggleButton(action, true, true, textPosition);
   }

   /**
    * Create a button from the given action using only the large icon.
    *
    * @param action the action
    * @return the button
    */
   public static JToggleButton toggleButtonLargeIconWithoutText(@NonNull FluentAction action) {
      return toggleButton(action, false, true, -1);
   }

   /**
    * Create a button from the given action using the small icon if defined.
    *
    * @param action       the action
    * @param textPosition Where to place the text relative to the icon
    * @return the button
    */
   public static JToggleButton toggleButtonSmallIconWithText(@NonNull FluentAction action, int textPosition) {
      return toggleButton(action, true, false, textPosition);
   }

   /**
    * Create a button from the given action using only the small icon.
    *
    * @param action the action
    * @return the button
    */
   public static JToggleButton toggleButtonSmallIconWithoutText(@NonNull FluentAction action) {
      return toggleButton(action, false, false, -1);
   }

}//END OF Components
