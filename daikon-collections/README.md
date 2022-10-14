# Talend Daikon - Crypto utils

## Description

This module contains collections that can be partially stored on disk to save memory.
It needs a file system that accept random access file.

## Build

To build module, simply run `mvn install`

```bash
$> mvn install
```

## Usage

This library offers [AVL Tree](https://www.programiz.com/dsa/avl-tree) on disk (to search values by keys).

Say, you want to save **Value** class range with **Key** class. Key class must implements comparable (As AVL Tree is a Sorted Tree)
First, define [serializer](./src/main/java/org/talend/daikon/collections/tree/file/Serializer.java) for both
```java
public class SerializerKey implements Serializer<Key> {
    @Override
    public byte[] serialize(final Key k) {
        // return byte that seralize k
    }
    @Override
    public Key deserialize(byte[] data) {
        // return Key for serialized data.
    }
}

public class SerializerValue implements Serializer<Value> {
    @Override
    public byte[] serialize(final Value v) {
        // return byte that seralize v
    }
    @Override
    public Value deserialize(byte[] data) {
        // return value for serialized data.
    }
}
```

Then define a builder for Tree
```java
final NodeFile<Key, Value> nodeFile = new NodeFile<>(randomAccessFile, new SerializerKey(), new SerializerValue());
final AvlTreeBuilder<Key, Value> treeBuilder = new AvlTreeBuilder<>(new BuilderFile<>(nodeFile));
```

Construct the tree
```java
// insert key/value
treeBuilder.insertNode(oneKey, () -> oneValue); // or use a value getter for value.
treeBuilder.insertNode(otherKey, () -> otherValue);
// ...

// build the tree
final AvlTree<Key, Value> tree = treeBuilder.build(); // or use treeBuilder.build(5); to have a depth of element in memory of 5, default is 10.  
```

use it
```java
final INode<Key, Value> aNode = tree.get(aKey);

Key k = aNode.getKey();
Value v = aNode.getData();
```
The tree is mostly 

## Limitation

- Once tree is build, it's immutable (but then support multi-threading).
- No deletion on tree builder (would be complex to add).
- Built tree only has "Value get(key)" method, no iteration from a key to another; but it would be easy to add if needed. 
- There are no clever method for AvlTreeBuilder.build(...) to give a limit in term of memory usage (just in term of depth). Quite difficult to add if needed. 

## License

Copyright (c) 2006-2022 Talend

Licensed under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0.txt)
