/*
 * Copyright (C) 2006-2022 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package org.talend.daikon.collections.tree;

import java.util.function.Supplier;

import org.talend.daikon.collections.tree.memory.MemoryNode;

/**
 * Tree builder.
 * 
 * @param <K> key class.
 * @param <T> data class.
 */
public class AvlTreeBuilder<K extends Comparable<K>, T> {

    private Node<K, T> root;

    private final NodeBuilder<K, T> builder;

    public AvlTreeBuilder(NodeBuilder<K, T> builder) {
        this.builder = builder;
        this.root = this.builder.findRoot();
    }

    /**
     * See https://www.programiz.com/dsa/avl-tree
     * A child become the parent.
     * Used to maintain balanced tree.
     * 
     * @param parent : current parent.
     * @param start : child that will be new parent.
     * @return new Parent.
     */
    private Node<K, T> rotate(Node<K, T> parent, int start) {
        // get futur parent.
        final Node<K, T> pivot = parent.getChild(start);

        // get futur direct child of "current parent"
        final Node<K, T> subChild = pivot.getChild(1 - start);

        pivot.setChild(1 - start, null);
        parent.setChild(start, null);

        // new parent own its ancient parent as child.
        pivot.setChild(1 - start, parent);
        // ancient parent put its grand child as direct child.
        parent.setChild(start, subChild);

        return pivot;
    }

    private Node<K, T> rebalance(final Node<K, T> node, final int firstChild, final K item) {

        final K childData = node.getChild(firstChild).getKey();
        final int comp = item.compareTo(childData);

        final Node<K, T> ret;
        if ((comp < 0 && firstChild == 0) || (comp > 0 && firstChild == 1)) {
            ret = this.rotate(node, firstChild);
        } else {
            final Node<K, T> newLeftChild = this.rotate(node.getChild(firstChild), 1 - firstChild);
            node.setChild(firstChild, newLeftChild);
            ret = this.rotate(node, firstChild);
        }
        return ret;
    }

    private Node<K, T> insertNode(final Node<K, T> from, final Node<K, T> newNode) {
        if (from == null) {
            return newNode;
        }

        final K nodeItem = from.getKey();

        int value = newNode.getKey().compareTo(nodeItem);
        if (value < 0) {
            final Node<K, T> newLeft = this.insertNode(from.getChild(0), newNode);
            from.setChild(0, newLeft);
        } else {
            final Node<K, T> newRight = this.insertNode(from.getChild(1), newNode);
            from.setChild(1, newRight);
        }

        final int balanceFactor = Node.getBalance(from);
        if (balanceFactor > 1) {
            return this.rebalance(from, 0, newNode.getKey());
        } else if (balanceFactor < -1) {
            return this.rebalance(from, 1, newNode.getKey());
        }
        return from;
    }

    // Insert a node
    public void insertNode(final K key, final Supplier<T> data) {

        // Find the position and insert the node
        final Node<K, T> newNode = this.builder.build(key, data);
        if (this.root == null) {
            this.root = newNode;
        } else {
            final Node<K, T> newRoot = this.insertNode(this.root, newNode);
            if (newRoot != this.root) {
                this.root = newRoot;
                this.builder.newRoot(newRoot);
            }
        }
    }

    public AvlTree<K, T> build() {
        return this.build(10);
    }

    /**
     * build immutable AVL Tree to be used for search key/value.
     * 
     * @param limit : depth on this tree where we keep node in memory.
     * (total of node keep in memory will be 2^limit).
     * @return immutable AVL Tree.
     */
    public AvlTree<K, T> build(int limit) {
        // even if tree is mainly stored on disk, keep elements in memory
        // to accelerate reading (deep of 10, roughly 1000 elements)
        if (this.root == null) {
            return null; // no element
        }
        final INode<K, T> rootNode = new MemoryNode<>(this.root, 1, limit);
        return new AvlTree<>(rootNode);
    }

    private String checkNode(final Node<K, T> node) {
        final StringBuilder builder = new StringBuilder();
        final K current = node.getKey();
        int h = Node.height(node);
        final Node<K, T> left = node.getChild(0);
        if (left != null) {
            final K leftKey = left.getKey();
            if (leftKey.compareTo(current) > 0) {
                builder.append(System.lineSeparator() + "Error Left > Parent");
            }
            builder.append(this.checkNode(left));
        }
        final Node<K, T> right = node.getChild(1);
        if (right != null) {
            final K rightKey = right.getKey();
            if (rightKey.compareTo(current) < 0) {
                builder.append(System.lineSeparator() + "Error Right(" + rightKey + ") < Parent (" + current + ")");
            }
            builder.append(this.checkNode(right));
        }
        if (h != Math.max(Node.height(left), Node.height(right)) + 1) {
            builder.append(System.lineSeparator() + "Height Error ==> " + current + " h=" + h + " left: " + Node.height(left)
                    + ", right: " + Node.height(right));
        }
        int bal = Node.getBalance(node);
        if ((bal < -1) || (bal > 1)) {
            builder.append(System.lineSeparator() + "Balance error " + bal + " node " + node.getKey());
        }
        return builder.toString();
    }

    /**
     * Method for unit test only (package protected)
     * to check builder is balanced.
     */
    void check() {
        if (this.root != null) {
            final String errors = this.checkNode(this.root);
            if (errors != null && !errors.isEmpty()) {
                throw new RuntimeException(errors);
            }
        }
    }

}
