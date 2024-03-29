package com.creanga;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ZstdCompressBenchmark {

//    @Param({"/gsoc-2018.json","/twitter.json"})
    @Param({"/github-events.json"})
    String fileName;

//    @Param({"1","3","5","10","20"})
    @Param({"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"})
    String zstdLevel;


    private byte[] buffer;
    private byte[] bufferSmile;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream(fileName)) {
            buffer = is.readAllBytes();
            ZstdUtil.compress(buffer, Integer.parseInt(zstdLevel));
            bufferSmile = JsonUtil.jsonToSmile(buffer);
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
