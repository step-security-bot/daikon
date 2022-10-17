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
 * Node of AvlTree
 * 
 * @param <K> : Class for Key.
 * @param <T> : Class for stored data.
 */
public interface INode<K extends Comparable<K>, T> {

    K getKey();

    T getData();

    INode<K, T> get(final K key);

    INode<K, T> getChild(int num);
}
