package com.gengoai.swing.component;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.File;

public class FileTreeModel implements TreeModel {
   private final File root;

   public FileTreeModel(File root) {
      this.root = root;
   }

   @Override
   public Object getRoot() {
      return root;
   }

   @Override
   public Object getChild(Object parent, int index) {
      File file = (File) parent;
      File[] children = file.listFiles();
      return children == null ? null : children[index];
   }

   @Override
   public int getChildCount(Object parent) {
      File file = (File) parent;
      File[] children = file.listFiles();
      return children == null ? 0 : children.length;
   }

   @Override
   public boolean isLeaf(Object node) {
      File file = (File) node;
      return file.isFile();
   }

   @Override
   public void valueForPathChanged(TreePath path, Object newValue) {

   }

   @Override
   public int getIndexOfChild(Object parent, Object child) {
      File file = (File) parent;
      if (file.isDirectory() && file.listFiles() != null) {
         File[] children = file.listFiles();
         for (int i = 0; i < children.length; i++) {
            if (children[i].equals(child)) {
               return i;
            }
         }
      }
      return -1;
   }

   @Override
   public void addTreeModelListener(TreeModelListener l) {

   }

   @Override
   public void removeTreeModelListener(TreeModelListener l) {

   }
}
