package org.talend.daikon.collections.tree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.daikon.collections.tree.file.BuilderFile;
import org.talend.daikon.collections.tree.file.NodeFile;
import org.talend.daikon.collections.tree.memory.BuilderMemo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

class AvlTreeTest {

    @Test
    void memoryTest() {
        final BuilderMemo<Integer, String> builderMemo = new BuilderMemo<>();
        final AvlTreeBuilder<Integer, String> treeBuilder = new AvlTreeBuilder<>(builderMemo);

        for (int i = 1; i <= 730; i++) {
            // System.out.println("test " + i);
            final int iCopy = i;
            treeBuilder.insertNode(Integer.valueOf(i), () -> "Node " + iCopy);

            final int iCopyFinal = 1501 - i;
            treeBuilder.insertNode(Integer.valueOf(iCopyFinal), () -> "Node " + iCopyFinal);
            treeBuilder.check();
        }
        final AvlTree<Integer, String> tree = treeBuilder.build();
        final INode<Integer, String> node413Copy = tree.get(413);
        Assertions.assertNotNull(node413Copy);
        Assertions.assertEquals(413, node413Copy.getKey());
        Assertions.assertEquals("Node 413", node413Copy.getData());

        final INode<Integer, String> unknownNode = tree.get(2700);
        Assertions.assertNull(unknownNode);
    }

    @Test
    void fileTest() throws IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        final File fic = new File(url.getPath(), "File2.txt");
        if (fic.exists()) {
            fic.delete();
        }
        fic.createNewFile();
        final RandomAccessFile rf = new RandomAccessFile(fic, "rw");
        final NodeFile<Integer, String> nodeFile = new NodeFile<>(rf, new SerializerInteger(), new SerializerString());

        // check build of avl tree
        final AvlTreeBuilder<Integer, String> treeBuilder = new AvlTreeBuilder<>(new BuilderFile<>(nodeFile));
        for (int i = 1; i <= 1750; i++) {
            final int iCopy = i;
            treeBuilder.insertNode(Integer.valueOf(i), () -> "Node " + iCopy);

            final int iCopyFinal = 3507 - i;
            treeBuilder.insertNode(Integer.valueOf(iCopyFinal), () -> "Node " + iCopyFinal);
        }
        treeBuilder.check();
        final AvlTree<Integer, String> tree = treeBuilder.build();

        // check Avl tree
        final INode<Integer, String> node413Copy = tree.get(413);
        Assertions.assertNotNull(node413Copy);
        Assertions.assertEquals(413, node413Copy.getKey());
        Assertions.assertEquals("Node 413", node413Copy.getData());

        final INode<Integer, String> unknownNode = tree.get(5700);
        Assertions.assertNull(unknownNode);

        final INode<Integer, String> node3506 = tree.get(3506);
        Assertions.assertEquals("Node 3506", node3506.getData());

        final INode<Integer, String> node1503 = tree.get(1503);
        Assertions.assertEquals("Node 1503", node1503.getData());

        final INode<Integer, String> node2800 = tree.get(2800);
        Assertions.assertEquals("Node 2800", node2800.getData());

        // check reading existing file
        final RandomAccessFile rf2 = new RandomAccessFile(fic, "rw");
        final NodeFile<Integer, String> nodeFile2 = new NodeFile<>(rf2, new SerializerInteger(), new SerializerString());

        final BuilderFile<Integer, String> fileBuilder2 = new BuilderFile<>(nodeFile2);
        final AvlTreeBuilder<Integer, String> treeBuilder2 = new AvlTreeBuilder<>(fileBuilder2);
        final AvlTree<Integer, String> tree2 = treeBuilder2.build();

        final INode<Integer, String> node702_Read = tree2.get(702);
        Assertions.assertNotNull(node702_Read);
        Assertions.assertEquals(702, node702_Read.getKey());
        Assertions.assertEquals("Node 702", node702_Read.getData());
    }

}
