package com.gengoai.swing;

import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MangoFrame extends JFrame {
    private final JPanel southPanel = new JPanel(new BorderLayout());

    public MangoFrame(String title) {
        super(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                onWindowOpen();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                super.windowIconified(e);
                onWindowIconfied();
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                super.windowDeiconified(e);
                onWindowDeiconfied();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                super.windowGainedFocus(e);
                onWindowGainedFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                super.windowLostFocus(e);
                onWindowLostFocus();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                onWindowClosing();
            }
        });
        southPanel.setVisible(false);
        add(southPanel, BorderLayout.SOUTH);
    }

    public void centerFrame() {
        Dimension screenRectangle = getToolkit().getScreenSize();
        Dimension frameSize = getSize();
        int xPos = screenRectangle.width / 2 - frameSize.width / 2;
        int yPos = screenRectangle.height / 2 - frameSize.height / 2;
        setLocation(xPos, yPos);
    }

    public void onWindowOpen() {
        //Do nothing
    }

    public void onWindowLostFocus() {
        //Do nothing
    }

    public void onWindowIconfied() {
        //Do nothing
    }

    public void onWindowDeiconfied() {
        //Do nothing
    }

    public void onWindowGainedFocus() {
        //Do nothing
    }

    public void onWindowClosing() {
        //Do nothing
    }

    public JMenuBar menuBar(@NonNull JMenu... menus) {
        JMenuBar menuBar = new JMenuBar();
        for (JMenu menu : menus) {
            menuBar.add(menu);
        }
        setJMenuBar(menuBar);
        return menuBar;
    }

    protected JToolBar toolBar(@NonNull Object... components) {
        JToolBar toolBar = Toolbars.createToolBar(components);
        toolBar.setBorderPainted(true);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("MenuBar.borderColor")),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        add(toolBar, BorderLayout.NORTH);
        return toolBar;
    }

    public JToolBar statusBar(@NonNull Object... components) {
        JToolBar toolBar = Toolbars.createToolBar(components);
        toolBar.setBorderPainted(true);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("MenuBar.borderColor")),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        southPanel.setVisible(true);
        southPanel.add(toolBar, BorderLayout.SOUTH);
        return toolBar;
    }

    public void setSouthComponent(Component component) {
        southPanel.setVisible(true);
        if (component instanceof View) {
            southPanel.add(((View) component).getRoot(), BorderLayout.CENTER);
        } else {
            southPanel.add(component, BorderLayout.CENTER);
        }
    }

    public void setWestComponent(Component component) {
        if (component instanceof View) {
            add(((View) component).getRoot(), BorderLayout.WEST);
        } else {
            add(component, BorderLayout.WEST);
        }
    }

    public void setNorthComponent(Component component) {
        if (component instanceof View) {
            add(((View) component).getRoot(), BorderLayout.NORTH);
        } else {
            add(component, BorderLayout.NORTH);
        }
    }

    public void setCenterComponent(Component component) {
        if (component instanceof View) {
            add(((View) component).getRoot(), BorderLayout.CENTER);
        } else {
            add(component, BorderLayout.CENTER);
        }
    }

    public void setEastComponent(Component component) {
        if (component instanceof View) {
            add(((View) component).getRoot(), BorderLayout.EAST);
        } else {
            add(component, BorderLayout.EAST);
        }
    }

}//END OF MangoFrame
