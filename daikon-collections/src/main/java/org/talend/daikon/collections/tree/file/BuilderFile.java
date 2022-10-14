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
package org.talend.daikon.collections.tree.file;

import org.talend.daikon.collections.tree.Node;
import org.talend.daikon.collections.tree.NodeBuilder;

import java.util.function.Supplier;

public class BuilderFile<K extends Comparable<K>, T> implements NodeBuilder<K, T> {

    private final NodeFile<K, T> file;

    public BuilderFile(final NodeFile<K, T> file) {
        this.file = file;
    }

    @Override
    public Node<K, T> findRoot() {
        return this.file.findRoot();
    }

    @Override
    public void newRoot(final Node<K, T> root) {
        this.file.newRoot(root);
    }

    @Override
    public Node<K, T> build(K key, Supplier<T> data) {
        return this.file.createNode(key, data.get());
    }
}
