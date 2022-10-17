package org.talend.daikon.collections.tree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.daikon.collections.tree.memory.BuilderMemo;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class AvlTreeIteratorTest {

    @Test
    void next() {
        final BuilderMemo<Integer, String> builderMemo = new BuilderMemo<>();
        final AvlTreeBuilder<Integer, String> treeBuilder = new AvlTreeBuilder<>(builderMemo);

        for (int i = 0; i < 30; i++) {
            final int index = i;
            treeBuilder.insertNode(i, () -> "Hello_" + index);
            treeBuilder.insertNode(400 - i, () -> "Hello_" + (400 - index));
        }

        AvlTree<Integer, String> tree = treeBuilder.build();
        Iterator<INode<Integer, String>> iterator = tree.iterator().build();
        this.testIterator(iterator, 0, 400, 60);

        Iterator<INode<Integer, String>> iterator2 = tree.iterator().startAt(200).build();
        this.testIterator(iterator2, 371, 400, 30);

        Iterator<INode<Integer, String>> iterator3 = tree.iterator().endAt(200).build();
        this.testIterator(iterator3, 0, 29, 30);

        Iterator<INode<Integer, String>> iterator4 = tree.iterator().endAt(380).startAt(20).build();
        this.testIterator(iterator4, 20, 380, 20);

        Iterator<INode<Integer, String>> iterator5 = tree.iterator().startAt(380).endAt(20).build();
        this.testIterator(iterator5, 0, 0, 0);
    }

    private void testIterator(Iterator<INode<Integer, String>> iterator, int min, int max, int nbeElement) {
        int nbe = 0;
        INode<Integer, String> next = null;
        Integer oldKey = null;
        while (iterator.hasNext()) {
            next = iterator.next();
            Assertions.assertNotNull(next);
            if (oldKey != null) {
                Assertions.assertTrue(oldKey < next.getKey());
            }
            if (nbe == 0) {
                Assertions.assertEquals(min, next.getKey());
            }
            nbe++;
        }
        Assertions.assertEquals(nbeElement, nbe);
        if (nbe > 0) {
            Assertions.assertEquals(max, next.getKey());
        }
    }
}