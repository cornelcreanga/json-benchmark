package org.simdjson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class JsonUtil {

    private static ObjectMapper smileMapper;
    private static ObjectMapper objectMapper;
    static {
        SmileFactory smileFactory = new SmileFactory();
        smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
        smileMapper = new ObjectMapper(smileFactory);
        objectMapper = new ObjectMapper();
    }

    public static void toSmile(String in, String out) throws Exception{
        JsonNode node = objectMapper.readTree(new File(in));
        smileMapper.writeValue(new FileOutputStream(out), node);

    }

    public static void main(String[] args) throws Exception{
        JsonUtil.toSmile("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json","/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.smile");
    }

}
