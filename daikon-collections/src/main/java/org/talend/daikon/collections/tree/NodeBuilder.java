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

public interface NodeBuilder<K extends Comparable<K>, T> {

    Node<K, T> findRoot();

    void newRoot(final Node<K, T> root);

    Node<K, T> build(final K key, final Supplier<T> data);
}
