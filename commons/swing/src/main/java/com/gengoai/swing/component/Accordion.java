package com.gengoai.swing.component;

import com.gengoai.swing.*;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

public class Accordion extends JPanel {
   private final VGridBox northPanel = new VGridBox();
   private final VGridBox southPanel = new VGridBox();
   private int activeItem = 0;
   private final java.util.List<AccordionItem> items = new ArrayList<>();

   private static final ComponentStyle<JLabel> defaultTitleStyle = ComponentStyle.defineStyle($ -> {
      TextAlignment.HorizontalLeft.set($);
      Border compoundBorder = new CompoundBorder(
            new LineBorder(FlatLafColors.Button_borderColor.color(), 1),
            new EmptyBorder(5, 5, 5, 5)
      );
      $.setBorder(compoundBorder);
      $.setForeground(FlatLafColors.Button_foreground.color());
      $.setBackground(FlatLafColors.Button_background.color());
      $.setOpaque(true);
   });

   private static final ComponentStyle<JLabel> focusedTitleStyle = ComponentStyle.defineStyle($ -> {
      TextAlignment.HorizontalLeft.set($);
      Color background = FlatLafColors.Button_default_focusColor.color();
      $.setBackground(background);
      $.setForeground(Colors.calculateBestFontColor(background));
      $.setBorder(new CompoundBorder(
            new LineBorder(background, 1),
            new EmptyBorder(5, 5, 5, 5)
      ));
      $.setOpaque(true);
   });

   private class AccordionItem {
      private final JLabel title;
      private final JComponent component;

      public AccordionItem(String title, JComponent component) {
         this.title = new JLabel(title);
         defaultTitleStyle.style(this.title);
         this.title.addMouseListener(SwingListeners.mouseClicked(e -> {
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
      var item = new AccordionItem(title, component);
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
            northPanel.add(item.title);
         } else {
            southPanel.add(item.title);
         }
         if (i == activeItem) {
            focusedTitleStyle.style(item.title);
            add(item.component, BorderLayout.CENTER);
         } else {
            defaultTitleStyle.style(item.title);
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
