package com.creanga;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.io.ByteStreams;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ZstdUtil {

    public static byte[] compress(byte[] input) {
        try {
            return Zstd.compress(input);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void compress(InputStream input, OutputStream output, int level) throws Exception {
        try (ZstdOutputStream out = new ZstdOutputStream(output,level)) {
            ByteStreams.copy(input, out);
        }
    }

    public static void decompress(InputStream input, OutputStream output) throws Exception {
        try (ZstdInputStream in = new ZstdInputStream(input)) {
            ByteStreams.copy(in, output);
        }
    }

    public static byte[] compress(byte[] input, int level) {
        try {
            return Zstd.compress(input, level);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public static byte[] decompress(byte[] input, int decompressedLength) {
        byte[] decompress = new byte[decompressedLength];
        try {
            long len = Zstd.decompress(decompress,input);
            return Arrays.copyOfRange(decompress, 0, (int)len);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] json = ByteStreams.toByteArray(new FileInputStream("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json"));
        System.out.println(json.length);
        byte[] c = ZstdUtil.compress(json);
        System.out.println(c.length);
        System.out.println(ZstdUtil.compress(json, 10).length);
        System.out.println(ZstdUtil.compress(json, 20).length);
        byte[] d = ZstdUtil.decompress(c, json.length);
        System.out.println(d.length);
    }


}
