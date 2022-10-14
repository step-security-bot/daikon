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

public class Node<K extends Comparable<K>, T> implements INode<K, T> {

    private int height;

    private NodeLink<K, T>[] childs = new NodeLink[2];

    private final K key;

    private final Supplier<T> data;

    private final NodeLink<K, T> link;

    public Node(final NodeLink<K, T> link, final K key, final Supplier<T> data) {
        super();
        this.key = key;
        this.data = data;
        this.link = link;
        this.height = 1;
    }

    public static <K extends Comparable<K>, T> int height(final Node<K, T> node) {
        if (node == null) {
            return 0;
        }
        return node.height;
    }

    public static <K extends Comparable<K>, T> int height(final NodeLink<K, T> node) {
        if (node == null) {
            return 0;
        }
        return Node.height(node.getNode());
    }

    public static <K extends Comparable<K>, T> int getBalance(final Node<K, T> node) {
        if (node == null) {
            return 0;
        }
        return Node.height(node.childs[0]) - Node.height(node.childs[1]);
    }

    private void updateHeight() {
        int h = Math.max(Node.height(this.childs[0]), Node.height(this.childs[1])) + 1;
        if (this.height != h) {
            this.height = h;
        }
    }

    public Node<K, T> getChild(int num) {
        if (this.childs[num] == null) {
            return null;
        }
        return this.childs[num].getNode();
    }

    public void setChild(int num, Node<K, T> child) {
        if (child == null) {
            this.childs[num] = null;
        } else {
            this.childs[num] = child.link;
        }
        this.updateHeight();
        this.link.saveNode(this);
    }

    public void setChildSimple(int num, NodeLink<K, T> child) {
        this.childs[num] = child;
    }

    public NodeLink<K, T> getChildSimple(int num) {
        return this.childs[num];
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public T getData() {
        return data.get();
    }

    public NodeLink<K, T> getLink() {
        return link;
    }

    @Override
    public Node<K, T> get(final K item) {
        int comp = this.key.compareTo(item);
        if (comp == 0) {
            return this;
        }
        if (comp > 0 && this.childs[0] != null) {
            return this.childs[0].getNode().get(item);
        }
        if (comp < 0 && this.childs[1] != null) {
            return this.childs[1].getNode().get(item);
        }
        return null;
    }
}
