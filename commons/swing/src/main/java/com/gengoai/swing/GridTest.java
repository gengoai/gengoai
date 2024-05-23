package com.gengoai.swing;

import javax.swing.*;
import java.awt.*;

public class GridTest extends SwingApplication {

   public static class VBox extends JPanel {
      private final GridLayout layout = new GridLayout(0, 1);

      public VBox() {
         setLayout(layout);
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

//      @Override
//      public void remove(Component comp) {
//         Component[] components = getComponents();
//         for (int i = 0; i < components.length; i++) {
//            if (components[i] == comp) {
//               remove(i);
//               break;
//            }
//         }
//      }
   }

   @Override
   public void initControls() throws Exception {
      var panel = new VBox();
      for (int i = 0; i < 10; i++) {
         panel.add("A",new JButton("Label " + i));
      }
      panel.setGap(5);
      panel.remove(panel.getComponent(3));
      mainWindowFrame.add(panel, BorderLayout.NORTH);
      setCenterComponent(new JTree());

      var bottomPanel = new VBox();
      bottomPanel.add(new JButton("Button 1"));
      mainWindowFrame.add(bottomPanel, BorderLayout.SOUTH);


      pack();
   }

   public static void main(String[] args) {
      SwingApplication.runApplication(GridTest::new,
                                      "GA", "GA", args);
   }
}
