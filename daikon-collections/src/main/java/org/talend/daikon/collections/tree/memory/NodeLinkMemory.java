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

import org.talend.daikon.collections.tree.Node;
import org.talend.daikon.collections.tree.NodeLink;

public class NodeLinkMemory<K extends Comparable<K>, T> implements NodeLink<K, T> {

    private Node<K, T> node;

    @Override
    public Node<K, T> getNode() {
        return this.node;
    }

    @Override
    public void saveNode(Node<K, T> node) {
        this.node = node;
    }
}
