package com.gengoai.swing.component;

import com.gengoai.swing.Colors;
import com.gengoai.swing.ComponentStyle;
import com.gengoai.swing.FlatLafColors;
import com.gengoai.swing.TextAlignment;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

import static com.gengoai.swing.component.Components.panel;

public class Accordion extends JPanel {
   private final VGridBox northPanel = new VGridBox();
   private final VGridBox southPanel = new VGridBox();
   private int activeItem = 0;
   private final java.util.List<AccordionItem> items = new ArrayList<>();

   private static final Border defaultBorder = BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(FlatLafColors.Button_borderColor.color(), 1),
         BorderFactory.createEmptyBorder(5, 5, 5, 5));

   private static final Border focusedBorder = BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(FlatLafColors.Button_default_focusColor.color(), 1),
         BorderFactory.createEmptyBorder(5, 5, 5, 5));


   private static final ComponentStyle<JLabel> defaultTitleStyle = ComponentStyle.defineStyle($ -> {
      $.setFont($.getFont().deriveFont(Font.BOLD));
      $.setBorder(defaultBorder);
      $.setForeground(FlatLafColors.Button_foreground.color());
      $.setBackground(FlatLafColors.Button_background.color());
      $.setOpaque(true);
   });


   private static final ComponentStyle<JLabel> focusedTitleStyle = ComponentStyle.defineStyle($ -> {
      Color background = FlatLafColors.Button_default_focusColor.color();
      $.setBorder(focusedBorder);
      $.setBackground(background);
      $.setForeground(Colors.calculateBestFontColor(background));
   });


   private class AccordionItem {
      private final JLabel titleLabel;
      private final JComponent component;

      public AccordionItem(String title, Icon icon, JComponent component) {
         this.titleLabel = new JLabel(title);
         if( icon != null) {
            this.titleLabel.setIcon(icon);
            this.titleLabel.setIconTextGap(5);
         }
         this.titleLabel.addMouseListener(SwingListeners.mouseClicked(e -> {
            if (e.getButton() == 1) {
               int index = items.indexOf(this);
               setActiveItem(index);
            }
         }));
         this.component = component;
         this.component.setBorder(new LineBorder(FlatLafColors.Button_default_focusColor.color(), 2));
      }
   }

   public Accordion() {
      super.setLayout(new BorderLayout());
   }

   public void addItem(String title, JComponent component) {
      addItem(title, null, component);
   }

   public void addItem(String title, Icon icon, JComponent component) {
      var item = new AccordionItem(title, icon, component);
      items.add(item);
      render();
   }

   public void setActiveItem(int index) {
      if (index < 0 || index >= items.size()) {
         return;
      }
      activeItem = index;
      render();
   }

   private void render() {
      southPanel.removeAll();
      northPanel.removeAll();
      removeAll();
      add(northPanel, BorderLayout.NORTH);
      add(southPanel, BorderLayout.SOUTH);
      for (int i = 0; i < items.size(); i++) {
         var item = items.get(i);
         if (i <= activeItem) {
            northPanel.add(item.titleLabel);
         } else {
            southPanel.add(item.titleLabel);
         }
         if (i == activeItem) {
            focusedTitleStyle.style(item.titleLabel);
            add(item.component, BorderLayout.CENTER);
         } else {
            defaultTitleStyle.style(item.titleLabel);
         }
      }
      repaint();
      revalidate();
   }

   @Override
   public void setLayout(LayoutManager mgr) {
      // Do nothing
   }


}
