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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.simdjson.JsonValue;
import org.simdjson.SimdJsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.creanga.JsonUtil.convertJsonLinesToArray;
import static com.creanga.SimdJsonPaddingUtil.padded;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ParseBenchmark {

    //@Param({"/gsoc-2018.json"})
    @Param({"/sampleArray.json"})
    String fileName;

    private final SimdJsonParser simdJsonParser = new SimdJsonParser();
    private ObjectMapper objectMapper;
    private ObjectMapper smileMapper;
    private byte[] bufferPadded;

    private byte[] buffer;
    private byte[] bufferLz4;
    private byte[] bufferZstd;

    private byte[] bufferSmile;
    private byte[] bufferSmileLz4;
    private byte[] bufferSmileZstd;


    @Setup(Level.Trial)
    public void setup() throws Exception {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream(fileName)) {
            buffer = is.readAllBytes();
            bufferPadded = padded(buffer);
            bufferLz4 = Lz4Util.compress(buffer);
            bufferZstd = ZstdUtil.compress(buffer);

            bufferSmile = JsonUtil.jsonToSmile(buffer);
//            bufferSmile = JsonUtil.jsonToSmile(convertJsonLinesToArray(buffer));
            bufferSmileLz4 = Lz4Util.compress(bufferSmile);
            bufferSmileZstd = ZstdUtil.compress(bufferSmile);

            objectMapper = new ObjectMapper();

            SmileFactory smileFactory = new SmileFactory();
            smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
            smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
            smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
            smileMapper = new ObjectMapper(smileFactory);
        }
    }
    @Benchmark
    public JsonNode jacksonJson() throws IOException {
        return objectMapper.readTree(buffer, 0, buffer.length);
    }
    @Benchmark
    public JsonNode jacksonJsonLz4() throws IOException {
        byte[] decompressed = Lz4Util.decompress(bufferLz4, buffer.length);
        return objectMapper.readTree(decompressed, 0, buffer.length);
    }
    @Benchmark
    public JsonNode jacksonJsonZstd() throws IOException {
        byte[] decompressed = ZstdUtil.decompress(bufferZstd, buffer.length);
        return objectMapper.readTree(decompressed, 0, buffer.length);
    }
    @Benchmark
    public JsonNode jacksonSmile() throws Exception {
        return smileMapper.readTree(bufferSmile, 0, bufferSmile.length);
    }
    @Benchmark
    public JsonNode jacksonSmileLz4() throws Exception {
        byte[] decompressed = Lz4Util.decompress(bufferSmileLz4, bufferSmile.length);
        return smileMapper.readTree(decompressed, 0, bufferSmile.length);
    }
    @Benchmark
    public JsonNode jacksonSmileZstd() throws Exception {
        byte[] decompressed = ZstdUtil.decompress(bufferSmileZstd, bufferSmile.length);
        return smileMapper.readTree(decompressed, 0, bufferSmile.length);
    }

    @Benchmark
    public JsonValue simdjson() {
        return simdJsonParser.parse(buffer, buffer.length);
    }

    @Benchmark
    public JsonValue simdjsonLz4() {
        byte[] decompressed = Lz4Util.decompress(bufferLz4, buffer.length);
        return simdJsonParser.parse(decompressed, buffer.length);
    }

    @Benchmark
    public JsonValue simdjsonZstd() {
        byte[] decompressed = ZstdUtil.decompress(bufferZstd, buffer.length);
        return simdJsonParser.parse(decompressed, buffer.length);
    }

//    @Benchmark
//    public JsonValue simdjsonPadded() {
//        return simdJsonParser.parse(bufferPadded, buffer.length);
//    }
}
