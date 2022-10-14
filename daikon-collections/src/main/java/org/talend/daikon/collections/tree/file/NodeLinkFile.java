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
import org.talend.daikon.collections.tree.NodeLink;

public class NodeLinkFile<K extends Comparable<K>, T> implements NodeLink<K, T> {

    private final NodeFile<K, T> file;

    private final long startPos;

    public NodeLinkFile(final NodeFile<K, T> file, long startPos) {
        super();
        this.file = file;
        this.startPos = startPos;
    }

    @Override
    public Node<K, T> getNode() {
        return this.file.getNode(this);
    }

    public long getStartPos() {
        return startPos;
    }

    @Override
    public void saveNode(final Node<K, T> node) {
        this.file.saveNode(node, this.startPos);
    }
}
