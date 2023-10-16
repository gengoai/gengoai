package com.gengoai.swing;

import com.gengoai.conversion.Cast;

import javax.swing.border.Border;
import java.awt.*;

/**
 * <p>Border that draws a title box around a component.</p>
 */
public class TitleBoxBorder implements Border {
    private final Color color;
    private final String title;

    /**
     * Instantiates a new Title box border.
     *
     * @param title the title
     */
    public TitleBoxBorder(String title) {
        this(title, FlatLafColors.Button_borderColor.color());
    }

    /**
     * Instantiates a new Title box border.
     *
     * @param title the title
     * @param color the color of the border
     */
    public TitleBoxBorder(String title, Color color) {
        this.title = title;
        this.color = color;
    }

    @Override
    public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
        int height = Fonts.getFontHeight(component);
        g.setColor(color);
        g.fillRect(x, y, w, y + height);
        g.fillRect(x, y, x + 3, h);
        g.fillRect(w - 3, y, w, h);
        g.fillRect(x, h - 3, w, h);
        g.setColor(Colors.calculateBestFontColor(color));
        Graphics2D ig = Cast.as(g);
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawString(title, x + 10, y + height - height / 4);
        g.setColor(Colors.adjust(color, 0.5));
        g.drawRect(x + 3, y + height, w - 6, h - 3 - height);
    }

    @Override
    public Insets getBorderInsets(Component component) {
        return new Insets(30, 5, 5, 5);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

}//END OF TitleBoxBorder
