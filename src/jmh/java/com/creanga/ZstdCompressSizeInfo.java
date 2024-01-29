package com.creanga;

import com.google.common.io.ByteStreams;

import java.io.IOException;

public class ZstdCompressSizeInfo {


    public static void main(String[] args) throws Exception {
//        byte[] json = ByteStreams.toByteArray(ZstdCompressSizeInfo.class.getResourceAsStream("/gsoc-2018.json"));
        byte[] json = ByteStreams.toByteArray(ZstdCompressSizeInfo.class.getResourceAsStream("/twitter.json"));
        System.out.printf("Json initial size %s\n", json.length);
        for (int i = 1; i <= 20; i++) {
            System.out.printf("Level %s size %s\n", i, ZstdUtil.compress(json, i).length);
        }
        byte[] smile = JsonUtil.jsonToSmile(json);
        System.out.printf("Smile initial size %s\n", smile.length);
        for (int i = 1; i <= 20; i++) {
            System.out.printf("Level %s size %s\n", i, ZstdUtil.compress(smile, i).length);
        }

    }

}
