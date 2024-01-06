package com.gengoai.swing.component;


import com.gengoai.io.resource.Resource;
import com.gengoai.swing.View;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseListener;
import java.util.List;

/**
 * <p>Displays a file tree.</p>
 */
public class MangoFileTree implements View {
    private final JTree tree = new JTree();
    private final JScrollPane scrollPane = new JScrollPane(tree);


    public MangoFileTree() {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * Instantiates a new File tree.
     */
    public MangoFileTree(@NonNull Resource root) {
        setLocation(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }


    public static class FileTreeNode extends DefaultMutableTreeNode {
        public final Resource resource;

        public FileTreeNode(Resource resource) {
            super(resource.baseName());
            this.resource = resource;
        }

        public FileTreeNode(Resource resource, boolean isRoot) {
            super(isRoot ? resource.path() : resource.baseName());
            this.resource = resource;
        }

    }

    /**
     * Sets the location of the file tree.
     *
     * @param root the root
     */
    public void setLocation(@NonNull Resource root) {
        var rootNode = new FileTreeNode(root, true);
        tree.setModel(new DefaultTreeModel(rootNode));
        tree.setShowsRootHandles(true);
        recurse(root, rootNode);
        tree.expandRow(0);
    }

    /**
     * Gets the DefaultTreeModel for the tree.
     *
     * @return the model
     */
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    /**
     * Adds a resource to the tree.
     *
     * @param parent   the parent
     * @param resource the resource
     */
    public void addResource(@NonNull FileTreeNode parent, @NonNull String resource) {
        var newFile = parent.resource;
        if (newFile.isDirectory()) {
            newFile = newFile.getChild(resource);
        } else {
            newFile = newFile.getParent().getChild(resource);
        }
        if (newFile.exists()) {
            JOptionPane.showMessageDialog(null, "File already exists");
            return;
        }
        MangoFileTree.FileTreeNode node = new MangoFileTree.FileTreeNode(newFile);
        parent.add(node);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.nodesWereInserted(parent, new int[]{parent.getChildCount() - 1});
    }


    /**
     * Reloads the tree.
     */
    public void reload() {
        setLocation(getRootNode().resource);
    }

    /**
     * Gets the selected node.
     *
     * @return the selected node
     */
    public FileTreeNode getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            return null;
        }
        return (FileTreeNode) path.getLastPathComponent();
    }

    /**
     * Gets the root node.
     *
     * @return the root node
     */
    public FileTreeNode getRootNode() {
        return (FileTreeNode) tree.getModel().getRoot();
    }

    /**
     * Adds a mouse listener to the tree.
     *
     * @param listener the listener
     */
    public void addMouseListener(@NonNull MouseListener listener) {
        tree.addMouseListener(listener);
    }

    /**
     * Adds a tree selection listener to the tree.
     *
     * @param listener the listener
     */
    public void addTreeSelectionListener(@NonNull TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    private void recurse(Resource resource, DefaultMutableTreeNode node) {
        List<Resource> children = resource.getChildren();
        children.sort((r1, r2) -> {
            if (r1.isDirectory() && !r2.isDirectory()) {
                return -1;
            } else if (!r1.isDirectory() && r2.isDirectory()) {
                return 1;
            }
            return r1.baseName().toLowerCase().compareTo(r2.baseName().toLowerCase());
        });
        for (Resource child : children) {
            if (child.asFile().get().isHidden()) {
                continue;
            }
            var childNode = new FileTreeNode(child);
            node.add(childNode);
            if (child.isDirectory()) {
                recurse(child, childNode);
            }
        }
    }

    @Override
    public JComponent getRoot() {
        return scrollPane;
    }

} //END OF FileTree
