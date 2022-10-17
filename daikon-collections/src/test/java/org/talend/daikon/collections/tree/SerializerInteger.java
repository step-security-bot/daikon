package org.talend.daikon.collections.tree;

import org.talend.daikon.collections.tree.file.Serializer;

class SerializerInteger implements Serializer<Integer> {

    @Override
    public byte[] serialize(final Integer value) {
        byte[] data = new byte[Integer.BYTES];
        for (int i = 0; i < Integer.BYTES; i++) {
            data[i] = (byte) ((value.intValue() >>> (Integer.BYTES - i - 1) * Byte.SIZE) & 0xFF);
        }
        return data;
    }

    @Override
    public Integer deserialize(byte[] data) {
        int value = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            int decal = (Integer.BYTES - i - 1) * Byte.SIZE;
            int theData = (int) (data[i] & 0xFF);
            value += theData << decal;
        }
        return Integer.valueOf(value);
    }
}
