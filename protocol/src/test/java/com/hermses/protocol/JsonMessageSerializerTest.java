package com.hermses.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonMessageSerializerTest {
    @Test
    void roundTrip() throws Exception {
        JsonMessageSerializer serializer = new JsonMessageSerializer();
        Message original = Message.chat("alice", "bonjour");
        String json = serializer.serialize(original);
        Message back = serializer.deserialize(json);
        assertEquals(original.getType(), back.getType());
        assertEquals(original.getSender(), back.getSender());
        assertEquals(original.getContent(), back.getContent());
        assertTrue(back.getTimestamp() > 0);
    }
}
