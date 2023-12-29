package com.gengoai.swing.component;


import com.gengoai.swing.View;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class SidePanelContentFrame implements View {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JToolBar toolbar = new JToolBar();
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JComponent contentPane;

    public SidePanelContentFrame(@NonNull JComponent contentPane) {
        toolbar.setFloatable(false);
        toolbar.setOrientation(JToolBar.VERTICAL);
        panel.add(toolbar, BorderLayout.WEST);
        this.contentPane = contentPane;


        splitPane.setRightComponent(contentPane);
        panel.add(splitPane, BorderLayout.CENTER);
    }

    public void addSidePanel(@NonNull SidePanel sidePanel) {
        toolbar.add(sidePanel.button);
        sidePanel.button.addActionListener(l -> {
            if (splitPane.getLeftComponent() == sidePanel.getRoot()) {
                splitPane.setLeftComponent(null);
            } else {
                int div = splitPane.getDividerLocation();
                splitPane.setLeftComponent(sidePanel.getRoot());
                if (div > 0) {
                    splitPane.setDividerLocation(div);
                }
            }
        });
    }

    public void hideSidePanel() {
        splitPane.setLeftComponent(null);
    }


    @Override
    public JComponent getRoot() {
        return panel;
    }
}
