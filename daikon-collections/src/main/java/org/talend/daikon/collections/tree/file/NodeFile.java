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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * Manipulate tree node on file.
 * 
 * @param <K> class for node keys.
 * @param <T> class for node data.
 */
public class NodeFile<K extends Comparable<K>, T> {

    /** file where tree is stored */
    private final RandomAccessFile file;

    private final Serializer<K> keySerializer;

    private final Serializer<T> dataSerializer;

    public NodeFile(RandomAccessFile file, Serializer<K> keySerializer, Serializer<T> dataSerializer) {
        super();
        this.file = file;
        this.keySerializer = keySerializer;
        this.dataSerializer = dataSerializer;
    }

    public synchronized Node<K, T> findRoot() {
        try {
            if (this.file.length() == 0L) {
                return null;
            }
            this.file.seek((long) "Tree".length());
            final long startRoot = this.file.readLong();
            final NodeLinkFile<K, T> link = new NodeLinkFile<>(this, startRoot);

            return link.getNode();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Replace current root on file.
     * 
     * @param root : new root.
     */
    public void newRoot(final Node<K, T> root) {
        synchronized (this.file) {
            try {
                final NodeLinkFile<K, T> link = (NodeLinkFile<K, T>) root.getLink();
                final long start = link.getStartPos();

                this.file.seek((long) "Tree".length());
                this.file.writeLong(start);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    /**
     * Read node from file.
     * 
     * @param nodeLink : link to this node.
     * @return
     */
    public synchronized Node<K, T> getNode(final NodeLinkFile<K, T> nodeLink) {
        try {
            file.seek(nodeLink.getStartPos());
            final int height = file.readInt();
            final long child1 = file.readLong();
            final long child2 = file.readLong();
            final int keySize = file.readInt();
            final int dataSize = file.readInt();

            final K key = this.read(keySize, this.keySerializer);
            final long startData = this.file.getFilePointer();
            final Supplier<T> dataGetter = () -> this.readFrom(startData, dataSize, dataSerializer);

            final Node<K, T> n = new Node<>(nodeLink, key, dataGetter);
            if (child1 > 0) {
                n.setChildSimple(0, new NodeLinkFile<>(this, child1));
            }
            if (child2 > 0) {
                n.setChildSimple(1, new NodeLinkFile<>(this, child2));
            }

            n.setHeight(height);

            return n;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Add new node on file.
     * 
     * @param key : node key.
     * @param data : node data.
     * @return the new node.
     */
    public synchronized Node<K, T> createNode(K key, T data) {

        try {
            if (this.file.length() == 0L) {
                this.file.writeChars("Tree");
                this.file.writeLong(file.getFilePointer() + Long.BYTES);
            }
            final long startPos = file.length();
            file.seek(startPos);

            file.writeInt(1); // height
            file.writeLong(0L); // left child
            file.writeLong(0L); // right child

            final byte[] keyBytes = this.keySerializer.serialize(key);
            final byte[] dataBytes = this.dataSerializer.serialize(data);
            file.writeInt(keyBytes.length);
            file.writeInt(dataBytes.length);
            file.write(keyBytes);
            file.write(dataBytes);

            final NodeLinkFile<K, T> link = new NodeLinkFile<>(this, startPos);
            return new Node<>(link, key, () -> data);
        } catch (IOException ex) {
            throw new UncheckedIOException("", ex);
        }

    }

    /**
     * Save an existing node when its childs were updated.
     * 
     * @param node : changed node to save.
     * @param startPos : pos of node in file.
     */
    public void saveNode(final Node<K, T> node, final long startPos) {
        synchronized (this.file) {
            try {
                this.file.seek(startPos);
                this.file.writeInt(node.getHeight());

                this.saveLink(node, 0);
                this.saveLink(node, 1);
            } catch (IOException exIO) {
                throw new UncheckedIOException("Error on save node : " + exIO.getMessage(), exIO);
            }

        }
    }

    private void saveLink(final Node<K, T> node, int numLink) throws IOException {
        final NodeLink<K, T> link = node.getChildSimple(numLink);
        if (link instanceof NodeLinkFile) {
            this.file.writeLong(((NodeLinkFile<K, T>) link).getStartPos());
        } else {
            this.file.writeLong(0L);
        }
    }

    private <U> U readFrom(long start, final int size, Serializer<U> serializer) {
        try {
            synchronized (this.file) {
                this.file.seek(start);
                return this.read(size, serializer);
            }
        } catch (IOException exIO) {
            throw new UncheckedIOException("Can't read data : " + exIO.getMessage(), exIO);
        }
    }

    private <U> U read(final int size, Serializer<U> serializer) {
        try {
            final byte[] data = new byte[size];
            this.file.read(data);
            return serializer.deserialize(data);
        } catch (IOException exIO) {
            throw new UncheckedIOException("Can't read data : " + exIO.getMessage(), exIO);
        }
    }

}
