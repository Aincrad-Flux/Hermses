package com.hermses.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMessageSerializer implements MessageSerializer {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Message message) throws Exception {
        return mapper.writeValueAsString(message);
    }

    @Override
    public Message deserialize(String payload) throws Exception {
        return mapper.readValue(payload, Message.class);
    }
}
