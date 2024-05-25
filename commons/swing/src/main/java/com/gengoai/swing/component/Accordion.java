package com.gengoai.swing.component;

import com.gengoai.swing.Colors;
import com.gengoai.swing.FlatLafColors;
import com.gengoai.swing.Fonts;
import com.gengoai.swing.TitleBoxBorder;
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
   private java.util.List<AccordionItem> items = new ArrayList<>();

   private class AccordionItem {
      private final JLabel title;
      private final JComponent component;

      public AccordionItem(String title, JComponent component) {
         this.title = new JLabel(title);
         Fonts.adjustFont(this.title, Font.BOLD, 14);
         this.title.setHorizontalAlignment(SwingConstants.LEFT);
         Border compoundBorder = new CompoundBorder(
               new LineBorder(FlatLafColors.Button_borderColor.color(), 1),
               new EmptyBorder(5, 5, 5, 5)
         );
         this.title.setBorder(compoundBorder);
         this.title.setForeground(FlatLafColors.Button_foreground.color());
         this.title.setBackground(FlatLafColors.Button_background.color());
         this.title.setOpaque(true);
         this.title.addMouseListener(SwingListeners.mouseClicked(e -> {
            int index = items.indexOf(this);
            setActiveItem(index);
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
            Color background = FlatLafColors.Button_default_focusColor.color();
            item.title.setBackground(background);
            item.title.setForeground(Colors.calculateBestFontColor(background));
            item.title.setBorder(new CompoundBorder(
                  new LineBorder(background, 1),
                  new EmptyBorder(5, 5, 5, 5)
            ));
            add(item.component, BorderLayout.CENTER);
         } else {
            Color background = FlatLafColors.Button_background.color();
            item.title.setBackground(background);
            item.title.setForeground(Colors.calculateBestFontColor(background));
            item.title.setBorder(new CompoundBorder(
                  new LineBorder(FlatLafColors.Button_borderColor.color(), 1),
                  new EmptyBorder(5, 5, 5, 5)
            ));
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
