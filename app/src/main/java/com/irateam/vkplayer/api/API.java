package com.irateam.vkplayer.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public class API {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
