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

import org.talend.daikon.collections.tree.INode;
import org.talend.daikon.collections.tree.Node;

public class MemoryNode<K extends Comparable<K>, T> implements INode<K, T> {

    private final INode<K, T> delegate;

    private final INode<K, T>[] childs = new INode[2];

    private final K key;

    private T data = null;

    public MemoryNode(INode<K, T> delegate, int deep, int limit) {
        super();
        this.delegate = delegate;
        this.key = this.delegate.getKey();
        if (delegate instanceof Node) {
            final Node<K, T> deleg = (Node<K, T>) delegate;
            final INode<K, T> delegateLeft = deleg.getChild(0);
            final INode<K, T> delegateRight = deleg.getChild(1);
            if (deep < limit) {
                if (delegateLeft != null) {
                    this.childs[0] = new MemoryNode<>(delegateLeft, deep + 1, limit);
                } else {
                    this.childs[0] = null;
                }
                if (delegateRight != null) {
                    this.childs[1] = new MemoryNode<>(delegateRight, deep + 1, limit);
                } else {
                    this.childs[1] = null;
                }
            } else {
                this.childs[0] = delegateLeft;
                this.childs[1] = delegateRight;
            }
        } else {
            this.childs[0] = null;
            this.childs[1] = null;
        }
    }

    @Override
    public INode<K, T> getChild(int num) {
        return this.childs[num];
    }

    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public T getData() {
        if (this.data == null) {
            this.data = delegate.getData();
        }
        return this.data;
    }

    @Override
    public INode<K, T> get(K key) {
        int comp = this.key.compareTo(key);
        if (comp == 0) {
            return this;
        }
        if (comp > 0 && this.childs[0] != null) {
            return this.childs[0].get(key);
        }
        if (comp < 0 && this.childs[1] != null) {
            return this.childs[1].get(key);
        }
        return null;
    }
}
