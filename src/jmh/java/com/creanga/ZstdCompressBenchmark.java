package com.creanga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ZstdCompressBenchmark {

    @Param({"/gsoc-2018.json","/twitter.json"})
    String fileName;

//    @Param({"1","3","5","10","20"})
    @Param({"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"})
    String zstdLevel;


    private ObjectMapper objectMapper;
    private ObjectMapper smileMapper;
    private byte[] buffer;
    private byte[] bufferSmile;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream(fileName)) {
            buffer = is.readAllBytes();
            bufferSmile = JsonUtil.jsonToSmile(buffer);
            objectMapper = new ObjectMapper();
            SmileFactory smileFactory = new SmileFactory();
            smileFactory.configure(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT, false);
            smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_NAMES, true);
            smileFactory.configure(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES, true);
            smileMapper = new ObjectMapper(smileFactory);
        }
    }

    @Benchmark
    public void compressPlainJson() throws IOException {
        ZstdUtil.compress(buffer, Integer.parseInt(zstdLevel));
    }

    @Benchmark
    public void compressSmileJson() throws IOException {
        ZstdUtil.compress(bufferSmile, Integer.parseInt(zstdLevel));
    }

}
