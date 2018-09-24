package com.bbproject.noconoco.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializer {

    public static String serialize(Serializable pObj) throws IOException {
        if (pObj == null) return "";

        ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
        objStream.writeObject(pObj);
        objStream.close();
        return encodeBytes(serialObj.toByteArray());
    }

    public static Object deserialize(String pStr) throws IOException {
        if (pStr == null || pStr.length() == 0) return null;
        ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(pStr));
        ObjectInputStream objStream = new ObjectInputStream(serialObj);
        try {
            return objStream.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static String encodeBytes(byte[] pBytes) {
        StringBuilder strBuf = new StringBuilder();

        for (byte aByte : pBytes) {
            strBuf.append((char) (((aByte >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((aByte) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

    private static byte[] decodeBytes(String pStr) {
        int count = pStr.length();
        byte[] bytes = new byte[count / 2];
        for (int i = 0; i < count; i += 2) {
            char c = pStr.charAt(i);
            bytes[i / 2] = (byte) ((c - 'a') << 4);
            c = pStr.charAt(i + 1);
            bytes[i / 2] += (c - 'a');
        }
        return bytes;
    }

}