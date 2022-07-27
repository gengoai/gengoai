/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.swing.component;

import com.gengoai.conversion.Cast;
import com.gengoai.string.Strings;
import com.gengoai.swing.component.listener.SwingListeners;
import com.gengoai.swing.component.listener.TreeViewItemSelectionListener;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.function.BiPredicate;

public class MangoTreeView extends JTree {
   private static final long serialVersionUID = 1L;
   protected final DefaultTreeModel baseModel;
   @NonNull
   protected final BiPredicate<String, Object> itemMatcher;
   private final Vector<TreeViewItemSelectionListener> selectActionListeners = new Vector<>();
   protected DefaultMutableTreeNode ROOT;

   public MangoTreeView(@NonNull BiPredicate<String, Object> itemMatcher) {
      this.itemMatcher = itemMatcher;
      this.baseModel = Cast.as(getModel());
      addKeyListener(SwingListeners.enterKeyPressed(e -> {
         if(getSelectionPath() != null) {
            DefaultMutableTreeNode node = Cast.as(getSelectionPath().getLastPathComponent());
            fireItemSelection(node.getUserObject());
         }
      }));
   }

   public static DefaultMutableTreeNode node(Object data, DefaultMutableTreeNode... children) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
      for(DefaultMutableTreeNode child : children) {
         node.add(child);
      }
      return node;
   }

   public void addSelectedItemListener(@NonNull TreeViewItemSelectionListener listener) {
      selectActionListeners.add(listener);
   }

   public void clearFilter() {
      setFilter(Strings.EMPTY);
   }

   private DefaultMutableTreeNode copy(DefaultMutableTreeNode node) {
      var newNode = new DefaultMutableTreeNode(node.getUserObject());
      if(node.children() != null) {
         node.children()
             .asIterator()
             .forEachRemaining(n -> newNode.add(copy(Cast.as(n))));
      }
      return newNode;
   }

   public void expandAll() {
      for(int i = 0; i < getRowCount(); i++) {
         expandRow(i);
      }
   }

   private void filter(String filter) {
      if(ROOT == null) {
         return;
      }
      boolean rootIsVisible = isRootVisible();
      if(Strings.isNullOrBlank(filter)) {
         baseModel.setRoot(ROOT);
      } else {
         DefaultMutableTreeNode root = copy(ROOT);
         Queue<DefaultMutableTreeNode> leaves = getLeafs(root);
         while(leaves.size() > 0) {
            DefaultMutableTreeNode leaf = leaves.remove();
            if(leaf == root) {
               continue;
            }
            if(!itemMatcher.test(filter, leaf.getUserObject())) {
               DefaultMutableTreeNode parent = Cast.as(leaf.getParent());
               if(parent != null) {
                  parent.remove(leaf);
                  if(parent.getChildCount() == 0) {
                     leaves.add(parent);
                  }
               }
            }
         }
         baseModel.setRoot(root);
      }
      setModel(baseModel);
      setRootVisible(rootIsVisible);
      updateUI();
      expandAll();
   }

   protected void fireItemSelection(Object o) {
      for(TreeViewItemSelectionListener selectActionListener : selectActionListeners) {
         selectActionListener.onSelect(o);
      }
   }

   private Queue<DefaultMutableTreeNode> getLeafs(DefaultMutableTreeNode root) {
      DefaultMutableTreeNode leaf = Cast.as(root.getFirstLeaf());
      if(leaf.isRoot()) {
         return new LinkedList<>();
      }
      Queue<DefaultMutableTreeNode> set = new LinkedList<>();
      int numberOfLeaves = root.getLeafCount();
      for(int i = 0; i < numberOfLeaves; i++) {
         if(leaf.getChildCount() == 0) {
            set.add(leaf);
         }
         leaf = Cast.as(leaf.getNextLeaf());
      }
      return set;
   }

   public void setFilter(String text) {
      filter(text);
   }

   @Override
   public void setModel(TreeModel model) {
      if(model instanceof DefaultTreeModel) {
         super.setModel(model);
      } else {
         throw new IllegalArgumentException("Only DefaultTreeModel are accepted");
      }
   }

   public void setRoot(TreeNode node) {
      if(node instanceof DefaultMutableTreeNode) {
         baseModel.setRoot(node);
         ROOT = Cast.as(node);
      } else {
         throw new IllegalArgumentException("Only DefaultMutableTreeNode are accepted");
      }
   }

}//END OF MangoTreeView
