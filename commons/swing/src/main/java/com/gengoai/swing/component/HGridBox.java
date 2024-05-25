package com.gengoai.swing.component;

import javax.swing.*;
import java.awt.*;

public class HGridBox extends JPanel {
   private final GridLayout layout = new GridLayout(1, 0);

   public HGridBox() {
      super.setLayout(layout);
   }

   public HGridBox(int gap) {
      this();
      setGap(gap);
   }

   @Override
   public void setLayout(LayoutManager mgr) {
      // Do nothing
   }

   public void setGap(int gap) {
      layout.setHgap(gap);
   }

   @Override
   public Component add(Component component) {
      layout.setColumns(layout.getColumns() + 1);
      return super.add(component);
   }

   @Override
   public void remove(int index) {
      super.remove(index);
      layout.setColumns(layout.getColumns() - 1);
   }

   @Override
   public void removeAll() {
      super.removeAll();
      layout.setColumns(0);
   }
}
