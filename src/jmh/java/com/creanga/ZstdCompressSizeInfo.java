package com.creanga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.google.common.io.ByteStreams;

import static com.creanga.JsonUtil.convertJsonLinesToArray;


public class ZstdCompressSizeInfo {

    private static ObjectMapper mapper;
    private static ObjectMapper smileMapper;

    static {
        mapper = new ObjectMapper();
        SmileFactory smileFactory = new SmileFactory();
        smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
        smileMapper = new ObjectMapper(smileFactory);
    }

    public static String[] files = new String[]{"/twitter.json","/gsoc-2018.json"};

    public static void main(String[] args) throws Exception {

        for (String file : files) {
            byte[] json = ByteStreams.toByteArray(ZstdCompressSizeInfo.class.getResourceAsStream(file));
            System.out.println("File " + file);
            System.out.printf("Json size %s\n", json.length);
            int len = Lz4Util.compress(json).length;
            System.out.printf("lz4 size %s percentage %,.2f\n", len, (double) len / json.length);
            len = Lz4Util.compress(json, true).length;
            System.out.printf("lz4high size %s percentage %,.2f\n", len, (double) len / json.length);

            for (int j = 1; j <= 20; j++) {
                len = ZstdUtil.compress(json, j).length;
                System.out.printf("level %s size %s percentage %,.2f\n", j, len, (double) len / json.length);
            }


            byte[] smile = JsonUtil.jsonToSmile(json);
            //byte[] smile = JsonUtil.jsonToSmile(convertJsonLinesToArray(json));
            len = Lz4Util.compress(smile).length;
            System.out.printf("Smile size %s\n", smile.length);
            System.out.printf("lz4 size %s percentage %,.2f\n", len, (double) len / smile.length);
            len = Lz4Util.compress(smile, true).length;
            System.out.printf("lz4high size %s percentage %,.2f\n", len, (double) len / smile.length);

            for (int j = 1; j <= 20; j++) {
                len = ZstdUtil.compress(smile, j).length;
                System.out.printf("level %s size %s percentage %,.2f\n", j, len, (double) len / smile.length);
            }

        }

    }


}
