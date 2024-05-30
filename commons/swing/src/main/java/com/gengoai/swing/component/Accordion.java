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

   private static final ComponentStyle<JLabel> defaultTitleStyle = ComponentStyle.defineStyle($ -> {
      TextAlignment.HorizontalLeft.set($);
      $.setFont($.getFont().deriveFont(Font.BOLD));
      $.setBorder(new EmptyBorder(5, 5, 5, 5));
      $.setForeground(FlatLafColors.Button_foreground.color());
      $.setBackground(FlatLafColors.Button_background.color());
      $.setOpaque(true);
   });

   private static final ComponentStyle<JPanel> defaultTitlePanelStyle = ComponentStyle.defineStyle($ -> {
      Border compoundBorder = new CompoundBorder(
            new LineBorder(FlatLafColors.Button_borderColor.color(), 1),
            new EmptyBorder(5, 5, 5, 5)
      );
      $.setFont($.getFont().deriveFont(Font.BOLD));
      $.setBorder(compoundBorder);
      $.setForeground(FlatLafColors.Button_foreground.color());
      $.setBackground(FlatLafColors.Button_background.color());
      $.setOpaque(true);
   });

   private static final ComponentStyle<JLabel> focusedTitleStyle = ComponentStyle.defineStyle($ -> {
      TextAlignment.HorizontalLeft.set($);
      Color background = FlatLafColors.Button_default_focusColor.color();
      $.setFont($.getFont().deriveFont(Font.BOLD));
      $.setBackground(background);
      $.setForeground(Colors.calculateBestFontColor(background));
      $.setOpaque(true);
   });

   private static final ComponentStyle<JPanel> focusedTitlePanelStyle = ComponentStyle.defineStyle($ -> {
      Color background = FlatLafColors.Button_default_focusColor.color();
      $.setFont($.getFont().deriveFont(Font.BOLD));
      $.setBackground(background);
      $.setForeground(Colors.calculateBestFontColor(background));
      $.setBorder(new CompoundBorder(
            new LineBorder(background, 1),
            new EmptyBorder(5, 5, 5, 5)
      ));
      $.setOpaque(true);
   });

   private class AccordionItem {
      private final JPanel titlePanel;
      private final JLabel titleLabel;
      private final JComponent component;

      public AccordionItem(String title, Icon icon, JComponent component) {
         this.titleLabel = new JLabel(title);
         this.titlePanel = panel($ -> {
            if(icon != null) {
               var iconLabel = new JLabel(icon);
               iconLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
               $.add(iconLabel, BorderLayout.WEST);
            }
            $.add(titleLabel, BorderLayout.CENTER);
            defaultTitleStyle.style(titleLabel);
         });

//         this.title = new JLabel(title);
//         if (icon != null) {
//            this.title.setIcon(icon);
//         }
         defaultTitlePanelStyle.style(this.titlePanel);
         this.titlePanel.addMouseListener(SwingListeners.mouseClicked(e -> {
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
            northPanel.add(item.titlePanel);
         } else {
            southPanel.add(item.titlePanel);
         }
         if (i == activeItem) {
            focusedTitleStyle.style(item.titleLabel);
            focusedTitlePanelStyle.style(item.titlePanel);
            add(item.component, BorderLayout.CENTER);
         } else {
            defaultTitleStyle.style(item.titleLabel);
            defaultTitlePanelStyle.style(item.titlePanel);
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
