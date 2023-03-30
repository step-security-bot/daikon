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

/**
 * AVL Tree follow https://www.programiz.com/dsa/avl-tree.
 * This is for storing data in balanced tree (like Java TreeMap), ordered by K(ey).
 * This implementation will store the tree in RandomAccessFile when tree is too big to be in memory.
 * 
 * @author clesaec
 *
 * @param <K> : Type for key
 * @param <T> : Class of stored data.
 */
public class AvlTree<K extends Comparable<K>, T> {

    private INode<K, T> root;

    public AvlTree(INode<K, T> root) {
        this.root = root;
    }

    public INode<K, T> get(K item) {
        if (this.root == null) {
            return null;
        }
        return this.root.get(item);
    }

    public AvlTreeIterator.IteratorBuilder<K, T> iterator() {
        return new AvlTreeIterator.IteratorBuilder<>(this.root);
    }

}
