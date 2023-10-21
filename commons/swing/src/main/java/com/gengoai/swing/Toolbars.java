package com.gengoai.swing;

import com.gengoai.conversion.Cast;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public final class Toolbars {

    private Toolbars() {
        throw new IllegalAccessError();
    }

    public static JToolBar createToolBar(@NonNull Object... components) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        for (Object component : components) {
            if (component == null) {
                toolBar.addSeparator();
            } else if (component instanceof Dimension) {
                toolBar.addSeparator(Cast.as(component));
            } else if (component instanceof View) {
                toolBar.add(Cast.<View>as(component).getRoot());
            } else {
                toolBar.add(Cast.<Component>as(component));
            }
        }
        return toolBar;
    }

}//END of Toolbars
