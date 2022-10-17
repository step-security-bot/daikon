package org.talend.daikon.collections.tree;

import java.util.Iterator;

/**
 * Iterate on a Tree.
 * Left => current => right
 *
 * @param <K>
 * @param <T>
 */
public class AvlTreeIterator<K extends Comparable<K>, T> implements Iterator<INode<K, T>> {

    private INode<K, T> nextElement;

    private boolean currentDone = false;

    private INode<K, T> localRoot;

    private Iterator<INode<K, T>>[] childsIterator = new Iterator[2];

    private boolean[] testedChild = new boolean[] { false, false };

    private final K startKey;

    private final K endKey;

    public AvlTreeIterator(INode<K, T> startWith, K startKey, K endKey) {
        this.localRoot = startWith;
        this.startKey = startKey;
        this.endKey = endKey;
        this.nextElement = this.searchNext();
    }

    @Override
    public boolean hasNext() {
        return nextElement != null;
    }

    @Override
    public INode<K, T> next() {
        final INode<K, T> next = nextElement;
        this.nextElement = this.searchNext();
        return next;
    }

    private INode<K, T> searchNext() {
        this.initChildIterator(0);
        if (this.childsIterator[0] != null && this.childsIterator[0].hasNext()) {
            return this.childsIterator[0].next();
        }
        if (!currentDone) {
            this.currentDone = true;
            K key = this.localRoot.getKey();
            if ((this.startKey == null || this.startKey.compareTo(key) <= 0)
                    && (this.endKey == null || this.endKey.compareTo(key) >= 0)) {
                return this.localRoot;
            }
        }
        this.initChildIterator(1);
        if (this.childsIterator[1] != null && this.childsIterator[1].hasNext()) {
            return this.childsIterator[1].next();
        }
        return null;
    }

    private void initChildIterator(int childNum) {
        if (!this.testedChild[childNum]) {
            final INode<K, T> child = this.localRoot.getChild(childNum);
            boolean explore = child != null;
            if (explore && childNum == 0) {
                // Explore left subtree where (key <= localRoot.key) only if localroot.key >= start key
                // (otherwise, all key of left sub-tree can't be valid).
                explore = this.startKey == null || this.startKey.compareTo(this.localRoot.getKey()) <= 0;
            } else if (explore) {
                // Explore right subtree where (key >= localRoot.key) only if localroot.key <= end key
                // (otherwise, all key of right sub-tree can't be valid).
                explore = this.endKey == null || this.endKey.compareTo(this.localRoot.getKey()) >= 0;
            }
            if (explore) {
                this.childsIterator[childNum] = new AvlTreeIterator<>(child, startKey, endKey);
            }
            this.testedChild[childNum] = true;
        }
    }

    public static class IteratorBuilder<K extends Comparable<K>, T> {

        private final INode<K, T> root;

        private K startKey = null;

        private K endKey = null;

        IteratorBuilder(INode<K, T> root) {
            this.root = root;
        }

        public AvlTreeIterator<K, T> build() {
            return new AvlTreeIterator<>(this.root, this.startKey, this.endKey);
        }

        public IteratorBuilder<K, T> startAt(K startKey) {
            this.startKey = startKey;
            return this;
        }

        public IteratorBuilder<K, T> endAt(K endKey) {
            this.endKey = endKey;
            return this;
        }
    }
}
