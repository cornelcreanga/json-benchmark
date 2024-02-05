package com.creanga;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ZstdDecompressBenchmark {


    //@Param({"/gsoc-2018.json","/twitter.json"})
    @Param({"/github-events.json"})
    String fileName;

    //    @Param({"1","3","5","10","20"})
    @Param({"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"})
    String zstdLevel;

    private byte[] buffer;
    private byte[] bufferSmile;
    int bufferLength;
    int bufferSmileLength;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        try (InputStream is = ParseBenchmark.class.getResourceAsStream(fileName)) {
            buffer = is.readAllBytes();
            bufferSmile = JsonUtil.jsonToSmile(buffer);
            bufferLength = buffer.length;
            bufferSmileLength = bufferSmile.length;

            buffer = ZstdUtil.compress(buffer, Integer.parseInt(zstdLevel));
            bufferSmile = ZstdUtil.compress(bufferSmile, Integer.parseInt(zstdLevel));
        }
    }


    @Benchmark
    public void decompressPlainJson() throws IOException {
        ZstdUtil.decompress(buffer, bufferLength);
    }

    @Benchmark
    public void decompressSmileJson() throws IOException {
        ZstdUtil.decompress(bufferSmile, bufferSmileLength);
    }

}
