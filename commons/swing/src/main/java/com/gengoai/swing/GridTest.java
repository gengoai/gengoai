package com.gengoai.swing;

import com.gengoai.swing.component.HGridBox;

import javax.swing.*;

public class GridTest extends SwingApplication {


   @Override
   public void initControls() throws Exception {
      HGridBox hGridBox = new HGridBox(10);
      hGridBox.add(new JButton("Button 1"));
      hGridBox.add(new JButton("Button 2"));
      hGridBox.add(new JButton("Button 3"));
      hGridBox.add(new JButton("Button 4"));
      hGridBox.add(new JButton("Button 5"));

      mainWindowFrame.add(hGridBox, "North");
      pack();
   }

   public static void main(String[] args) {
      SwingApplication.runApplication(GridTest::new,
                                      "GA", "GA", args);
   }
}
