package org.simdjson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.fasterxml.jackson.dataformat.smile.SmileParser;
import net.jpountz.lz4.LZ4BlockInputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.simdjson.SimdJsonPaddingUtil.padded;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ParseAndSelectBenchmark {

    private SimdJsonParser simdJsonParser;
    private ObjectMapper objectMapper;
    private ObjectMapper smileMapper;

    private byte[] buffer;
    private byte[] bufferLz4;
    private byte[] bufferSmileLz4;
    private byte[] bufferSmile;
    private byte[] bufferPadded;



    @Setup(Level.Trial)
    public void setup() throws IOException {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/twitter.json")) {
            buffer = is.readAllBytes();
            bufferPadded = padded(buffer);
        }
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/twitter.smile")) {
            bufferSmile = is.readAllBytes();
        }
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/twitter.json.lz4")) {
            bufferLz4 = is.readAllBytes();
        }
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/twitter.smile.lz4")) {
            bufferSmileLz4 = is.readAllBytes();
        }


        simdJsonParser = new SimdJsonParser();
        objectMapper = new ObjectMapper();

        SmileFactory smileFactory = new SmileFactory();
        smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
        smileMapper = new ObjectMapper(smileFactory);
    }


    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_smile_lz4() throws IOException {
        LZ4BlockInputStream in = new LZ4BlockInputStream(new ByteArrayInputStream(bufferSmileLz4));
        JsonNode jacksonJsonNode = smileMapper.readTree(in);
        Set<String> defaultUsers = new HashSet<>();
        Iterator<JsonNode> tweets = jacksonJsonNode.get("statuses").elements();
        while (tweets.hasNext()) {
            JsonNode tweet = tweets.next();
            JsonNode user = tweet.get("user");
            if (user.get("default_profile").asBoolean()) {
                defaultUsers.add(user.get("screen_name").textValue());
            }
        }
        return defaultUsers.size();
    }


    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_smile() throws IOException {
        JsonNode jacksonJsonNode = smileMapper.readTree(bufferSmile);
        Set<String> defaultUsers = new HashSet<>();
        Iterator<JsonNode> tweets = jacksonJsonNode.get("statuses").elements();
        while (tweets.hasNext()) {
            JsonNode tweet = tweets.next();
            JsonNode user = tweet.get("user");
            if (user.get("default_profile").asBoolean()) {
                defaultUsers.add(user.get("screen_name").textValue());
            }
        }
        return defaultUsers.size();
    }


    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson() throws IOException {
        JsonNode jacksonJsonNode = objectMapper.readTree(buffer);
        Set<String> defaultUsers = new HashSet<>();
        Iterator<JsonNode> tweets = jacksonJsonNode.get("statuses").elements();
        while (tweets.hasNext()) {
            JsonNode tweet = tweets.next();
            JsonNode user = tweet.get("user");
            if (user.get("default_profile").asBoolean()) {
                defaultUsers.add(user.get("screen_name").textValue());
            }
        }
        return defaultUsers.size();
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjson() {
        JsonValue simdJsonValue = simdJsonParser.parse(buffer, buffer.length);
        Set<String> defaultUsers = new HashSet<>();
        Iterator<JsonValue> tweets = simdJsonValue.get("statuses").arrayIterator();
        while (tweets.hasNext()) {
            JsonValue tweet = tweets.next();
            JsonValue user = tweet.get("user");
            if (user.get("default_profile").asBoolean()) {
                defaultUsers.add(user.get("screen_name").asString());
            }
        }
        return defaultUsers.size();
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjson_lz4() {
        byte[] buffer = Lz4Util.decompress(bufferLz4);
        JsonValue simdJsonValue = simdJsonParser.parse(buffer, buffer.length);
        Set<String> defaultUsers = new HashSet<>();
        Iterator<JsonValue> tweets = simdJsonValue.get("statuses").arrayIterator();
        while (tweets.hasNext()) {
            JsonValue tweet = tweets.next();
            JsonValue user = tweet.get("user");
            if (user.get("default_profile").asBoolean()) {
                defaultUsers.add(user.get("screen_name").asString());
            }
        }
        return defaultUsers.size();
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjsonPadded() {
        JsonValue simdJsonValue = simdJsonParser.parse(bufferPadded, buffer.length);
        Set<String> defaultUsers = new HashSet<>();
        Iterator<JsonValue> tweets = simdJsonValue.get("statuses").arrayIterator();
        while (tweets.hasNext()) {
            JsonValue tweet = tweets.next();
            JsonValue user = tweet.get("user");
            if (user.get("default_profile").asBoolean()) {
                defaultUsers.add(user.get("screen_name").asString());
            }
        }
        return defaultUsers.size();
    }
}
