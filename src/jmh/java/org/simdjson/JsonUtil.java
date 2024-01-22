package org.simdjson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.google.common.io.ByteStreams;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import java.io.*;

public class JsonUtil {

    private static ObjectMapper smileMapper;
    private static ObjectMapper jsonMapper;
    private static ObjectMapper ionMapper;
    private static ObjectMapper cborMapper;
    private static ObjectMapper messagePackMapper;
    static {
        SmileFactory smileFactory = new SmileFactory();
        smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
        smileMapper = new ObjectMapper(smileFactory);

        ionMapper = new IonObjectMapper();
        cborMapper = new CBORMapper();
        messagePackMapper = new MessagePackMapper();
        jsonMapper = new ObjectMapper();

    }

    public static byte[] jsonToSmile(byte[] input) throws Exception{
        JsonNode node = jsonMapper.readTree(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        smileMapper.writeValue(baos, node);
        return baos.toByteArray();
    }
    public static byte[] jsonToIon(byte[] input) throws Exception{
        JsonNode node = jsonMapper.readTree(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        ionMapper.writeValue(baos, node);
        return baos.toByteArray();
    }
    public static byte[] jsonToCbor(byte[] input) throws Exception{
        JsonNode node = jsonMapper.readTree(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        cborMapper.writeValue(baos, node);
        return baos.toByteArray();
    }
    public static byte[] jsonToMessagePack(byte[] input) throws Exception{
        JsonNode node = jsonMapper.readTree(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        messagePackMapper.writeValue(baos, node);
        return baos.toByteArray();
    }


    public static void main(String[] args) throws Exception {
        byte[] json = ByteStreams.toByteArray(new FileInputStream("/home/cornel/projects/json-benchmark/src/jmh/resources/twitter.json"));
        System.out.println(json.length);
        System.out.println(jsonToSmile(json).length);
        System.out.println(jsonToIon(json).length);
        System.out.println(jsonToCbor(json).length);
        System.out.println(jsonToMessagePack(json).length);

    }

}
