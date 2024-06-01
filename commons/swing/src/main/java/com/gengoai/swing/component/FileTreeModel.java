package com.gengoai.swing.component;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileTreeModel implements TreeModel {
   private final File root;
   private final ConcurrentHashMap<File, List<File>> children = new ConcurrentHashMap<>();
   private final AtomicBoolean showHidden = new AtomicBoolean(false);

   public FileTreeModel(File root) {
      this.root = root;
   }

   public void setShowHidden(boolean showHidden) {
      this.showHidden.set(showHidden);
      children.clear();
   }

   public boolean isShowHidden() {
      return showHidden.get();
   }

   private List<File> getChildren(File parent) {
      File[] children = parent.listFiles();
      if (children == null) {
         return List.of();
      }
      List<File> childList = new ArrayList<>(List.of(children));
      if (!showHidden.get()) {
         childList.removeIf(File::isHidden);
      }
      childList.sort((f1, f2) -> {
         if (f1.isDirectory()) {
            if (f2.isDirectory()) {
               return f1.getName().compareTo(f2.getName());
            }
            return -1;
         }

         if (f2.isDirectory()) {
            return 1;
         }

         return f1.getName().compareTo(f2.getName());
      });
      return childList;
   }

   @Override
   public Object getRoot() {
      return root;
   }

   @Override
   public Object getChild(Object parent, int index) {
      File file = (File) parent;
      List<File> children = this.children.computeIfAbsent(file, this::getChildren);
      return children.get(index);
   }

   @Override
   public int getChildCount(Object parent) {
      File file = (File) parent;
      List<File> children = this.children.computeIfAbsent(file, this::getChildren);
      return children.size();
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
      List<File> children = this.children.computeIfAbsent(file, this::getChildren);
      return children.indexOf((File) child);
   }

   @Override
   public void addTreeModelListener(TreeModelListener l) {

   }

   @Override
   public void removeTreeModelListener(TreeModelListener l) {

   }
}
