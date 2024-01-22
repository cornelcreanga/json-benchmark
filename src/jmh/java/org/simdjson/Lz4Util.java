package org.simdjson;

import com.google.common.io.ByteStreams;
import net.jpountz.lz4.*;
import net.jpountz.xxhash.XXHashFactory;

import java.io.*;
import java.util.Arrays;

public class Lz4Util {
    static LZ4Factory factory = LZ4Factory.fastestInstance();


    public static byte[] decompress(byte[] input, int decompressedLength) {
        byte[] decompress = new byte[decompressedLength];
        try {
            LZ4FastDecompressor lz4FastDecompressor = factory.fastDecompressor();
            lz4FastDecompressor.decompress(input, decompress);
            return decompress;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] compress(byte[] input) {
        return compress(input, false);
    }

    public static byte[] compress(byte[] input, boolean high) {
        try {
            LZ4Compressor lz4Compressor = high ? factory.highCompressor() : factory.fastCompressor();
            int maxCompressedLength = lz4Compressor.maxCompressedLength(input.length);
            byte[] dest = new byte[maxCompressedLength];
            int len = lz4Compressor.compress(input,0,input.length,dest, 0, maxCompressedLength);
            return Arrays.copyOf(dest, len);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public static byte[] decompress(byte[] input) throws Exception{
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
//        decompress(new ByteArrayInputStream(input), baos);
//        return baos.toByteArray();
//    }
//
//    public static byte[] compress(byte[] input)  throws Exception {
//        return compress(input, false);
//    }
//
//    public static byte[] compress(byte[] input, boolean high) throws Exception  {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
//        compress(new ByteArrayInputStream(input), baos, high);
//        return baos.toByteArray();
//    }

    public static void compress(InputStream input, OutputStream output) throws Exception {
        compress(input, output, false);
    }
    public static void compress(InputStream input, OutputStream output, boolean high) throws Exception {
        try (LZ4FrameOutputStream out = new LZ4FrameOutputStream(output,
                LZ4FrameOutputStream.BLOCKSIZE.SIZE_4MB,
                -1,
                high ? factory.highCompressor() : factory.fastCompressor(),
                XXHashFactory.fastestInstance().hash32(),
                LZ4FrameOutputStream.FLG.Bits.BLOCK_INDEPENDENCE)
        ) {
            ByteStreams.copy(input, out);
        }
    }

    public static void decompress(InputStream input, OutputStream output) throws Exception {
        try (LZ4FrameInputStream in = new LZ4FrameInputStream(input)) {
            ByteStreams.copy(in, output);
        }
    }

    public static void main(String[] args) throws Exception {
        //Lz4Util.compressFile("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json","/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json.lz4");
        //Lz4Util.compressFile("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.smile","/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.smile.lz4");
        byte[] in = ByteStreams.toByteArray(new FileInputStream("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json"));
        byte[] json = in;
        System.out.println(json.length);
        byte[] c = Lz4Util.compress(json, false);
        System.out.println(c.length);
        byte[] d = Lz4Util.decompress(c, json.length);
        System.out.println(d.length);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Lz4Util.compress(new ByteArrayInputStream(in), baos);
        byte[] lz4 = baos.toByteArray();
        baos = new ByteArrayOutputStream();
        Lz4Util.decompress(new ByteArrayInputStream(lz4), baos);


        System.out.println(baos.toByteArray().length);
        System.out.println(in.length);
        System.out.println(Lz4Util.compress(in).length);
        System.out.println(Lz4Util.compress(in, true).length);
        System.out.println(ZstdUtil.compress(in).length);
        System.out.println("----");
        for (int i = 1; i <= 20; i++) {
            System.out.println(ZstdUtil.compress(in, i).length);

        }
        System.out.println("smile");
        in = JsonUtil.jsonToSmile(in);
        System.out.println(in.length);
        System.out.println(Lz4Util.compress(in).length);
        System.out.println(Lz4Util.compress(in, true).length);
        System.out.println(ZstdUtil.compress(in).length);
        System.out.println("----");
        for (int i = 1; i <= 20; i++) {
            System.out.println(ZstdUtil.compress(in, i).length);

        }

    }

}
