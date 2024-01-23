package com.creanga;


import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ConversionBenchmark {


    private byte[] buffer;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream("/gsoc-2018.json")) {
            buffer = is.readAllBytes();
        }
    }

    @Benchmark
    public int convertToSmile() throws Exception {
        return JsonUtil.jsonToSmile(buffer).length;
    };
    @Benchmark
    public int convertToIon() throws Exception {
        return JsonUtil.jsonToIon(buffer).length;
    };
    @Benchmark
    public int convertToCbor() throws Exception {
        return JsonUtil.jsonToCbor(buffer).length;
    };
    @Benchmark
    public int convertToMessagePack() throws Exception {
        return JsonUtil.jsonToMessagePack(buffer).length;
    };
}
