package com.gengoai.swing.component;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

public class MangoColorCombobox  extends JComboBox<MangoColorCombobox.ColorItem> {

    public static class ColorItem {
        private final Color color;

        public ColorItem(Color color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return " ";
        }
    }

    public static class ColorRenderer extends BasicComboBoxRenderer {


        @Override
        public void setOpaque(boolean isOpaque) {
            super.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);
            ColorItem colorItem = (ColorItem) value;
            setText(value.toString());
            setBackground(colorItem.color);
            return this;
        }

    }

    static class ColorComboBoxEditor implements ComboBoxEditor {
        final protected JButton editor;

        protected EventListenerList listenerList = new EventListenerList();

        public ColorComboBoxEditor(Color initialColor) {
            editor = new JButton("");
            editor.setBackground(initialColor);
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Color currentBackground = editor.getBackground();
                    Color color = JColorChooser.showDialog(editor, "Color Chooser", currentBackground);
                    if ((color != null) && (currentBackground != color)) {
                        editor.setBackground(color);
                        fireActionEvent(color);
                    }
                }
            };
            editor.addActionListener(actionListener);
        }

        public void addActionListener(ActionListener l) {
            listenerList.add(ActionListener.class, l);
        }

        public Component getEditorComponent() {
            return editor;
        }

        public Object getItem() {
            return editor.getBackground();
        }

        public void removeActionListener(ActionListener l) {
            listenerList.remove(ActionListener.class, l);
        }

        public void selectAll() {
            // Ignore
        }

        public void setItem(Object newValue) {
            if (newValue instanceof Color) {
                Color color = (Color) newValue;
                editor.setBackground(color);
            } else {
                try {
                    Color color = Color.decode(newValue.toString());
                    editor.setBackground(color);
                } catch (NumberFormatException e) {
                }
            }
        }

        protected void fireActionEvent(Color color) {
            Object listeners[] = listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ActionListener.class) {
                    ActionEvent actionEvent = new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, color
                            .toString());
                    ((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
                }
            }
        }
    }

    public MangoColorCombobox() {
        super(new ColorItem[]{
                new ColorItem(Color.RED),
                new ColorItem(Color.GREEN),
                new ColorItem(Color.BLUE),
                new ColorItem(Color.YELLOW),
                new ColorItem(Color.BLACK),
                new ColorItem(Color.WHITE),
                new ColorItem(Color.CYAN),
                new ColorItem(Color.MAGENTA),
                new ColorItem(Color.ORANGE),
                new ColorItem(Color.PINK),
                new ColorItem(Color.GRAY),
                new ColorItem(Color.LIGHT_GRAY),
                new ColorItem(Color.DARK_GRAY)
        });
        setRenderer(new ColorRenderer());
        setOpaque(true);
        setEditable(true);
        setEditor(new ColorComboBoxEditor(Color.RED));
        addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if( e.getItem() instanceof ColorItem ) {
                    ColorItem colorItem = (ColorItem) e.getItem();
                    ColorComboBoxEditor ed = (ColorComboBoxEditor) getEditor();
                    ed.editor.setBackground(colorItem.color);
                } else if ( e.getItem() instanceof Color ){
                    Color color = (Color) e.getItem();
                    ColorComboBoxEditor ed = (ColorComboBoxEditor) getEditor();
                    ed.editor.setBackground(color);
                }
            }
        });
    }

    public Color getSelectedColor() {
        return ((ColorComboBoxEditor) getEditor()).editor.getBackground();
    }

}
