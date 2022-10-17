package org.talend.daikon.collections.tree;

import org.talend.daikon.collections.tree.file.Serializer;

class SerializerString implements Serializer<String> {

    @Override
    public byte[] serialize(final String value) {
        return value.getBytes();
    }

    @Override
    public String deserialize(byte[] data) {
        return new String(data);
    }
}
