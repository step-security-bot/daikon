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

/**
 * To serialize / deserialize data.
 * 
 * @param <T> : class to serialize.
 */
public interface Serializer<T> {

    byte[] serialize(final T object);

    T deserialize(final byte[] data);
}
