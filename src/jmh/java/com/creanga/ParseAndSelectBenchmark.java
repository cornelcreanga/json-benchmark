package com.creanga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.simdjson.JsonValue;
import org.simdjson.SimdJsonParser;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.creanga.SimdJsonPaddingUtil.padded;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ParseAndSelectBenchmark {

    private SimdJsonParser simdJsonParser;
    private ObjectMapper objectMapper;
    private ObjectMapper smileMapper;

    private byte[] buffer;
    private byte[] bufferLz4;
    private byte[] bufferZstd;

    private byte[] bufferSmile;
    private byte[] bufferSmileLz4;
    private byte[] bufferSmileZstd;

    private byte[] bufferPadded;



    @Setup(Level.Trial)
    public void setup() throws Exception {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/twitter.json")) {
            buffer = is.readAllBytes();
            bufferPadded = padded(buffer);
        }

        bufferSmile = JsonUtil.jsonToSmile(buffer);
        bufferLz4 = Lz4Util.compress(buffer);
        bufferZstd = ZstdUtil.compress(buffer);

        bufferSmileLz4 = Lz4Util.compress(bufferSmile);
        bufferSmileZstd = ZstdUtil.compress(bufferSmile);

        simdJsonParser = new SimdJsonParser();
        objectMapper = new ObjectMapper();

        SmileFactory smileFactory = new SmileFactory();
        smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
        smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
        smileMapper = new ObjectMapper(smileFactory);
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_smile() throws Exception {
        return select(smileMapper, bufferSmile);
    }
    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_smile_lz4() throws Exception {
        return select(smileMapper, Lz4Util.decompress(bufferSmileLz4, bufferSmile.length));
    }
    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_smile_zstd() throws Exception {
        return select(smileMapper, ZstdUtil.decompress(bufferSmileZstd, bufferSmile.length));
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson() throws Exception {
        return select(objectMapper, buffer);
    }
    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_lz4() throws Exception {
        return select(objectMapper, Lz4Util.decompress(bufferLz4, buffer.length));
    }
    @Benchmark
    public int countUniqueUsersWithDefaultProfile_jackson_zstd() throws Exception {
        return select(objectMapper, ZstdUtil.decompress(bufferZstd, buffer.length));
    }


    private int select(ObjectMapper mapper, byte[] data) throws Exception{
        JsonNode jacksonJsonNode = mapper.readTree(data);
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
        return countUsers(simdJsonValue);
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjson_lz4() throws Exception{
        byte[] decompressed = Lz4Util.decompress(bufferLz4, buffer.length);
        JsonValue simdJsonValue = simdJsonParser.parse(decompressed, decompressed.length);
        return countUsers(simdJsonValue);
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjson_zstd() throws Exception{
        byte[] decompressed = ZstdUtil.decompress(bufferZstd, buffer.length);
        JsonValue simdJsonValue = simdJsonParser.parse(decompressed, decompressed.length);
        return countUsers(simdJsonValue);
    }

    @Benchmark
    public int countUniqueUsersWithDefaultProfile_simdjsonPadded() {
        JsonValue simdJsonValue = simdJsonParser.parse(bufferPadded, buffer.length);
        return countUsers(simdJsonValue);
    }

    private static int countUsers(JsonValue simdJsonValue) {
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
