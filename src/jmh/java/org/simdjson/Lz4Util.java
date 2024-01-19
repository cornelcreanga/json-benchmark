package org.simdjson;

import com.google.common.io.ByteStreams;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;

import java.io.*;

public class Lz4Util {
    static LZ4Factory factory = LZ4Factory.fastestInstance();

    public static byte[] decompress(byte[] input) {
        try {
            LZ4BlockInputStream in = new LZ4BlockInputStream(new ByteArrayInputStream(input));
            return in.readAllBytes();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void compressFile(String input, String output) throws Exception {
        try (InputStream in = new FileInputStream(input);
             LZ4BlockOutputStream out = new LZ4BlockOutputStream(new FileOutputStream(output), 1024 * 1024)) {
            ByteStreams.copy(in, out);
        }
    }

    public static void decompressFile(String input, String output) throws Exception {
        try (OutputStream out = new FileOutputStream(output);
             LZ4BlockInputStream in = new LZ4BlockInputStream(new FileInputStream(output))) {
            ByteStreams.copy(in, out);
        }

    }

    public static void main(String[] args) throws Exception {
        //Lz4Util.compressFile("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json","/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json.lz4");
        Lz4Util.compressFile("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.smile","/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.smile.lz4");
    }

}
