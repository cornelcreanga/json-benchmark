package com.creanga;

import com.google.common.io.ByteStreams;


public class ZstdCompressSizeInfo {

    public static String[] files = new String[]{"/twitter.json","/gsoc-2018.json"};
    public static void main(String[] args) throws Exception {

        for (String file : files) {
            byte[] json = ByteStreams.toByteArray(ZstdCompressSizeInfo.class.getResourceAsStream(file));
            System.out.println("File " + file);
            System.out.printf("Json size %s\n", json.length);
            int len = Lz4Util.compress(json).length;
            System.out.printf("lz4 size %s percentage %,.2f\n",  len, (double)len/json.length);
            len = Lz4Util.compress(json, true).length;
            System.out.printf("lz4high size %s percentage %,.2f\n", len, (double)len/json.length);

            for (int j = 1; j <= 20; j++) {
                len = ZstdUtil.compress(json, j).length;
                System.out.printf("level %s size %s percentage %,.2f\n", j, len, (double)len/json.length);
            }
            byte[] smile = JsonUtil.jsonToSmile(json);
            len = Lz4Util.compress(smile).length;
            System.out.printf("lz4 size %s percentage %,.2f\n",  len, (double)len/smile.length);
            len = Lz4Util.compress(smile, true).length;
            System.out.printf("lz4high size %s percentage %,.2f\n", len, (double)len/smile.length);
            System.out.printf("Smile size %s\n", smile.length);
            for (int j = 1; j <= 20; j++) {
                len = ZstdUtil.compress(smile, j).length;
                System.out.printf("level %s size %s percentage %,.2f\n", j, len, (double)len/smile.length);
            }

        }


    }

}
