package com.gengoai.news.gui;

import com.gengoai.swing.Menus;
import com.gengoai.swing.SwingApplication;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Test extends SwingApplication {
   @Override
   public void initControls() throws Exception {
      CrawlerGUIControls controls = new CrawlerGUIControls(this.mainWindowFrame);
      CrawlerGUIController controller = new CrawlerGUIController(controls);
      menuBar(Menus.menu("File",
                         'F',
                         controller.newProject));

      mainWindowFrame.addWindowListener(new WindowListener() {
         @Override
         public void windowOpened(WindowEvent e) {

         }

         @Override
         public void windowClosing(WindowEvent e) {
            if (controller.timer == null) {
               mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               return;
            }
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit?", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
               mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            } else {
               mainWindowFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
         }

         @Override
         public void windowClosed(WindowEvent e) {

         }

         @Override
         public void windowIconified(WindowEvent e) {

         }

         @Override
         public void windowDeiconified(WindowEvent e) {

         }

         @Override
         public void windowActivated(WindowEvent e) {

         }

         @Override
         public void windowDeactivated(WindowEvent e) {

         }
      });
      setResizable(false);
   }

   public static void main(String[] args) {
      SwingApplication.runApplication(Test::new,
                                      "Test",
                                      "Test",
                                      args);
   }
}
