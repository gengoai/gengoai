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

package com.gengoai.swing;

import com.gengoai.application.Application;
import com.gengoai.conversion.Cast;
import com.gengoai.io.Resources;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * <p> Abstract base class for a swing based applications. Child classes should define their UI via the {@link
 * #setup()} method and should define a <code>main</code> method that calls {@link #run(String[])}. An example
 * application is listed below.</p>
 * <pre>
 * {@code
 *    public class MyApplication extends SwingApplication {
 *
 *      public static void main(String[] args)  {
 *        runApplication(MyApplication::new, args)
 *      }
 *
 *      public void initControls() throws Exception {
 *        //GUI setup goes here.
 *      }
 *
 *    }
 * }
 * </pre>
 *
 * @author David B. Bracewell
 */
public abstract class SwingApplication extends Application {
   private static final long serialVersionUID = 1L;
   public static final JComponent SEPARATOR = null;
   public final JFrame mainWindowFrame;
   private final JPanel southPanel = new JPanel(new BorderLayout());
   protected SwingApplicationConfig properties;

   private static JToolBar createToolBar(@NonNull Object... components) {
      JToolBar toolBar = new JToolBar();
      toolBar.setFloatable(false);
      for(Object component : components) {
         if(component == null) {
            toolBar.addSeparator();
         } else if(component instanceof Dimension) {
            toolBar.addSeparator(Cast.as(component));
         } else if(component instanceof View) {
            toolBar.add(Cast.<View>as(component).getRoot());
         } else {
            toolBar.add(Cast.<Component>as(component));
         }
      }
      return toolBar;
   }

   public static void runApplication(Supplier<? extends SwingApplication> supplier,
                                     String applicationName,
                                     String[] args) {
      SwingUtilities.invokeLater(() -> {
         SwingApplicationConfig config = new SwingApplicationConfig();
         try {
            config.load(Resources.from(applicationName + ".properties"));
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
         final String lookAndFeel = config.get("lookAndFeel").asString("light");
         try {
            switch(lookAndFeel.toLowerCase()) {
               case "dark":
                  UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
                  break;
               case "light":
                  UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                  break;
               case "darcula":
                  UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf");
                  break;
               case "intellij":
                  UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
                  break;
               default:
                  UIManager.setLookAndFeel(lookAndFeel);
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
         SwingApplication swingApplication = supplier.get();
         swingApplication.properties = config;
         swingApplication.run(args);
      });
   }

   /**
    * Instantiates a new Application.
    */
   protected SwingApplication() {
      this(null);
   }

   /**
    * Instantiates a new SwingApplication.
    *
    * @param name The name of the application
    */
   protected SwingApplication(String name) {
      super(name);
      this.mainWindowFrame = new JFrame();
   }

   public JFrame getFrame() {
      return mainWindowFrame;
   }

   public Point getScreenLocation() {
      Point location = mainWindowFrame.getLocation();
      SwingUtilities.convertPointToScreen(location, mainWindowFrame);
      return location;
   }

   protected abstract void initControls() throws Exception;

   public void invalidate() {
      mainWindowFrame.invalidate();
   }

   protected JMenuBar menuBar(@NonNull JMenu... menus) {
      JMenuBar menuBar = new JMenuBar();
      for(JMenu menu : menus) {
         menuBar.add(menu);
      }
      mainWindowFrame.setJMenuBar(menuBar);
      return menuBar;
   }

   protected void onClose() throws Exception {
      if((mainWindowFrame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
         properties.set("window.maximized", "true");
      } else {
         properties.set("window.maximized", "false");
         properties.set("window.width", Integer.toString(mainWindowFrame.getWidth()));
         properties.set("window.height", Integer.toString(mainWindowFrame.getHeight()));
         properties.set("window.position.x", Integer.toString(mainWindowFrame.getLocation().x));
         properties.set("window.position.y", Integer.toString(mainWindowFrame.getLocation().y));
      }
   }

   public void pack() {
      mainWindowFrame.pack();
   }

   @Override
   public final void run() {
      mainWindowFrame.setVisible(true);
   }

   public void setCenterComponent(Component component) {
      if(component instanceof View) {
         mainWindowFrame.add(((View) component).getRoot(), BorderLayout.CENTER);
      } else {
         mainWindowFrame.add(component, BorderLayout.CENTER);
      }
   }

   public void setEastComponent(Component component) {
      if(component instanceof View) {
         mainWindowFrame.add(((View) component).getRoot(), BorderLayout.EAST);
      } else {
         mainWindowFrame.add(component, BorderLayout.EAST);
      }
   }

   public void setIconImage(Image icon) {
      mainWindowFrame.setIconImage(icon);
   }

   public void setMaximumSize(Dimension dimension) {
      mainWindowFrame.setMaximumSize(dimension);
   }

   public void setMinimumSize(Dimension dimension) {
      mainWindowFrame.setMinimumSize(dimension);
   }

   public void setPreferredSize(Dimension dimension) {
      mainWindowFrame.setPreferredSize(dimension);
   }

   public void setSouthComponent(Component component) {
      if(component instanceof View) {
         southPanel.add(((View) component).getRoot(), BorderLayout.CENTER);
      } else {
         southPanel.add(component, BorderLayout.CENTER);
      }
   }

   public void setTitle(String title) {
      mainWindowFrame.setTitle(title);
   }

   public void setWestComponent(Component component) {
      if(component instanceof View) {
         mainWindowFrame.add(((View) component).getRoot(), BorderLayout.WEST);
      } else {
         mainWindowFrame.add(component, BorderLayout.WEST);
      }
   }

   @Override
   public final void setup() throws Exception {
      int width = properties.get("window.width").asIntegerValue(800);
      int height = properties.get("window.height").asIntegerValue(600);
      mainWindowFrame.setMinimumSize(new Dimension(800, 600));
      mainWindowFrame.setSize(new Dimension(width, height));

      if(properties.get("window.maximized").asBooleanValue(false)) {
         mainWindowFrame.setExtendedState(mainWindowFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
      } else {
         Rectangle screenRectangle = mainWindowFrame.getGraphicsConfiguration()
                                                    .getDevice()
                                                    .getDefaultConfiguration()
                                                    .getBounds();
         int xPos = properties.get("window.position.x")
                              .asIntegerValue(screenRectangle.width / 2 - width / 2);
         int yPos = properties.get("window.position.y")
                              .asIntegerValue(screenRectangle.height / 2 - height / 2);
         mainWindowFrame.setLocation(xPos, yPos);
      }

      mainWindowFrame.setTitle(getName());
      mainWindowFrame.setLayout(new BorderLayout());
      mainWindowFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      mainWindowFrame.add(southPanel, BorderLayout.SOUTH);

      mainWindowFrame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            try {
               onClose();
               properties.save();
            } catch(Exception ex) {
               ex.printStackTrace();
            }
         }
      });
      southPanel.setVisible(false);
      initControls();
   }

   protected JToolBar statusBar(@NonNull Object... components) {
      JToolBar toolBar = createToolBar(components);
      toolBar.setBorderPainted(true);
      toolBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("MenuBar.borderColor")),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      southPanel.setVisible(true);
      southPanel.add(toolBar, BorderLayout.SOUTH);
      return toolBar;
   }

   protected JToolBar toolBar(@NonNull Object... components) {
      JToolBar toolBar = createToolBar(components);
      toolBar.setBorderPainted(true);
      toolBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("MenuBar.borderColor")),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      mainWindowFrame.add(toolBar, BorderLayout.NORTH);
      return toolBar;
   }

}// END OF SwingApplication
