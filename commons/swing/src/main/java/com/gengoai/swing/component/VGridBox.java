package com.gengoai.swing.component;

import javax.swing.*;
import java.awt.*;

public class VGridBox extends JPanel {
   private final GridLayout layout = new GridLayout(0, 1);

   public VGridBox() {
      super.setLayout(layout);
   }

   public VGridBox(int gap) {
      this();
      setGap(gap);
   }

   @Override
   public void setLayout(LayoutManager mgr) {
      // Do nothing
   }

   public void setGap(int gap) {
      layout.setVgap(gap);
   }

   @Override
   public Component add(Component component) {
      layout.setRows(layout.getRows() + 1);
      return super.add(component);
   }

   @Override
   public void remove(int index) {
      super.remove(index);
      layout.setRows(layout.getRows() - 1);
   }


}
