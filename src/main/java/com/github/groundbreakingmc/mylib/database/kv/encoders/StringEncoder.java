package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;

import java.nio.charset.StandardCharsets;

public final class StringEncoder implements KeyValueEncoder<String> {

    @Override
    public byte[] encode(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String decode(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }
}
