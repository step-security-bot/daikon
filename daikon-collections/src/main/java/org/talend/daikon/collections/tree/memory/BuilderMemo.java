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
package org.talend.daikon.collections.tree.memory;

import java.util.function.Supplier;

import org.talend.daikon.collections.tree.Node;
import org.talend.daikon.collections.tree.NodeBuilder;
import org.talend.daikon.collections.tree.NodeLink;

public class BuilderMemo<K extends Comparable<K>, T> implements NodeBuilder<K, T> {

    private Node<K, T> root = null;

    @Override
    public Node<K, T> findRoot() {
        return this.root;
    }

    @Override
    public void newRoot(final Node<K, T> root) {
        this.root = root;
    }

    @Override
    public Node<K, T> build(K key, Supplier<T> data) {
        final NodeLink<K, T> link = new NodeLinkMemory<>();
        final Node<K, T> node = new Node<>(link, key, data);
        link.saveNode(node);
        return node;
    }

}
