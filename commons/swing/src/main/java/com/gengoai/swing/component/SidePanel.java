package com.gengoai.swing.component;


import com.gengoai.swing.View;

import javax.swing.*;
import java.awt.*;

public class SidePanel implements View {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel label = new JLabel("Side Panel");
    private final JToolBar toolBar = new JToolBar();
    private final JComponent contentPane;
    public final JButton button;


    public SidePanel(JComponent contentPane, JButton button) {
        this("Side Panel", contentPane, button);
    }

    public SidePanel(String panelName, JComponent contentPane, JButton button) {
        this.contentPane = contentPane;
        this.button = button;
        label.setText(panelName);
        label.setFont(label.getFont().deriveFont(18f));

        var header = new JPanel(new BorderLayout());
        header.add(label, BorderLayout.CENTER);
        toolBar.setFloatable(false);
        header.add(toolBar, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(button, BorderLayout.SOUTH);
        panel.add(contentPane, BorderLayout.CENTER);
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    public void addToolbarButton(JButton button) {
        toolBar.add(button);
    }

    public void addToolbarSeparator() {
        toolBar.addSeparator();
    }

    @Override
    public JComponent getRoot() {
        return panel;
    }

}//END OF SidePanel
