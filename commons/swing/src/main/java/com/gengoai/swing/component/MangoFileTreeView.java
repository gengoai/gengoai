package com.gengoai.swing.component;

import com.gengoai.SystemInfo;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.io.File;
import java.util.Optional;

public class MangoFileTreeView extends JTree {

   public MangoFileTreeView() {
      this(new File(SystemInfo.USER_HOME));
   }

   public MangoFileTreeView(@NonNull File root) {
      super(new FileTreeModel(root));
      setCellRenderer(new FileTreeCellRenderer());
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
   }

   public void setShowHidden(boolean showHidden) {
      var ftm = new FileTreeModel((File) getModel().getRoot());
      ftm.setShowHidden(showHidden);
      setModel(ftm);
   }

   public boolean isShowHidden() {
      return ((FileTreeModel) getModel()).isShowHidden();
   }

   public void setRoot(@NonNull File root) {
      setModel(new FileTreeModel(root));
   }

   public Optional<File> getSelectedFile() {
      TreePath path = getSelectionPath();
      if (path == null) {
         return Optional.empty();
      }
      return Optional.of((File) path.getLastPathComponent());
   }
}
