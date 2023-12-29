package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.swing.component.listener.FluentAction;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AnalyzeTextDialog extends JDialog {
    private final Consumer<String> onAnalyze;
    private final JButton btnAnalyze = new JButton("Analyze");
    private final JTextArea txtArea = new JTextArea();

    public AnalyzeTextDialog(Consumer<String> onAnalyze) {
        super();
        this.onAnalyze = onAnalyze;
        setMinimumSize(new Dimension(800, 600));
        setLayout(new BorderLayout());

        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem paste = new JMenuItem(new FluentAction("Paste", e -> txtArea.paste()));
        contextMenu.add(paste);

        txtArea.setComponentPopupMenu(contextMenu);

        add(txtArea, BorderLayout.CENTER);
        add(btnAnalyze, BorderLayout.SOUTH);
        btnAnalyze.addActionListener(e -> {
            onAnalyze.accept(((JTextArea) getContentPane().getComponent(0)).getText());
            setVisible(false);
        });


    }

}
