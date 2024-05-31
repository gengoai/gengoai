package com.gengoai.swing.component;

import com.gengoai.io.FileUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

   @Override
   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if(!(value instanceof File file)){
         return this;
      }
      if (row == 0) {
         setText(file.getAbsolutePath());
         return this;
      }
      if (file.isDirectory()) {
         setIcon(UIManager.getIcon("FileView.directoryIcon"));
      } else {
         setIcon(UIManager.getIcon("FileView.fileIcon"));
      }
      setText(FileUtils.baseName(file.getAbsolutePath()));
      return this;
   }
}
